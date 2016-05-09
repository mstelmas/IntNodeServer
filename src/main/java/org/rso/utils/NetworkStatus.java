package org.rso.utils;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NetworkStatus {
    private final NodeInfo coordinator;
    private final List<NodeInfo> nodes;
}
