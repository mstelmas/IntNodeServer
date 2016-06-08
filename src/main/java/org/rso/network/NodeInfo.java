package org.rso.network;

import lombok.*;
import org.rso.storage.types.Location;
import org.rso.network.types.NodeType;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class NodeInfo {

    public static final String NODE_ID = "id";
    public static final String NODE_ADDRESS = "ip";
    public static final String NODE_TYPE = "type";

    private int nodeId;
    private String nodeIPAddress;
    private NodeType nodeType;
    private List<Location> locations;
}
