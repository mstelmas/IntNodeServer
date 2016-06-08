package org.rso.network.services;

import javaslang.control.Try;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.rso.network.dto.NetworkStatusDto;
import org.rso.network.dto.NodeStatusDto;
import org.rso.utils.AppProperty;
import org.rso.utils.NodeInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Log
@Service
public class NodeNetworkServiceImpl implements NodeNetworkService {

    @Value("${timeout.request.read}")
    private int readTimeout;

    @Value("${timeout.request.connect}")
    private int connectionTimeout;

    @Value("${log.tag.coordinator}")
    private String coordinatorTag;

    @Value("${log.tag.management}")
    private String managementTag;

    private final RestTemplate restTemplate = new RestTemplate();

    private final AppProperty appProperty = AppProperty.getInstance();

    private static final String STATUS_URL = "http://{ip}:{port}/utils/status";
    private static final String HEARTBEAT_URL = "http://{ip}:{port}/utils/heartbeat";
    private static final String DEFAULT_NODES_PORT = "8080";

    @PostConstruct
    public void initialize() {
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(readTimeout);
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(connectionTimeout);
    }

    @Override
    public Try<NetworkStatusDto> getNetworkStatus() {

        for(NodeInfo nodeInfo : appProperty.getAvailableNodes()) {
            final Try<NetworkStatusDto> networkStatus = getNetworkStatus(nodeInfo);

            if(networkStatus.isSuccess()) {
                return networkStatus;
            }

        }
        return Try.failure(new RuntimeException("No nodes available to get a network status"));
    }

    @Override
    public Try<NetworkStatusDto> getNetworkStatus(@NonNull final NodeInfo nodeInfo) {
        return Try.of(() -> {

            final ResponseEntity<NetworkStatusDto> networkStatusEntity = restTemplate.getForEntity(
                    STATUS_URL,
                    NetworkStatusDto.class,
                    nodeInfo.getNodeIPAddress(),
                    DEFAULT_NODES_PORT
            );

            if(networkStatusEntity.getStatusCode() != HttpStatus.OK) {

                final String errorMessage = String.format("Unable to get network status from node: %d [%s]", nodeInfo.getNodeId(), nodeInfo.getNodeIPAddress());

                log.info(errorMessage);
                throw new RuntimeException(errorMessage);
            }

            return networkStatusEntity.getBody();
        });
    }

    @Override
    public Try<NodeStatusDto> getHeartbeat(@NonNull final NodeInfo nodeInfo) {
        return Try.of(() ->
             restTemplate.getForObject(
                HEARTBEAT_URL,
                NodeStatusDto.class,
                nodeInfo.getNodeIPAddress(),
                DEFAULT_NODES_PORT
            )
        );
    }

    @Override
    public Try<Void> setNetworkStatus(@NonNull final NodeInfo nodeInfo, @NonNull final NetworkStatusDto networkStatusDto) {
        return Try.run(() -> {
            final ResponseEntity<Void> networkStatusUpdateEntity = restTemplate.postForEntity(
                    STATUS_URL,
                    networkStatusDto,
                    Void.class,
                    nodeInfo.getNodeIPAddress(),
                    DEFAULT_NODES_PORT
            );

            if(networkStatusUpdateEntity.getStatusCode() != HttpStatus.NO_CONTENT) {
                    /* TODO: This might create some integrity issues. Consider what to do here... */
                log.warning(
                        String.format("%s %s: Unable to update network status in node %d [%s]",
                        coordinatorTag, managementTag,
                        nodeInfo.getNodeId(), nodeInfo.getNodeIPAddress())
                );
                throw new RuntimeException("Unable to update network status in node: " + nodeInfo.getNodeId());
            }

            log.info(
                    String.format("%s %s: Updated network status successfully sent to node %d [%s]",
                    coordinatorTag, managementTag,
                    nodeInfo.getNodeId(), nodeInfo.getNodeIPAddress())
            );
        });
    }
}
