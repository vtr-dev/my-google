package monitor.interfaces;

import data.interfaces.DataServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MonitorServer extends Remote {
    void registerDataServer(String dataServerName, DataServer dataServer) throws RemoteException;
    void notifyOnline(String dataServerName) throws RemoteException;
    String getUniqueServerName() throws RemoteException;
}