package org.rso.network.services;


public interface NodeUtilService {
    void doHeartBeat();
    void doElection();
    void verifyCoordinatorPresence();
}
