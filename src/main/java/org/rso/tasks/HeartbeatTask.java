package org.rso.tasks;

import lombok.extern.java.Log;
import org.rso.service.NodeUtilService;
import org.rso.utils.AppProperty;
import org.rso.utils.NodeType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

@Log
@Component
public class HeartbeatTask {

    @Resource
    private NodeUtilService nodeUtilService;

    private AppProperty appProperty = AppProperty.getInstance();

    /* Heartbeat procedure task - executed by current coordinator */
    @Scheduled(fixedRateString = "${delay.heartbeat}")
    public void heartbeatTask() {

        Optional.ofNullable(appProperty.getSelfNode())
                .ifPresent(selfNodeInfo -> {
                    if(selfNodeInfo.getNodeType() == NodeType.INTERNAL_COORDINATOR) {
                        nodeUtilService.doHeartBeat();
                    }
                });
    }
}
