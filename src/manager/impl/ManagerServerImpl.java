package manager.impl;

import data.interfaces.DataServer;
import loadbalancer.interfaces.LoadBalancerServer;
import manager.interfaces.ManagerServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ManagerServerImpl extends UnicastRemoteObject implements ManagerServer, Runnable {

    private final int REPLICATION_FACTOR = 1;
    private final LoadBalancerServer loadBalancerServer;

    public ManagerServerImpl(LoadBalancerServer loadBalancerServer) throws RemoteException {
        this.loadBalancerServer = loadBalancerServer;
        new Thread(this).start();
    }

    @Override
    public void storeFileChunk(String fileName, int chunkNumber, byte[] chunkData) throws RemoteException {
        int successfulReplications = 0;
        try {
            for (int i = 0; i < REPLICATION_FACTOR; i++) {
                String node = loadBalancerServer.chooseDataServerForStorage();
                System.out.println("Nó selecionado: " + node);

                DataServer dataNode = loadBalancerServer.getDataServer(node);
                System.out.println(dataNode);
                try {
                    dataNode.storeChunk(fileName, chunkNumber, chunkData);
                    successfulReplications++;
                    System.out.println("Chunk armazenado com sucesso no nó: " + node);

                    if (successfulReplications >= REPLICATION_FACTOR) {
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao armazenar o chunk no nó " + node + ": " + e.getMessage());
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
        return new ArrayList<>();
    }

    @Override
    public void run() {

    }
}