package data.impl;

import data.interfaces.DataServer;
import monitor.interfaces.MonitorServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class DataServerImpl extends UnicastRemoteObject implements DataServer, Runnable {

    private static final String PERSISTENCE_DIR = "src/data/datachunks";
    private MonitorServer monitorServer;
    private String name;

    public DataServerImpl(String nome, MonitorServer monitorServer) throws RemoteException {
        this.name = nome;
        this.monitorServer = monitorServer;
        monitorServer.registerDataServer(name, this);
        new Thread(this).start();
    }

    @Override
    public void storeChunk(String fileName, int chunkNumber, byte[] chunkData) throws RemoteException {
        saveChunkToFile(fileName, chunkNumber, chunkData);
    }

    public List<String> searchKeyword(String keyword, String fileName) throws RemoteException {
        List<String> results = new ArrayList<>();

        File dir = new File(PERSISTENCE_DIR, name);
        if (!dir.exists()) {
            results.add("\nDirectory not found: " + dir.getPath());
            return results;
        }

        File file = new File(dir, fileName);
        if (!file.exists() || file.isDirectory()) {
            results.add("\nFile not found or is a directory: " + file.getPath());
            return results;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] chunkData = new byte[(int) file.length()];
            fis.read(chunkData);
            String chunkText = new String(chunkData);

            List<Integer> positions = search(keyword, chunkText);
            for (int position : positions) {
                int start = Math.max(0, position - 30);
                int end = Math.min(chunkText.length(), position + keyword.length() + 30);
                String snippet = chunkText.substring(start, end);
                results.add("\n--> Keyword found!" + "\nFile: " + fileName + "\nDescription: " + snippet);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return results;
    }

    private List<Integer> search(String keyword, String text) {
        List<Integer> positions = new ArrayList<>();
        int index = 0;
        while ((index = text.indexOf(keyword, index)) != -1) {
            positions.add(index);
            index += keyword.length();
        }
        return positions;
    }

    @Override
    public void run() {
        while (true) {
            try {
                keepAlive();
                Thread.sleep(5000); // Envia keepalive a cada 5 segundos
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void saveChunkToFile(String fileName, int chunkNumber, byte[] chunkData) {
        File dir = new File(PERSISTENCE_DIR, name);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File chunkFile = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
            fos.write(chunkData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void keepAlive() throws RemoteException {
        monitorServer.notifyOnline(name);
    }
}

