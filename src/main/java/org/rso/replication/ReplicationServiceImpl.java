package org.rso.replication;

import javaslang.control.Try;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.rso.dto.DtoConverters;
import org.rso.dto.UniversityDto;
import org.rso.repositories.UniversityRepo;
import org.rso.utils.AppProperty;
import org.rso.utils.Location;
import org.rso.utils.NodeInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Log
@Service
public class ReplicationServiceImpl implements ReplicationService {

    @Resource
    private UniversityRepo universityRepo;

    @Value("${timeout.request.read}")
    private int readTimeout;

    @Value("${timeout.request.connect}")
    private int connectionTimeout;

    @Value("${log.tag.coordinator}")
    private String coordinatorTag;

    @Value("${log.tag.replication}")
    private String replicationTag;

    private final RestTemplate restTemplate = new RestTemplate();

    private final AppProperty appProperty = AppProperty.getInstance();

    private static final String DEFAULT_NODES_PORT = "8080";
    private static final String REPLICATION_URL = "http://{ip}:{port}/utils/replication";

    @PostConstruct
    public void initialize() {
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(readTimeout);
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(connectionTimeout);
    }

    @Override
    public List<Location> getTopLocations(final int topLocations) {

        final Map<Location, List<NodeInfo>> replicationMap = appProperty.getReplicationMap();

        final int locationEntriesNumber = replicationMap.size();

        if(topLocations < 1 || topLocations > locationEntriesNumber) {
            throw new RuntimeException(
                    String.format("Cannot retrieve %d topLocations locations from location map of %d entries", topLocations, locationEntriesNumber)
            );
        }

        return replicationMap.entrySet().stream()
                .sorted((es1, es2) -> Integer.compare(es1.getValue().size(), es2.getValue().size()))
                .limit(topLocations)
                .map(Map.Entry::getKey)
                .collect(toList());
    }

    @Override
    public Try<Void> replicateLocation(@NonNull final Location location, @NonNull final NodeInfo nodeInfo) {

        return Try.run(() -> {

            log.info(
                    String.format("%s %s: Replicating location %s on node: %d [%s]",
                            coordinatorTag, replicationTag, location,
                            nodeInfo.getNodeId(),
                            nodeInfo.getNodeIPAddress())
            );

            final List<UniversityDto> universitiesForLocationDto = Optional.ofNullable(universityRepo.findByLocation(location))
                    .orElseThrow(() -> new RuntimeException(
                            String.format("No universities found for location: %s", location.toString()))
                    ).stream()
                        .map(DtoConverters.universityEntityToDto)
                        .collect(Collectors.toList());


            final ResponseEntity<Void> replicationResponseEntity = restTemplate.postForEntity (
                    REPLICATION_URL,
                    universitiesForLocationDto,
                    Void.class,
                    nodeInfo.getNodeIPAddress(),
                    DEFAULT_NODES_PORT
            );

            if(replicationResponseEntity.getStatusCode() != HttpStatus.OK) {
                log.warning(
                        String.format("%s %s: Could not replicate location: %s on node %d [%s] (HTTP Status: %s)",
                                coordinatorTag, replicationTag, location,
                                nodeInfo.getNodeId(), nodeInfo.getNodeIPAddress(),
                                replicationResponseEntity.getStatusCode()
                        )
                );
                throw new RuntimeException(
                        String.format("Could not replicate location %s on node: %d [%s] (HTTP Status: %s)",
                                location, nodeInfo.getNodeId(), nodeInfo.getNodeIPAddress(),
                                replicationResponseEntity.getStatusCode())
                );
            }
        });
    }

    @Override
    public Try<Void> replicateLocation(@NonNull final Location location, @NonNull final NodeInfo sourceNode, @NonNull final NodeInfo destNode) {

        /* If we are replicating from coordinator, there is no need for remote downloading of the content */
        if (sourceNode.equals(appProperty.getCoordinatorNode())) {
            return replicateLocation(location, destNode);
        }

        return Try.run(() -> {
            final UniversityDto[] locationToReplicate = restTemplate.getForObject(
                    REPLICATION_URL,
                    UniversityDto[].class,
                    sourceNode.getNodeIPAddress(),
                    DEFAULT_NODES_PORT,
                    location
            );

            log.info(
                    String.format("%s %s: Received replication data about location %s from %d [%s]",
                            coordinatorTag, replicationTag, location, sourceNode.getNodeId(),
                            sourceNode.getNodeIPAddress()
                    )
            );

            final ResponseEntity<Void> replicationResponseEntity = restTemplate.postForEntity (
                    REPLICATION_URL,
                    locationToReplicate,
                    Void.class,
                    destNode.getNodeIPAddress(),
                    DEFAULT_NODES_PORT
            );

            if(replicationResponseEntity.getStatusCode() != HttpStatus.OK) {
                log.warning(
                        String.format("%s %s: Could not replicate location: %s on node %d [%s] (HTTP Status: %s)",
                                coordinatorTag, replicationTag, location,
                                destNode.getNodeId(), destNode.getNodeIPAddress(),
                                replicationResponseEntity.getStatusCode()
                        )
                );
                throw new RuntimeException(
                        String.format("Could not replicate location %s on node: %d [%s] (HTTP Status: %s)",
                                location, destNode.getNodeId(), destNode.getNodeIPAddress(),
                                replicationResponseEntity.getStatusCode())
                );
            }
        });
    }
}
