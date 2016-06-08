package org.rso.network.services;


import javaslang.control.Try;
import lombok.NonNull;
import org.rso.network.dto.NetworkStatusDto;
import org.rso.network.dto.NodeStatusDto;
import org.rso.network.NodeInfo;

public interface NodeNetworkService {
    Try<NetworkStatusDto> getNetworkStatus();
    Try<NetworkStatusDto> getNetworkStatus(@NonNull final NodeInfo nodeInfo);
    Try<NodeStatusDto> getHeartbeat(@NonNull final NodeInfo nodeInfo);
    Try<Void> setNetworkStatus(@NonNull final NodeInfo nodeInfo, @NonNull final NetworkStatusDto networkStatusDto);

}
