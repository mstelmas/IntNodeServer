package org.rso.controllers;

import lombok.extern.java.Log;
import org.rso.dto.DtoConverters;
import org.rso.dto.NodeStatusDto;
import org.rso.service.InternalNodeUtilService;
import org.rso.utils.AppProperty;
import org.rso.utils.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Log
@RestController
@RequestMapping("utils")
public class UtilController {

    /* TODO:
            1. Integrity checks to ensure we got a heartbeat from current coordinator
            2. Yoda time
            3. Remove ugly singleton
    */

    @Autowired
    private InternalNodeUtilService utilService;

    @Value("${log.tag.election}")
    private String electionTag;

    @Value("${log.tag.heartbeat}")
    private String heartbeatTag;

    private final AppProperty appProperty = AppProperty.getInstance();

    @RequestMapping(value = "/heartbeat")
    public NodeStatusDto heartbeatProtocol(final HttpServletRequest httpServletRequest){

        log.info(String.format("%s: Received heartbeat request from: %s", heartbeatTag, httpServletRequest.getRemoteAddr()));

        appProperty.setLastCoordinatorPresence(new Date());

        return DtoConverters.nodeIntoToNodeStatusDto.apply(appProperty.getSelfNode());
    }

    @RequestMapping(value = "/election", method = RequestMethod.POST)
    public NodeStatusDto electionAction(@RequestBody final NodeStatusDto node) {

        log.info(String.format("%s: Received election request from: %s", electionTag, node.toString()));

        final NodeInfo selfNode = appProperty.getSelfNode();
        final int selfNodeId = selfNode.getNodeId();

        if(selfNodeId > node.getNodeId()) {
            utilService.doElection();
        }

        return DtoConverters.nodeIntoToNodeStatusDto.apply(selfNode);
    }
}
