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
import java.util.concurrent.atomic.AtomicInteger;

import static registry.RegistryUtils.IP_REGISTRY;
import static registry.RegistryUtils.PORT_REGISTRY;

public class LoadBalancerServerImpl extends UnicastRemoteObject implements LoadBalancerServer {

    private final AtomicInteger currentIndex = new AtomicInteger(0);
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
        if (!activeServers.isEmpty()) {
            int index = currentIndex.getAndUpdate(i -> (i + 1) % activeServers.size());
            return activeServers.get(index);
        }
        return null;
    }

    @Override
    public String chooseDataServersForSearch(String fileName) throws RemoteException {
        List<String> list = fileNameStoraged.get(fileName);
        return list.isEmpty() ? "" : fileNameStoraged.get(fileName).get(random.nextInt(list.size()));
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
        RegistryServer registryServer = (RegistryServer) RegistryUtils.lookupServer("RegistryServer", IP_REGISTRY, PORT_REGISTRY);
        return (DataServer) registryServer.lookupServer(dataServerName);
    }

    public void updateDataServer(String dataServerName, DataServer dataServer, boolean isOnline) {
        if (isOnline) {
            dataServers.putIfAbsent(dataServerName, dataServer);
            System.out.println("\n Active servers " + dataServerName);
        } else {
            dataServers.remove(dataServerName);
            fileNameStoraged.values().forEach(sublist -> sublist.removeIf(s -> s.equals(dataServerName)));
            System.out.println("\n Offline servers " + dataServerName);

        }
    }

}