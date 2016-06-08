package org.rso.network.controllers;

import javaslang.control.Try;
import lombok.extern.java.Log;
import org.rso.DtoConverters;
import org.rso.network.dto.NetworkStatusDto;
import org.rso.network.dto.NodeStatusDto;
import org.rso.network.exceptions.NodeNotFoundException;
import org.rso.network.services.NodeNetworkService;
import org.rso.replication.ReplicationServiceImpl;
import org.rso.utils.AppProperty;
import org.rso.utils.NodeInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Log
@RestController
@RequestMapping("coord")
public class CoordinatorController {

    @Resource
    private NodeNetworkService nodeNetworkService;

    @Resource
    private ReplicationServiceImpl replicationServiceImpl;

    @Value("${replication.redundancy}")
    private int replicationReduntancy;

    private final AppProperty appProperty = AppProperty.getInstance();


    @RequestMapping(value = "/nodes", method = RequestMethod.GET)
    public List<NodeStatusDto> getAllNodes() {
        return appProperty.getAvailableNodes().stream()
                .map(DtoConverters.nodeInfoToNodeStatusDto)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/nodes/{nodeId}", method = RequestMethod.GET)
    public ResponseEntity<NodeStatusDto> getNodeById(@PathVariable(value="nodeId") final int nodeId) {
        final NodeStatusDto nodeStatusDto = Optional.ofNullable(appProperty.getNodeById(nodeId))
                .map(DtoConverters.nodeInfoToNodeStatusDto)
                .orElseThrow(() -> new NodeNotFoundException(String.format("Node with id = %s does not exist", nodeId)));

        return ResponseEntity.ok(nodeStatusDto);
    }


    @RequestMapping(value = "/nodes", method = RequestMethod.POST)
    public ResponseEntity<Void> registerNode(final HttpServletRequest httpServletRequest) {
        // allocate new ID, data structures for new node
        final NodeInfo allocatedNodeInfo = appProperty.allocateNewNode();

        final NodeInfo createdNodeInfo = NodeInfo.builder()
                .nodeId(allocatedNodeInfo.getNodeId())
                .nodeIPAddress(httpServletRequest.getRemoteAddr())
                .nodeType(allocatedNodeInfo.getNodeType())
                .build();

        // addEntry entry into appProperty

        appProperty.addAvaiableNode(createdNodeInfo);

        // TODO: inform other nodes about network update

        // TODO: do not block for network updates (this can take a looong time)



        // perform replication on a new node
        replicationServiceImpl.getTopLocations(replicationReduntancy)
                .forEach(location ->
                    Try.run(() -> replicationServiceImpl.replicateLocation(location, createdNodeInfo))
                            .onSuccess(e -> {
                                log.info(
                                        String.format("Successfully replicated data about location: %s on node: %d [%s]",
                                                location, createdNodeInfo.getNodeId(), createdNodeInfo.getNodeIPAddress())
                                );

                                // TODO: network update!

                                if(createdNodeInfo.getLocations() == null) {
                                    createdNodeInfo.setLocations(new ArrayList<>());
                                }

                                createdNodeInfo.getLocations().add(location);

//                                locationMap.addEntry(location, createdNodeInfo);
                            })
                );

// TODO: network update!
//        createdNodeInfo.setLocations(locationMap.getLocationsForNode(createdNodeInfo));

        final NetworkStatusDto updatedNetworkStatusDto = NetworkStatusDto.builder()
                .coordinator(DtoConverters.nodeInfoToNodeStatusDto.apply(appProperty.getCoordinatorNode()))
                .nodes(appProperty.getAvailableNodes().stream().map(DtoConverters.nodeInfoToNodeStatusDto).collect(toList()))
                .build();

        appProperty.getAvailableNodes().forEach(availableNodeInfo ->
                nodeNetworkService.setNetworkStatus(availableNodeInfo, updatedNetworkStatusDto)
                        .onFailure(ez ->
                                                /* A node suddenly stopped responding; we don't need to do anything here though
                                                   since it will be removed during the next Heartbeat check iteration anyway.
                                                 */
                                log.info(String.format("Node %s stopped responding during network status update. It should be removed in the next Heartbeat check",
                                        availableNodeInfo.getNodeIPAddress()))
                        )
        );



        // remmemeber to remove transfered locations from database info

        //TODO: remove location from selected nodes...

        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdNodeInfo.getNodeId())
                .toUri())
                .build();
    }
}
