package org.rso.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.rso.utils.Location;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeStatusDto {
    private int nodeId;
    private String nodeIPAddress;
    private String nodeType;
    private List<Location> locations;
}
