package loadbalancer.interfaces;

import data.interfaces.DataServer;
import manager.impl.ChunkInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface LoadBalancerServer extends Remote {
    String chooseDataServerForStorage() throws RemoteException;
    List<String> chooseDataServersForSearch() throws RemoteException;
    void updateDataServer(String dataServerName, DataServer dataServer, boolean isOnline) throws RemoteException;
    void registerFileChunks(String fileId, ChunkInfo chunkInfo)throws RemoteException;
    void registerDataServer(String dataServerName, DataServer dataServer) throws RemoteException;
    DataServer getDataServer(String dataServerName)throws RemoteException;
}