package org.rso.network.services;

import javaslang.control.Try;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.rso.DtoConverters;
import org.rso.configuration.services.AppProperty;
import org.rso.network.NetworkStatus;
import org.rso.network.dto.NetworkStatusDto;
import org.rso.network.dto.NodeStatusDto;
import org.rso.network.NodeInfo;
import org.rso.network.types.NodeType;
import org.rso.replication.ReplicationService;
import org.rso.storage.types.Location;
import org.rso.utils.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/*
    TODO:
     1. Parallel calls to nodes
     2. # of retries before a node is considered inactive and therefore removed
 */
@Log
@Service("internalNodeUtilService")
public class InternalNodeUtilService implements NodeUtilService {

    @Value("${delay.election}")
    private long electionDelay;

    @Value("${log.tag.coordinator}")
    private String coordinatorTag;

    @Value("${log.tag.election}")
    private String electionTag;

    @Value("${log.tag.heartbeat}")
    private String heartbeatTag;

    @Value("${log.tag.replication}")
    private String replicationTag;

    @Value("${log.tag.management}")
    private String managementTag;

    @Value("${timeout.request.read}")
    private int readTimeout;

    @Value("${timeout.request.connect}")
    private int connectionTimeout;

    @Value("${replication.redundancy}")
    private int replicationRedundancy;

    @Resource
    private NodeNetworkService nodeNetworkService;

    @Resource
    private ReplicationService replicationService;

    private static final String DEFAULT_NODES_PORT = "8080";
    private static final String ELECTION_URL = "http://{ip}:{port}/utils/election";
    private static final String COORDINATOR_URL = "http://{ip}:{port}/utils/coordinator";

    private final AppProperty appProperty = AppProperty.getInstance();

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void initialize() {
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(readTimeout);
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(connectionTimeout);
    }

    public void doHeartBeat() {

        if(appProperty.getAvailableNodes().isEmpty()) {
            return;
        }

        final NodeInfo selfNode = Optional.ofNullable(appProperty.getSelfNode())
                .orElseThrow(() -> new RuntimeException("Critical state in network! Self node not initialized during Heartbeat"));

        if(!selfNode.equals(appProperty.getCoordinatorNode())) {
            throw new RuntimeException("Critical state in network! Heartbeat invoked by non coordinator!");
        }

        log.info(String.format("%s %s: Running heartbeat checks (%s nodes)", coordinatorTag, heartbeatTag, appProperty.getAvailableNodes().size()));

        appProperty.getAvailableNodes().forEach(nodeInfo ->
                nodeNetworkService.getHeartbeat(nodeInfo)
                    .onSuccess(nodeStatusDto ->
                        log.info(
                                String.format("%s %s: Heartbeat check of %s received: %s",
                                        coordinatorTag, heartbeatTag,
                                        nodeInfo.getNodeIPAddress(), nodeStatusDto)
                        )
                    )
                    .onFailure(e -> {
                        log.info(
                                String.format("%s %s: Node %s stopped responding",
                                        coordinatorTag, heartbeatTag, nodeInfo.getNodeIPAddress())
                        );

                        /* Node stopped responding. We need to: */

                        /* 1. remove it from a list of available nodes */
                        appProperty.removeNode(nodeInfo.getNodeId());

                        /* 2. Replicate data placed on that node */
                        final Optional<List<Location>> locationsStoredOnNode = Optional.ofNullable(nodeInfo.getLocations());

                        if(!locationsStoredOnNode.isPresent()) {
                            log.info(
                                    String.format("%s %s: Node %d [%s] contains uninitialized reference to collection of locations." +
                                            "Data integrity might be 'compromised'. Aborting replication!",
                                            coordinatorTag, replicationTag, nodeInfo.getNodeId(), nodeInfo.getNodeIPAddress())
                            );

                            throw new RuntimeException(
                                    String.format("Uninitialized reference to locations stored on node %d [%s]",
                                            nodeInfo.getNodeId(), nodeInfo.getNodeIPAddress())
                            );
                        }

                        if(locationsStoredOnNode.get().isEmpty()) {
                            log.info(
                                    String.format("%s %s: Node %d [%s] did not store any data! There is no need for any replication!",
                                            coordinatorTag, replicationTag, nodeInfo.getNodeId(), nodeInfo.getNodeIPAddress())
                            );
                        } else {
                            log.info(
                                    String.format("%s %s: Node %d [%s] stored the following data: %s. The need for data replication is real :(",
                                            coordinatorTag, replicationTag,
                                            nodeInfo.getNodeId(), nodeInfo.getNodeIPAddress(),
                                            nodeInfo.getLocations())
                            );


                            final NetworkStatus currentNetworkStatus = appProperty.getNetworkStatus();

                            if(currentNetworkStatus.getNodes().isEmpty()) {
                                log.info(
                                        String.format("%s %s: Network does not contain any nodes. Aborting replication...", coordinatorTag, replicationTag)
                                );
                            } else {
                                replicateLocations(locationsStoredOnNode.get());
                            }
                        }

                        /* 3. Inform all the remaining nodes about changes in network */
                        final NetworkStatusDto updatedNetworkStatusDto = DtoConverters.networkStatusEntityToDto.apply(
                                appProperty.getNetworkStatus()
                        );

                        appProperty.getAvailableNodes().forEach(availableNodeInfo ->
                                nodeNetworkService.setNetworkStatus(availableNodeInfo, updatedNetworkStatusDto)
                                        .onFailure(ez ->
                                                /* A node suddenly stopped responding; we don't need to do anything here though
                                                   since it will be removed during the next Heartbeat check iteration anyway.
                                                 */
                                                log.info(String.format("%s %s: Node %s stopped responding during network status update. It should be removed in the next Heartbeat check",
                                                        coordinatorTag, managementTag, availableNodeInfo.getNodeIPAddress()))
                                        )
                        );
                    })
        );
    }

    /* Replication Algorithm */
    private Try<Void> replicateLocations(@NonNull final List<Location> locations) {

        final Map<Location, List<NodeInfo>> replicationMap = appProperty.getReplicationMap();

        locations.forEach(locationToReplicate -> {
            final List<NodeInfo> nodesWithLocation = Optional.ofNullable(replicationMap.get(locationToReplicate))
                        .orElseThrow(() -> new RuntimeException("No node with location: " + locationToReplicate + " found"));

            if(nodesWithLocation.size() >= replicationRedundancy) {
                log.info(
                        String.format("%s %s: Location %s is already duplicated %d times. Skipping this one...",
                                coordinatorTag, replicationTag,
                                locationToReplicate, nodesWithLocation.size())
                );
            } else if(nodesWithLocation.size() == 0 || nodesWithLocation.isEmpty()){
                log.warning(
                        String.format("%s %s: Location %s is not stored on any other node! It has been lost for eternity...",
                                coordinatorTag, replicationTag, locationToReplicate)
                );
            } else {
                final NodeInfo replicationSource = pickReplicationSourceNode(nodesWithLocation);

                final NodeInfo replicationDestination = pickReplicationDestNode(locationToReplicate, replicationMap, replicationSource)
                        .orElseThrow(() -> new RuntimeException("No available node found for replication destination!"));

                log.info(
                        String.format("%s %s: Performing replication of location: %s from source node: %d [%s] to destination: %d [%s]",
                                coordinatorTag, replicationTag,
                                locationToReplicate,
                                replicationSource.getNodeId(), replicationSource.getNodeIPAddress(),
                                replicationDestination.getNodeId(), replicationDestination.getNodeIPAddress())
                );

                replicationService.replicateLocation(locationToReplicate, replicationSource, replicationDestination)
                        .onSuccess(e -> {
                            log.info(
                                    String.format("%s %s: Successfully replicated location %s on node %d [%s]",
                                            coordinatorTag, replicationTag, locationToReplicate,
                                            replicationDestination.getNodeId(), replicationDestination.getNodeIPAddress())
                            );

                            appProperty.getNodeById(replicationDestination.getNodeId()).getLocations().add(locationToReplicate);
                        })
                        .onFailure(e -> {
                            log.warning(
                                    String.format("%s %s: Could not replicate location %s on node %d [%s] (reason: %s)",
                                            coordinatorTag, replicationTag, locationToReplicate,
                                            replicationDestination.getNodeId(), replicationDestination.getNodeIPAddress(),
                                            e.getMessage())
                            );
                            throw new RuntimeException(
                                    String.format("Could not replicate location %s on node %d [%s] because of %s",
                                            locationToReplicate, replicationDestination.getNodeId(), replicationDestination.getNodeIPAddress(),
                                            e.getMessage()
                                    )
                            );
                        });

            }
        });
        return Try.success(null);
    }

    /* We want to return a node which:
            1. Does not contain replicated location
            2. Stores the lowest number of locations
            3. Is no equal to sourceNode
     */
    private Optional<NodeInfo> pickReplicationDestNode(@NonNull final Location locationToReplicate,
                                                       @NonNull final Map<Location, List<NodeInfo>> replicationMap,
                                                       @NonNull final NodeInfo sourceNode) {
        return replicationMap.entrySet().stream()
                .filter(locationListEntry -> !locationListEntry.getKey().equals(locationToReplicate))
                .sorted((es1, es2) -> Integer.compare(es1.getValue().size(), es2.getValue().size()))
                .flatMap(locationListEntry -> locationListEntry.getValue().stream())
                .filter(nodeInfo -> !nodeInfo.equals(sourceNode))
                .findAny();
    }

    /* We want to return a node which will be used as a location source */
    private NodeInfo pickReplicationSourceNode(@NonNull final List<NodeInfo> nodesWithLocation) {
        return nodesWithLocation.get(0); // despite looking suspiciously, this is actually safe!
    }


    /*
    * 1-pobrac wszystkie wezly o wiekszym identyfikatorze
    * 2-nawiazac kontakt z kazdym
    *       a - jezeli odpowie przerwij proces elekcji
    *       b - jezeli nie odpowie jestes koordnatorem
    *               -poinformuj wszystkie wezly o tym fakcjie
    *               -zmien swoje glowne ustawienia
    *               */
    public void doElection(){
        log.info(String.format("%s: Running election procedure", electionTag));

        final int selfNodeId = appProperty.getSelfNode().getNodeId();
        final List<String> listOfIpAddresses = getAvailableIPAddresses(appProperty.getAvailableNodes(), selfNodeId);

        if(listOfIpAddresses.isEmpty()) {
//            koniec elekcji jestem nowym koorynatorem

            /* Election process is done. I am the new coordinator */

            comunicateAsNewCoordinator();
        } else {
//            proces elekcji dla innych wezlow

            for(String ip: listOfIpAddresses){

                try {

                    final NodeStatusDto info = restTemplate.postForObject(
                            ELECTION_URL,
                            appProperty.getSelfNode(),
                            NodeStatusDto.class,
                            ip,
                            DEFAULT_NODES_PORT
                    );
                    log.info("info "+ info);
                    if(info.getNodeId()>selfNodeId){
                        return;
                    }
                }catch (Exception e){
                    log.info(String.format("%s: Exception during election procedure - host %s not found", electionTag, ip));
                }

            }
        }
    }

    /* TODO: Refactor using Yoda Time */
    public void verifyCoordinatorPresence() {
        Date lastPresence = appProperty.getLastCoordinatorPresence();
        final NodeInfo currentCoordinator = appProperty.getCoordinatorNode();
//        log.info("koordynator obecny byl ostatnio " + DataTimeLogger.logTime(lastPresence));
        log.info(String.format("Coordinator (id = %s, IP = %s) last seen: %s",
                currentCoordinator.getNodeId(), currentCoordinator.getNodeIPAddress(), DataTimeLogger.logTime(lastPresence)));
        long dif = DateComparator.compareDate(lastPresence,new Date());

        if(dif > electionDelay){
            doElection();
        }
    }

    /*
    * powiadom wszytskich ze jestes nowym koordynatorem
    * to automatycznie usuwa koordynatorow jako dostepne serwery z listy AppProperty
    * Rozpocznij proces replikacji danych ktore byly dostepne na koordynatorze - chyba najtrudniejsze jak narazie*/
    private void comunicateAsNewCoordinator() {

        final NodeInfo currentSelfNode = appProperty.getSelfNode();

        final NodeInfo oldCoordinatorNode = appProperty.getCoordinatorNode();

        /////////////////////////////////////////////////////////////
        /* Coordinator stopped responding. We need to: */

        /* 1. Replicate data placed on that coordinator */
        final Optional<List<Location>> locationsStoredOnCoordinator = Optional.ofNullable(oldCoordinatorNode.getLocations());

        if(!locationsStoredOnCoordinator.isPresent()) {
            log.info(
                    String.format("%s %s: Old coordinator %d [%s] contains uninitialized reference to collection of locations." +
                                    "Data integrity might be 'compromised'. Aborting replication!",
                            coordinatorTag, replicationTag, oldCoordinatorNode.getNodeId(), oldCoordinatorNode.getNodeIPAddress())
            );

            throw new RuntimeException(
                    String.format("Uninitialized reference to locations stored on node %d [%s]",
                            oldCoordinatorNode.getNodeId(), oldCoordinatorNode.getNodeIPAddress())
            );
        }

        final NodeInfo newCoordinatorNode = NodeInfo.builder()
                .nodeId(currentSelfNode.getNodeId())
                .nodeIPAddress(currentSelfNode.getNodeIPAddress())
                .nodeType(NodeType.INTERNAL_COORDINATOR)
                .locations(currentSelfNode.getLocations())
                .build();

        appProperty.removeNode(currentSelfNode.getNodeId());
        appProperty.setCoordinatorNode(newCoordinatorNode);
        appProperty.setSelfNode(newCoordinatorNode);

        if(locationsStoredOnCoordinator.get().isEmpty()) {
            log.info(
                    String.format("%s %s: Old coordinator %d [%s] did not store any data! There is no need for any replication!",
                            coordinatorTag, replicationTag, oldCoordinatorNode.getNodeId(), oldCoordinatorNode.getNodeIPAddress())
            );
        } else {
            log.info(
                    String.format("%s %s: Old coordinator %d [%s] stored the following data: %s. The need for data replication is real :(",
                            coordinatorTag, replicationTag,
                            oldCoordinatorNode.getNodeId(), oldCoordinatorNode.getNodeIPAddress(),
                            oldCoordinatorNode.getLocations())
            );


            final NetworkStatus currentNetworkStatus = appProperty.getNetworkStatus();

            if(currentNetworkStatus.getNodes().isEmpty()) {
                log.info(
                        String.format("%s %s: Network does not contain any nodes. Aborting replication...", coordinatorTag, replicationTag)
                );
            } else {
                replicateLocations(locationsStoredOnCoordinator.get());
            }
        }

        log.info(String.format("%s: I am the new coordinator! %s", coordinatorTag, appProperty.getSelfNode()));

        final HttpEntity<NodeStatusDto> selfNodeStatusDtoHttpEntity =
                new HttpEntity<>(DtoConverters.nodeInfoToNodeStatusDto.apply(appProperty.getSelfNode()));

        /* TODO: parallel calls to nodes */
        appProperty.getAvaiableNodesIpAddresses().forEach(nodeIpAddress -> {
            Try.run(() -> {
                final ResponseEntity<Void> newCoordinatorResponseEntity = restTemplate.exchange(
                        COORDINATOR_URL,
                        HttpMethod.PUT,
                        selfNodeStatusDtoHttpEntity,
                        Void.class,
                        nodeIpAddress,
                        DEFAULT_NODES_PORT
                );

                log.info(String.format("%s %s: Elected coordinator update returned status: %s", coordinatorTag, electionTag, newCoordinatorResponseEntity.getStatusCode()));
            }).onFailure(e -> log.info(String.format("%s %s: Node %s stopped responding? %s", coordinatorTag, heartbeatTag, nodeIpAddress, e.getMessage())));
        });

        /* 3. Inform all the remaining nodes about changes in network */
        final NetworkStatusDto updatedNetworkStatusDto = DtoConverters.networkStatusEntityToDto.apply(
                appProperty.getNetworkStatus()
        );

        appProperty.getAvailableNodes().forEach(availableNodeInfo ->
                nodeNetworkService.setNetworkStatus(availableNodeInfo, updatedNetworkStatusDto)
                        .onFailure(ez ->
                                                /* A node suddenly stopped responding; we don't need to do anything here though
                                                   since it will be removed during the next Heartbeat check iteration anyway.
                                                 */
                                log.info(String.format("%s %s: Node %s stopped responding during network status update. It should be removed in the next Heartbeat check",
                                        coordinatorTag, managementTag, availableNodeInfo.getNodeIPAddress()))
                        )
        );

        log.info(String.format("%s %s: Election process completed!", coordinatorTag, electionTag));
    }

    private List<String> getAvailableIPAddresses(final List<NodeInfo> availableNodes, final int selfNodeId) {
        return availableNodes.stream()
                .filter(node -> node.getNodeId() > selfNodeId)
                .map(NodeInfo::getNodeIPAddress)
                .collect(toList());
    }
}
