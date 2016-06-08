package org.rso.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.rso.exceptions.NodeNotFoundException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Log
public class AppProperty {

    public static final int EXTERNAL_SERVER_PORT = 9000;
    public static final int INTERNAL_SERVER_PORT = 8080;

    private static AppProperty appProperty;
    private Date lastCoordinatorPresence;
    @Getter @Setter
    private NodeInfo selfNode;
    @Getter @Setter
    private NodeInfo coordinatorNode;

    private AtomicInteger idCounter = null;

    private List<NodeInfo> listOfAvaiableNodes = new ArrayList<>();

    private AppProperty(){
        this.lastCoordinatorPresence = new Date();
    }

    public synchronized static AppProperty getInstance() {
        if(appProperty==null){
            appProperty = new AppProperty();
        }
        return appProperty;
    }

    public synchronized NodeInfo allocateNewNode() {

        /* TODO:
               This is retarded. Change appProeprty impleemntation to Map
         */
        if(idCounter == null) {

            if(selfNode == null && coordinatorNode == null) {
                idCounter = new AtomicInteger(1);
            } else {

                if(!listOfAvaiableNodes.isEmpty()) {
                    final int availableNodesMax = listOfAvaiableNodes.stream().map(NodeInfo::getNodeId).mapToInt(Integer::intValue).max().getAsInt();

                    idCounter = new AtomicInteger(
                            Math.max(Math.max(availableNodesMax, selfNode.getNodeId()), coordinatorNode.getNodeId())
                    );
                } else {
                    idCounter = new AtomicInteger(
                            Math.max(selfNode.getNodeId(), coordinatorNode.getNodeId())
                    );
                }

            }
        }

        return NodeInfo.builder()
                .nodeId(idCounter.incrementAndGet())
                .nodeIPAddress("")
                .nodeType(NodeType.INTERNAL)
                .build();
    }

    public synchronized void setLastCoordinatorPresence(Date lastCoordinatorPresence) {
        log.info("aktualizujemy date obecnosci koordynatora "+DataTimeLogger.logTime(lastCoordinatorPresence));
        this.lastCoordinatorPresence = lastCoordinatorPresence;
    }

    public synchronized Date getLastCoordinatorPresence() {
        return lastCoordinatorPresence;
    }

    public synchronized List<NodeInfo> getAvailableNodes() {
        return listOfAvaiableNodes;
    }

    public synchronized void setListOfAvaiableNodes(List<NodeInfo> listOfAvaiableNodes) {
        this.listOfAvaiableNodes = listOfAvaiableNodes;
    }

    public synchronized void addAvaiableNode(NodeInfo n){
        this.listOfAvaiableNodes.add(n);
    }

    public synchronized void removeNode(int nodeId){
        this.listOfAvaiableNodes = listOfAvaiableNodes.stream().
                                    filter(p -> p.getNodeId() != nodeId).
                                    collect(Collectors.toCollection(ArrayList::new));
    }

    public List<String> getAvaiableNodesIpAddresses(){
        return this.listOfAvaiableNodes.stream().map(p->p.getNodeIPAddress()).collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean isSelfNodeCoordinator(){
        return selfNode.equals(coordinatorNode);
    }

    public NetworkStatus getNetworkStatus() {
        return NetworkStatus.builder()
                .coordinator(appProperty.getCoordinatorNode())
                .nodes(appProperty.getAvailableNodes().stream().collect(toList()))
                .build();
    }

    public List<NodeInfo> getAllNodes() {
        final List<NodeInfo> collect = getAvailableNodes().stream().collect(Collectors.toList());
        collect.add(getCoordinatorNode());
        return collect;
    }

    public Map<Location, List<NodeInfo>> getReplicationMap() {
        final Map<Location, List<NodeInfo>> replicationMap = new HashMap<>();

        getAllNodes().stream()
                    .forEach(nodeInfo ->
                            Optional.ofNullable(nodeInfo.getLocations())
                                .ifPresent(locations -> locations.forEach(location -> {
                                    if(replicationMap.containsKey(location)) {
                                        replicationMap.get(location).add(nodeInfo);
                                    } else {
                                        final List<NodeInfo> nodeInfos = new ArrayList<>();
                                        nodeInfos.add(nodeInfo);
                                        replicationMap.put(location, nodeInfos);
                                    }
                                }))
                            );

        return replicationMap;
    }


    public NodeInfo getNodeById(final int nodeId) throws NodeNotFoundException {
        return getAvailableNodes().stream()
                .filter(node -> node.getNodeId() == nodeId)
                .findFirst()
                .orElse(null);
//                .orElseThrow(() -> new NodeNotFoundException(String.format("Node with id = %s does not exist", nodeId)));
    }

}

