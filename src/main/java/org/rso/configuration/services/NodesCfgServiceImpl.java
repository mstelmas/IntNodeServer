package org.rso.configuration.services;

import com.google.common.collect.ImmutableList;
import lombok.Setter;
import org.rso.storage.types.Location;
import org.rso.network.NodeInfo;
import org.rso.network.types.NodeType;

import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * Properties based nodes configuration
 */

/* TODO:
    1. More Integrity checks (id duplicates etc)
    2. Replace coordinator's reference with its id
    3. Refactor to Java 8
 */
public class NodesCfgServiceImpl implements NodesCfgService {

    private final Map<Integer, NodeInfo> nodes = new HashMap<>();
    private NodeInfo coordinatorNode;

    private int coordinatorId;
    private int selfId;

    private static final String NODES_COUNT = "size";
    private static final String NODE_SELF_ID = "self";

    @Setter
    private Properties properties;

    @Setter
    private String propertyKey;

    public void afterPropertiesSet() {
        final int countOfNodes = Integer.parseInt(properties.getProperty(propertyKey + "." + NODES_COUNT));

        if(countOfNodes <= 0) {
            throw new IllegalArgumentException();
        }

        selfId = Integer.parseInt(properties.getProperty(propertyKey + "." + NODE_SELF_ID));

        for(int i = 0; i < countOfNodes; i++) {

            final NodeType nodeType = NodeType.valueOf(properties.getProperty(propertyValueOf(NodeInfo.NODE_TYPE, i)));

            if(nodeType == NodeType.INTERNAL_COORDINATOR) {
                if(coordinatorNode != null) {
                    throw new RuntimeException("Coordinator already selected!");
                }

                coordinatorNode = NodeInfo.builder()
                        .nodeId(Integer.parseInt(properties.getProperty(propertyValueOf(NodeInfo.NODE_ID, i))))
                        .nodeIPAddress(properties.getProperty(propertyValueOf(NodeInfo.NODE_ADDRESS, i)))
                        .nodeType(NodeType.INTERNAL_COORDINATOR)
                        .locations(Arrays.asList(Location.values())) // initial coordinator has all data on stratup
                        .build();

                nodes.put(coordinatorNode.getNodeId(), coordinatorNode);

            } else {
                final NodeInfo internalNode = NodeInfo.builder()
                        .nodeId(Integer.parseInt(properties.getProperty(propertyValueOf(NodeInfo.NODE_ID, i))))
                        .nodeIPAddress(properties.getProperty(propertyValueOf(NodeInfo.NODE_ADDRESS, i)))
                        .nodeType(NodeType.INTERNAL)
                        .build();

                nodes.put(internalNode.getNodeId(), internalNode);
            }
        }

        if(coordinatorNode == null) {
            throw new RuntimeException("No coordinator specified!");
        }

        if(nodes.get(selfId) == null) {
            throw new RuntimeException("The specified self node does not exist");
        }
    }

    @Override
    public List<NodeInfo> getAllNodes() {
        return ImmutableList.copyOf(
                nodes.entrySet().stream().map(Map.Entry::getValue).collect(toList())
        );
    }

    @Override
    public List<NodeInfo> getInternalNodes() {
        return ImmutableList.copyOf(
                nodes.entrySet().stream().map(Map.Entry::getValue).filter(internalNode).collect(toList())
        );
    }

    @Override
    public NodeInfo getCoordinatorNode() {
        return coordinatorNode;
    }

    @Override
    public NodeInfo getNodeById(final int nodeId) {
        return nodes.get(nodeId);
    }

    @Override
    public NodeInfo getSelfNode() {
        return getNodeById(selfId);
    }

    private String propertyValueOf(final String propertyValue, final int i) {
        return propertyKey + "." + i + "." + propertyValue;
    }

    private Predicate<NodeInfo> internalNode = nodeInfo -> nodeInfo.getNodeType().equals(NodeType.INTERNAL);
}
