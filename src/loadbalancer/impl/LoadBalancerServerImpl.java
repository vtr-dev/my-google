package loadbalancer.impl;

import data.interfaces.DataServer;
import loadbalancer.interfaces.LoadBalancerServer;
import manager.impl.ChunkInfo;
import registry.RegistryUtils;
import registry.interfaces.RegistryServer;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static registry.RegistryUtils.IP_REGISTRY;
import static registry.RegistryUtils.PORT_REGISTRY;

public class LoadBalancerServerImpl extends UnicastRemoteObject implements LoadBalancerServer {

    private static final String PERSISTENCE_FILE = "src/loadbalancer/persistence/PersistenceFile";
    private Map<String, DataServer> dataServers;
    private Map<String, List<ChunkInfo>> fileChunks;
    private Random random;

    public LoadBalancerServerImpl() throws RemoteException {
        dataServers = new HashMap<>();
        fileChunks = new HashMap<>();
        this.random = new Random();
      //  loadState();
    }

    @Override
    public String chooseDataServerForStorage() throws RemoteException {
        List<String> activeServers = dataServers.keySet().stream().toList();
        return activeServers.get(random.nextInt(activeServers.size()));
    }

    @Override
    public List<String> chooseDataServersForSearch() throws RemoteException {
        return dataServers.keySet().stream().toList();
    }

    public void registerFileChunks(String fileId, ChunkInfo chunkInfo) {
        fileChunks.computeIfAbsent(fileId, k -> new ArrayList<>()).add(chunkInfo);
        saveState();
    }

    @Override
    public void registerDataServer(String dataServerName, DataServer dataServer) throws RemoteException {
        dataServers.put(dataServerName, dataServer);
        saveState();
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
        }
        saveState();
    }

    private void saveState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PERSISTENCE_FILE))) {
            oos.writeObject(dataServers);
            oos.writeObject(fileChunks);
            System.out.println("Estado salvo com sucesso no arquivo: " + PERSISTENCE_FILE);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar estado no arquivo: " + PERSISTENCE_FILE, e);
        }
    }

    private void loadState() {
        try (InputStream is = new FileInputStream(PERSISTENCE_FILE);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            dataServers = (Map<String, DataServer>) ois.readObject();
            fileChunks = (Map<String, List<ChunkInfo>>) ois.readObject();
            System.out.println("Estado carregado com sucesso.");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erro ao carregar o estado do balanceador de carga.", e);
        }
    }

    public Map<String, DataServer> getDataServers() {
        return dataServers;
    }

    public Map<String, List<ChunkInfo>> getFileChunks() {
        return fileChunks;
    }
}