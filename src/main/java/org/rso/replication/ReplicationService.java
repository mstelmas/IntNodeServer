package org.rso.replication;

import javaslang.control.Try;
import lombok.NonNull;
import org.rso.storage.types.Location;
import org.rso.network.NodeInfo;

import java.util.List;


public interface ReplicationService {
    List<Location> getTopLocations(final int topLocations);
    Try<Void> replicateLocation(@NonNull final Location location, @NonNull final NodeInfo nodeInfo);
    Try<Void> replicateLocation(@NonNull final Location location, @NonNull final NodeInfo sourceNode, @NonNull final NodeInfo destNode);

    Try<NodeInfo> replicateOnNewNode(@NonNull final NodeInfo createdNodeInfo);
}
