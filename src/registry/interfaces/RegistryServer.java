package registry.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistryServer  extends Remote {

    void registry(String alias, Remote server) throws RemoteException;
    Remote lookupServer(String alias) throws RemoteException;
}
