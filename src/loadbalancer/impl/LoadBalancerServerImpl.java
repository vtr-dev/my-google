package loadbalancer.impl;

import data.interfaces.DataServer;
import loadbalancer.interfaces.LoadBalancerServer;
import registry.RegistryUtils;
import registry.interfaces.RegistryServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static registry.RegistryUtils.IP_REGISTRY;
import static registry.RegistryUtils.PORT_REGISTRY;

public class LoadBalancerServerImpl extends UnicastRemoteObject implements LoadBalancerServer {

    private Map<String, DataServer> dataServers;
    private Map<String, List<String>> fileNameStoraged;
    private Random random;

    public LoadBalancerServerImpl() throws RemoteException {
        dataServers = new HashMap<>();
        fileNameStoraged = new HashMap<>();
        this.random = new Random();
    }

    @Override
    public String chooseDataServerForStorage() throws RemoteException {
        List<String> activeServers = dataServers.keySet().stream().toList();
        return activeServers.get(random.nextInt(activeServers.size()));
    }

    @Override
    public String chooseDataServersForSearch(String fileName) throws RemoteException {
        return fileNameStoraged.get(fileName).getFirst();
    }

    public void registerFileInDataServer(String fileName, String dataServerName) {
        fileNameStoraged.computeIfAbsent(fileName, k -> new ArrayList<>()).add(dataServerName);
    }

    @Override
    public void registerDataServer(String dataServerName, DataServer dataServer) throws RemoteException {
        dataServers.put(dataServerName, dataServer);
    }

    @Override
    public DataServer getDataServer(String dataServerName) throws RemoteException {
        RegistryServer registryServer = (RegistryServer) RegistryUtils.lookupServer("RegistryServer",IP_REGISTRY, PORT_REGISTRY );
        return (DataServer) registryServer.lookupServer(dataServerName);
    }

    public void updateDataServer(String dataServerName, DataServer dataServer, boolean isOnline) {
        if (isOnline) {
            dataServers.putIfAbsent(dataServerName, dataServer);
        } else {
            dataServers.remove(dataServerName);
            fileNameStoraged.values().forEach(sublist -> sublist.removeIf(s -> s.equals(dataServerName)));
        }
    }

}