package org.rso.network.services;


import javaslang.control.Try;
import lombok.extern.java.Log;
import org.rso.DtoConverters;
import org.rso.configuration.services.AppProperty;
import org.rso.network.dto.NetworkStatusDto;
import org.rso.network.dto.NodeStatusDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Log
@Service("nodeConnectorService")
public class InternalNodeConnectorService {

    @Value("${timeout.request.read}")
    private int readTimeout;

    @Value("${timeout.request.connect}")
    private int connectionTimeout;

    private final AppProperty appProperty = AppProperty.getInstance();

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String STATUS_URL = "http://{ip}:{port}/utils/status";
    private static final String NODES_URL = "http://{ip}:{port}/coord/nodes";
    private static final String DEFAULT_NODES_PORT = "8080";

    @PostConstruct
    public void initialize() {
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(readTimeout);
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(connectionTimeout);
    }

    /* TODO: Refactor */
    public void connectToNetwork(final String nodeIpAddress) {
        assert !StringUtils.isEmpty(nodeIpAddress);

        log.info(String.format("Trying to connect to network using node %s configuration", nodeIpAddress));

        /* Get network configuration from node */
        Try.run(() -> {
            final NetworkStatusDto networkStatusDto = restTemplate.getForObject(
                    STATUS_URL,
                    NetworkStatusDto.class,
                    nodeIpAddress,
                    DEFAULT_NODES_PORT
            );

            final NodeStatusDto coordinatorStatusDto = Optional.ofNullable(networkStatusDto.getCoordinator())
                    .orElseThrow(() -> new RuntimeException(String.format("Cannot retrieve network coordinator details from %s", nodeIpAddress)));

            log.info(String.format("Retrieved network configuration from %s.\nContacting network coordinator %s (id: %s)",
                    nodeIpAddress, coordinatorStatusDto.getNodeIPAddress(), coordinatorStatusDto.getNodeId()));

            /* Contact coordinator to get registered into network */

            final ResponseEntity<Void> registrationEntity = restTemplate.postForEntity(
                    NODES_URL,
                    null,
                    Void.class,
                    coordinatorStatusDto.getNodeIPAddress(),
                    DEFAULT_NODES_PORT
            );

            if(registrationEntity.getStatusCode() == HttpStatus.CREATED) {

                final String createdNodeLocation = registrationEntity.getHeaders().getLocation().toASCIIString();

                log.info(String.format("Registration successful: %s", createdNodeLocation));

                final NodeStatusDto createdNodeDto = restTemplate.getForObject(
                        createdNodeLocation,
                        NodeStatusDto.class
                );

                appProperty.setSelfNode(DtoConverters.nodeStatusDtoToNodeInfo.apply(createdNodeDto));

//                final NetworkStatusDto updatedNetworkStatusDto = restTemplate.getForObject(
//                        STATUS_URL,
//                        NetworkStatusDto.class,
//                        nodeIpAddress,
//                        DEFAULT_NODES_PORT
//                );

//                appProperty.setCoordinatorNode(DtoConverters.nodeStatusDtoToNodeInfo.apply(updatedNetworkStatusDto.getCoordinator()));
//                appProperty.setListOfAvaiableNodes(
//                        updatedNetworkStatusDto.getNodes().stream()
//                                .map(DtoConverters.nodeStatusDtoToNodeInfo)
//                                .collect(Collectors.toList())
//                );


            } else {
                log.info("Could not register new node :(");
                throw new RuntimeException("Could not register new node :(");
            }

        }).onFailure((e) -> { throw new RuntimeException(e.getMessage()); });
    }
}
