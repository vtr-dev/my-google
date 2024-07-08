package registry.impl;

import registry.interfaces.RegistryServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RegistryServerImpl extends UnicastRemoteObject implements RegistryServer {

    private Map<String, Remote> server;

    public RegistryServerImpl() throws RemoteException {
        server = new HashMap<>();
    }

    @Override
    public void registry(String alias,Remote sever) throws RemoteException {
        server.put(alias, sever);
    }

    @Override
    public Remote lookupServer(String alias) throws RemoteException {
       return  server.get(alias);
    }

}
