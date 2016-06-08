package org.rso.network;

import lombok.Builder;
import lombok.Data;
import org.rso.utils.NodeInfo;

import java.util.List;

@Data
@Builder
public class NetworkStatus {
    private final NodeInfo coordinator;
    private final List<NodeInfo> nodes;
}
