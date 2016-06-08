package org.rso;

import org.rso.storage.dto.GraduateDto;
import org.rso.storage.dto.UniversityDto;
import org.rso.storage.entities.Graduate;
import org.rso.storage.entities.University;
import org.rso.network.dto.NetworkStatusDto;
import org.rso.network.dto.NodeStatusDto;
import org.rso.utils.NetworkStatus;
import org.rso.utils.NodeInfo;
import org.rso.utils.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class DtoConverters {

    public static Function<NodeInfo, NodeStatusDto> nodeInfoToNodeStatusDto = nodeInfo ->
            NodeStatusDto.builder()
                    .nodeId(nodeInfo.getNodeId())
                    .nodeIPAddress(nodeInfo.getNodeIPAddress())
                    .nodeType(nodeInfo.getNodeType().name())
                    .locations(nodeInfo.getLocations())
                    .build();

    public static Function<NodeStatusDto, NodeInfo> nodeStatusDtoToNodeInfo  = nodeStatusDto ->
            NodeInfo.builder()
                    .nodeId(nodeStatusDto.getNodeId())
                    .nodeIPAddress(nodeStatusDto.getNodeIPAddress())
                    .nodeType(NodeType.valueOf(nodeStatusDto.getNodeType()))
                    .locations(nodeStatusDto.getLocations())
                    .build();

    public static Function<GraduateDto, Graduate> graduateDtoToEntity = graduateDto ->
            Graduate.builder()
                    .name(graduateDto.getName())
                    .surname(graduateDto.getSurname())
                    .comeFrom(graduateDto.getComeFrom())
                    .locationFrom(graduateDto.getLocationFrom())
                    .fieldOfStudyList(graduateDto.getFieldOfStudies())
                    .build();

    public static Function<UniversityDto, University> universityDtoToEntity = universityDto ->
            University.builder()
                    .name(universityDto.getName())
                    .location(universityDto.getLocation())
                    .universityType(universityDto.getUniversityType())
                    .yearOfFundation(universityDto.getYearOfFundation())
                    .graduates(new ArrayList<>())
                    .build();

    public static Function<University, UniversityDto> universityEntityToDto = entity ->
            UniversityDto.builder()
                    .name(entity.getName())
                    .yearOfFundation(entity.getYearOfFundation())
                    .location(entity.getLocation())
                    .universityType(entity.getUniversityType())
                    .value(Optional.ofNullable(entity.getGraduates()).map(List::size).orElse(0))
                    .build();

    public static Function<NetworkStatus, NetworkStatusDto> networkStatusEntityToDto = entity ->
            NetworkStatusDto.builder()
                    .coordinator(DtoConverters.nodeInfoToNodeStatusDto.apply(entity.getCoordinator()))
                    .nodes(entity.getNodes().stream().map(DtoConverters.nodeInfoToNodeStatusDto).collect(toList()))
                    .build();

}
