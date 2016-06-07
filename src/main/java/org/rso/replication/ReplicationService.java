package org.rso.replication;

import javaslang.control.Try;
import lombok.NonNull;
import org.rso.utils.Location;
import org.rso.utils.NodeInfo;

import java.util.List;


public interface ReplicationService {
    List<Location> getTopLocations(final int topLocations);
    Try<Void> replicateLocation(@NonNull final Location location, @NonNull final NodeInfo nodeInfo);
}
