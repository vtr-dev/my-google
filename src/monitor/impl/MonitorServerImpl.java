package monitor.impl;

import data.interfaces.DataServer;
import loadbalancer.interfaces.LoadBalancerServer;
import monitor.interfaces.MonitorServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class MonitorServerImpl extends UnicastRemoteObject implements MonitorServer, Runnable {

    private static final long TIMEOUT = 10000; // 10 segundos de timeout
    private Map<String, DataServer> dataServers;
    private Map<String, Long> lastHeartbeat;
    private LoadBalancerServer loadBalancerServer;


    public MonitorServerImpl(LoadBalancerServer loadBalancerServer) throws RemoteException {
        dataServers = new HashMap<>();
        lastHeartbeat = new HashMap<>();
        this.loadBalancerServer = loadBalancerServer;
        new Thread(this).start();
    }

    @Override
    public void registerDataServer(String dataServerName, DataServer dataServer) throws RemoteException {
        dataServers.put(dataServerName, dataServer);
        lastHeartbeat.put(dataServerName, System.currentTimeMillis());
        loadBalancerServer.registerDataServer(dataServerName,dataServer);
    }

    @Override
    public void notifyOnline(String dataServerName) throws RemoteException {
        lastHeartbeat.put(dataServerName, System.currentTimeMillis());
    }

    @Override
    public String getUniqueServerName() throws RemoteException {
        return "DataServer-" + UUID.randomUUID().toString();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(5000); // Verificar status a cada 5 segundos
                long currentTime = System.currentTimeMillis();
                lastHeartbeat.entrySet().removeIf(entry -> {
                    if (currentTime - entry.getValue() > TIMEOUT) {
                        System.out.println("DataServer " + entry.getKey() + " is offline and has been removed.");
                        dataServers.remove(entry.getKey());
                        try {
                            loadBalancerServer.updateDataServer(entry.getKey(),dataServers.get(entry.getKey()),false);
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                        return true;
                    }
                    return false;
                });
            } catch (InterruptedException e ) {
                throw new RuntimeException(e);
            }
        }
    }

}
