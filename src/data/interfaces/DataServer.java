package data.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface  DataServer extends Remote {
    void storeChunk(String fileName, int chunkNumber, byte[] chunkData) throws RemoteException;
    List<String> searchKeyword(String keyword) throws RemoteException;
    int getChunkCount() throws RemoteException;
}