package loadbalancer.interfaces;

import data.interfaces.DataServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LoadBalancerServer extends Remote {
    String chooseDataServerForStorage() throws RemoteException;
    public String chooseDataServersForSearch(String fileName) throws RemoteException;
    void updateDataServer(String dataServerName, DataServer dataServer, boolean isOnline) throws RemoteException;
    void registerFileInDataServer(String fileName, String dataServerName)throws RemoteException;
    void registerDataServer(String dataServerName, DataServer dataServer) throws RemoteException;
    DataServer getDataServer(String dataServerName)throws RemoteException;
}