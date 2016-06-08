package org.rso.configuration.services;

import org.rso.network.NodeInfo;

import java.util.List;

public interface NodesCfgService {
    List<NodeInfo> getAllNodes();
    List<NodeInfo> getInternalNodes();
    NodeInfo getCoordinatorNode();
    NodeInfo getNodeById(final int nodeId);
    NodeInfo getSelfNode();
}
