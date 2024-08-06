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
import java.util.concurrent.TimeUnit;

public class ManagerServerImpl extends UnicastRemoteObject implements ManagerServer {

    private final int REPLICATION_FACTOR = 2;
    private final LoadBalancerServer loadBalancerServer;
    private Set<String> files;

    public ManagerServerImpl(LoadBalancerServer loadBalancerServer) throws RemoteException {
        this.loadBalancerServer = loadBalancerServer;
        files = new HashSet<>();
    }

    @Override
    public void storeFileChunk(String fileName, int chunkNumber, byte[] chunkData) throws RemoteException {
        final int maxAttempts = 3;
        ExecutorService executor = Executors.newFixedThreadPool(REPLICATION_FACTOR);
        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < REPLICATION_FACTOR; i++) {
                futures.add(executor.submit(() -> {
                    int attempt = 0;
                    while (attempt < maxAttempts) {
                        String dataNodeName;
                        try {
                            dataNodeName = loadBalancerServer.chooseDataServerForStorage();
                            if (dataNodeName == null || dataNodeName.isEmpty()) {
                                System.err.println("No data node available.");
                                return false;
                            }
                            DataServer dataNode = loadBalancerServer.getDataServer(dataNodeName);
                            if (dataNode == null) {
                                System.err.println("Data node not found: " + dataNodeName);
                                return false;
                            }
                            files.add(fileName + chunkNumber);
                            dataNode.storeChunk(fileName + chunkNumber, 1, chunkData);
                            loadBalancerServer.registerFileInDataServer(fileName + chunkNumber, dataNodeName);
                            System.out.println("Chunk successfully stored on node: " + dataNodeName);
                            return true;
                        } catch (Exception e) {
                            System.err.println("Error storing chunk (attempt " + (attempt + 1) + "): " + e.getMessage());
                        }
                        attempt++;
                    }
                    return false;
                }));
            }

            long successfulReplications = futures.stream().filter(future -> {
                try {
                    return future.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error retrieving task result: " + e.getMessage());
                    return false;
                }
            }).count();

            if (successfulReplications >= REPLICATION_FACTOR) {
                System.out.println("Chunk successfully stored on all nodes.");
            } else {
                System.err.println("Failed to store the chunk on all nodes.");
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }


    @Override
    public List<String> searchKeyword(String keyword) throws RemoteException {
        List<String> results = new ArrayList<>();
        if (files.isEmpty()){
            return new ArrayList<>();
        }
        ExecutorService executorService = Executors.newFixedThreadPool(files.size());
        List<Future<List<String>>> futures = new ArrayList<>();

        try {
            for (String fileName : files) {
                Callable<List<String>> task = () -> {
                    List<String> searchResults = new ArrayList<>();
                    boolean success = false;

                    while (!success) {
                        try {
                            String dataServerName = loadBalancerServer.chooseDataServersForSearch(fileName);
                            if (dataServerName == null || dataServerName.isEmpty()) {
                               break;
                            }
                            System.out.println("Starting search on data server: " + dataServerName);
                            DataServer dataNode = loadBalancerServer.getDataServer(dataServerName);
                            searchResults = dataNode.searchKeyword(keyword, fileName);
                            System.out.println("Search completed.");
                            success = true;
                        } catch (RemoteException e) {
                            System.err.println("Error searching on node. Retrying with another node. Error: " + e.getMessage());
                        } catch (Exception e) {
                            throw new RemoteException("Unexpected error occurred while searching keyword.", e);
                        }
                    }
                    return searchResults;
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