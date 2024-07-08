package manager.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ManagerServer extends Remote {
    void storeFileChunk(String fileName, int chunkNumber, byte[] chunkData) throws RemoteException;
    List<String> searchKeyword(String keyword) throws RemoteException;
}