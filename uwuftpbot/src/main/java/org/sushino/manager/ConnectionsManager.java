package org.sushino.manager;

import java.util.*;
import org.sushino.wrapper.FtpWrapper;

public class ConnectionsManager {

    private static ConnectionsManager manager = null;
    private final Map<Integer, FtpWrapper> syncMap = Collections.synchronizedMap(new Hashtable<>());

    private ConnectionsManager() {
        //singleton
    }

    public static ConnectionsManager getManager() {
        manager = (manager == null) ? new ConnectionsManager() : manager;
        return manager;
    }

    public void registerConnection(Integer userID, FtpWrapper newConnection) {
        synchronized (syncMap) {
            syncMap.put(userID, newConnection);
            System.out.println("Connection registered. Params[userID=" + userID + ",newConnection[" + newConnection.toString() + "]");
        }
    }

    public void unregisterConnection(Integer userID) {
        synchronized (syncMap) {
            syncMap.remove(userID);
            System.out.println("Connection removed. Params[userID=" + userID + "]");
        }
    }

    public FtpWrapper getConnection(Integer userID) {
        FtpWrapper tFtp = null;

        try {
            synchronized (syncMap) {
                tFtp = syncMap.get(userID);
            }
        } catch (Exception ignored) {
        }

        return tFtp;
    }



}
