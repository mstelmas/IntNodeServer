package org.rso.controllers;

import lombok.extern.java.Log;
import org.rso.dto.DtoConverters;
import org.rso.exceptions.NodeNotFoundException;
import org.rso.network.dto.NodeStatusDto;
import org.rso.network.services.NodeUtilService;
import org.rso.utils.AppProperty;
import org.rso.utils.NodeInfo;
import org.rso.utils.NodeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Optional;

@Log
@RestController
@RequestMapping("utils")
public class ElectionController {

        /* TODO:
            1. Integrity checks to ensure we got a heartbeat from current coordinator
            2. Yoda time
            3. Remove ugly singleton
    */


    @Resource(name = "internalNodeUtilService")
    private NodeUtilService nodeUtilService;

    @Value("${log.tag.election}")
    private String electionTag;

    private final AppProperty appProperty = AppProperty.getInstance();

    @RequestMapping(value = "/election", method = RequestMethod.POST)
    public NodeStatusDto electionAction(@RequestBody final NodeStatusDto node) {

        log.info(String.format("%s: Received election request from: %s", electionTag, node.toString()));

        final NodeInfo selfNode = appProperty.getSelfNode();
        final int selfNodeId = selfNode.getNodeId();

        if(selfNodeId > node.getNodeId()) {
            nodeUtilService.doElection();
        }

        return DtoConverters.nodeInfoToNodeStatusDto.apply(selfNode);
    }

    @RequestMapping(value = "/coordinator", method = RequestMethod.GET)
    public NodeStatusDto getCurrentCoordinator() {
        return DtoConverters.nodeInfoToNodeStatusDto.apply(appProperty.getCoordinatorNode());
    }

    @RequestMapping(value = "/coordinator", method = RequestMethod.PUT)
    public ResponseEntity<Void> setNewCoordinator(@RequestBody final NodeStatusDto coordinator) {

        /* TODO: safety checks of input coordinator DTO */

        if(coordinator.getNodeId() == appProperty.getCoordinatorNode().getNodeId()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        final NodeInfo coordinatorNode = Optional.ofNullable(appProperty.getNodeById(coordinator.getNodeId()))
                .orElseThrow(() -> new NodeNotFoundException(String.format("Node with id = %s does not exist", coordinator.getNodeId())));


        final NodeInfo newCoordinatorNode = NodeInfo.builder()
                .nodeId(coordinator.getNodeId())
                .nodeIPAddress(coordinator.getNodeIPAddress())
                .nodeType(NodeType.INTERNAL_COORDINATOR)
                .build();

        log.info(String.format("%s: Setting new coordinator: %s", electionTag, newCoordinatorNode));

        appProperty.removeNode(coordinator.getNodeId());

        appProperty.setCoordinatorNode(newCoordinatorNode);

        return ResponseEntity.noContent().build();
    }
}
