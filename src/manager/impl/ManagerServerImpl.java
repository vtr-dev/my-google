package manager.impl;

import data.interfaces.DataServer;
import loadbalancer.interfaces.LoadBalancerServer;
import manager.interfaces.ManagerServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ManagerServerImpl extends UnicastRemoteObject implements ManagerServer {

    private final int REPLICATION_FACTOR = 1;
    private final LoadBalancerServer loadBalancerServer;
    private Set<String> files;

    public ManagerServerImpl(LoadBalancerServer loadBalancerServer) throws RemoteException {
        this.loadBalancerServer = loadBalancerServer;
        files = new HashSet<>();
    }

    @Override
    public void storeFileChunk(String fileName, int chunkNumber, byte[] chunkData) throws RemoteException {
        int successfulReplications = 0;
        files.add(fileName);
        try {
            for (int i = 0; i < REPLICATION_FACTOR; i++) {
                String dataNodeName = loadBalancerServer.chooseDataServerForStorage();
                System.out.println("Nó selecionado: " + dataNodeName);

                DataServer dataNode = loadBalancerServer.getDataServer(dataNodeName);
                System.out.println(dataNode);
                try {
                    dataNode.storeChunk(fileName, chunkNumber, chunkData);
                    successfulReplications++;
                    loadBalancerServer.registerFileInDataServer(fileName, dataNodeName);
                    System.out.println("Chunk armazenado com sucesso no nó: " + dataNodeName);

                    if (successfulReplications >= REPLICATION_FACTOR) {
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao armazenar o chunk no nó " + dataNodeName + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (successfulReplications >= REPLICATION_FACTOR) {
                System.out.println("Chunk armazenado com sucesso em todos os nós.");
            } else {
                System.err.println("Erro: Não foi possível armazenar o chunk em nós suficientes.");
            }
        } catch (Exception e) {
            System.err.println("Erro ao armazenar o chunk: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public List<String> searchKeyword(String keyword) throws RemoteException {
        List<String> results = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(files.size());
        List<Future<List<String>>> futures = new ArrayList<>();

        try {
            for (String fileName : files) {
                Callable<List<String>> task = () -> {
                    String dataServerName = loadBalancerServer.chooseDataServersForSearch(fileName);
                    DataServer dataNode = loadBalancerServer.getDataServer(dataServerName);
                    return dataNode.searchKeyword(keyword, fileName);
                };
                futures.add(executorService.submit(task));
            }

            for (Future<List<String>> future : futures) {
                try {
                    results.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RemoteException("Error occurred while searching keyword.", e);
                }
            }
        } finally {
            executorService.shutdown();
        }

        return results;
    }

}