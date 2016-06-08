package org.rso.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NetworkStatusDto {
    private NodeStatusDto coordinator;
    private List<NodeStatusDto> nodes;
}
