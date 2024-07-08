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
        monitorServer.registerDataServer(name,this);
        new Thread(this).start();
    }

    @Override
    public void storeChunk(String fileName, int chunkNumber, byte[] chunkData) throws RemoteException {
        saveChunkToFile(fileName, chunkNumber, chunkData);
    }

    @Override
    public List<String> searchKeyword(String keyword) throws RemoteException {
        List<String> results = new ArrayList<>();
        AhoCorasick ahoCorasick = new AhoCorasick();
        ahoCorasick.addKeyword(keyword);
        ahoCorasick.prepare();

        File dir = new File(PERSISTENCE_DIR, name);
        if (dir.exists()) {
            for (File fileDir : dir.listFiles()) {
                String fileName = fileDir.getName();
                for (File chunkFile : fileDir.listFiles()) {
                    try (FileInputStream fis = new FileInputStream(chunkFile)) {
                        byte[] chunkData = new byte[(int) chunkFile.length()];
                        fis.read(chunkData);
                        String chunkText = new String(chunkData);

                        List<Integer> positions = ahoCorasick.search(chunkText);
                        if (!positions.isEmpty()) {
                            results.add("Keyword found in file: " + fileName + " chunk: " + chunkFile.getName());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return results;
    }

    @Override
    public int getChunkCount() throws RemoteException {
        int chunkCount = 0;
        File dir = new File(PERSISTENCE_DIR, name);
        if (dir.exists()) {
            for (File fileDir : dir.listFiles()) {
                chunkCount += fileDir.listFiles().length;
            }
        }
        return chunkCount;
    }

    @Override
    public void run() {
        while (true) {
            try {
                keepAlive();
                Thread.sleep(5000); // Enviar keepalive a cada 5 segundos
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void saveChunkToFile(String fileName, int chunkNumber, byte[] chunkData) {
        File dir = new File(PERSISTENCE_DIR, name + File.separator + fileName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File chunkFile = new File(dir, "chunk_" + chunkNumber);
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

