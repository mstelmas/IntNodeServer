package org.rso.network.controllers;

import lombok.extern.java.Log;
import org.rso.DtoConverters;
import org.rso.network.dto.NodeStatusDto;
import org.rso.utils.AppProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;

@Log
@RestController
@RequestMapping("utils")
public class HeartbeatController {

    @Value("${log.tag.heartbeat}")
    private String heartbeatTag;

    private final AppProperty appProperty = AppProperty.getInstance();

    @RequestMapping(value = "/heartbeat")
    public NodeStatusDto heartbeatProtocol(final HttpServletRequest httpServletRequest){

        log.info(String.format("%s: Received heartbeat request from: %s", heartbeatTag, httpServletRequest.getRemoteAddr()));

        appProperty.setLastCoordinatorPresence(new Date());

        return DtoConverters.nodeInfoToNodeStatusDto.apply(
                Optional.ofNullable(appProperty.getSelfNode())
                    .orElseThrow(() -> new RuntimeException("Critical state in network found. Self node not initialized"))
        );
    }
}
