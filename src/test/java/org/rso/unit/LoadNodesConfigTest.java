package org.rso.unit;

import com.google.common.collect.ImmutableList;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rso.configuration.services.NodesCfgService;
import org.rso.network.NodeInfo;
import org.rso.network.types.NodeType;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(locations = "/testNodesContext.xml")
public class LoadNodesConfigTest {

    @Resource
    protected NodesCfgService nodesCfgService;

    private final NodeInfo COORDINATOR_NODE = NodeInfo.builder()
            .nodeId(100)
            .nodeIPAddress("192.168.0.1")
            .nodeType(NodeType.INTERNAL_COORDINATOR)
            .build();

    private final NodeInfo TEST_NODE = NodeInfo.builder()
            .nodeId(200)
            .nodeIPAddress("192.168.0.2")
            .nodeType(NodeType.INTERNAL)
            .build();

    private final List<NodeInfo> TEST_NODES = new ImmutableList.Builder<NodeInfo>()
            .add(COORDINATOR_NODE)
            .add(TEST_NODE)
            .add(NodeInfo.builder().nodeId(300).nodeIPAddress("192.168.0.3").nodeType(NodeType.INTERNAL).build())
            .build();

    @Test
    @Ignore
    public void shouldLoadNodesCfgFileProperly() {
        final NodeInfo coordinatorNode = nodesCfgService.getCoordinatorNode();
        final NodeInfo nodeOfId200 = nodesCfgService.getNodeById(200);
        final NodeInfo selfNode = nodesCfgService.getSelfNode();
        final List<NodeInfo> allLoadedNodes = nodesCfgService.getAllNodes();


        assertThat(selfNode).isEqualToComparingFieldByField(COORDINATOR_NODE);
        assertThat(coordinatorNode).isEqualToComparingFieldByField(COORDINATOR_NODE);
        assertThat(nodeOfId200).isEqualToComparingFieldByField(TEST_NODE);
        assertThat(allLoadedNodes).hasSize(3).containsAll(TEST_NODES);
    }
}
