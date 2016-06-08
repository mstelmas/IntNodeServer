package org.rso.network.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import org.rso.DtoConverters;
import org.rso.network.dto.NetworkStatusDto;
import org.rso.utils.AppProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.toList;

@Log
@RestController
@RequestMapping("utils")
@Api(value = "/utils", description = "Endpoint for managing current network configuration")
public class StatusController {
    private final AppProperty appProperty = AppProperty.getInstance();

    @RequestMapping(value = "/status")
    @ApiOperation(httpMethod = "GET", value = "Retrieve current network configuration", response = NetworkStatusDto.class)
    public NetworkStatusDto getNetworkStatus() {
        return NetworkStatusDto.builder()
                .coordinator(DtoConverters.nodeInfoToNodeStatusDto.apply(appProperty.getCoordinatorNode()))
                .nodes(appProperty.getAvailableNodes().stream().map(DtoConverters.nodeInfoToNodeStatusDto).collect(toList()))
                .build();
    }

    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public ResponseEntity<Void> setNetworkStatus(@RequestBody final NetworkStatusDto networkStatusDto) {

        /* TODO:
              NetworkStatusDto validation.
              Maybe send only updates not entire network status table
         */

        appProperty.setCoordinatorNode(DtoConverters.nodeStatusDtoToNodeInfo.apply(networkStatusDto.getCoordinator()));
        appProperty.setListOfAvaiableNodes(networkStatusDto.getNodes().stream().map(DtoConverters.nodeStatusDtoToNodeInfo).collect(toList()));

        return ResponseEntity.noContent().build();
    }
}
