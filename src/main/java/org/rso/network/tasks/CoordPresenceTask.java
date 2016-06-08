package org.rso.network.tasks;

import lombok.extern.java.Log;
import org.rso.network.services.NodeUtilService;
import org.rso.configuration.services.AppProperty;
import org.rso.network.types.NodeType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

@Log
@Component
public class CoordPresenceTask {

    @Resource
    private NodeUtilService nodeUtilService;

    private AppProperty appProperty = AppProperty.getInstance();

    /* Coordinator presence verification task - executed by each internal node */
    @Scheduled(fixedRateString = "${delay.coordinator.check}")
    public void coordinatorPresenceVerificationTask() {

        Optional.ofNullable(appProperty.getSelfNode())
                .ifPresent(selfNodeInfo -> {
                    if(selfNodeInfo.getNodeType() == NodeType.INTERNAL) {
                        nodeUtilService.verifyCoordinatorPresence();
                    }
                });
    }
}
