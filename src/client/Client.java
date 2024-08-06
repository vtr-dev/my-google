package client;

import manager.interfaces.ManagerServer;
import registry.RegistryUtils;
import registry.interfaces.RegistryServer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static registry.RegistryUtils.IP_REGISTRY;
import static registry.RegistryUtils.PORT_REGISTRY;

public class Client {

    private static final int CHUNK_SIZE = 1024 * 1024 * 8; // Chunk size in bytes (4MB)
    private static final String filePath = "/home/aluno/Downloads/";
    private static final String fileName = "2021.json";
    private static final String keyword = "Serra da Capivara";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose an option:");
        System.out.println("1 - Upload file");
        System.out.println("2 - Perform search");
        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            RegistryServer registryServer = (RegistryServer) RegistryUtils.lookupServer("RegistryServer", IP_REGISTRY, PORT_REGISTRY);
            ManagerServer managerServer = (ManagerServer) registryServer.lookupServer("ManagerServer");

            switch (choice) {
                case 1:
                    long startUpload = System.currentTimeMillis();
                    File file = new File(filePath + fileName);
                    if (file.exists() && file.isFile()) {
                        upload(file, managerServer);
                        long endUpload = System.currentTimeMillis();
                        System.out.println("\nFile uploaded successfully. Time in ms: " + (endUpload - startUpload));
                    } else {
                        System.err.println("Error: File not found or is a directory.");
                    }
                    break;
                case 2:
                    System.out.println("\nStarting search...");
                    long startSearch = System.currentTimeMillis();
                    managerServer.searchKeyword(keyword).forEach(System.out::println);
                    long endSearch = System.currentTimeMillis();
                    System.out.println("\nSearch completed successfully. Time in ms: " + (endSearch - startSearch));
                    break;
                default:
                    System.out.println("\nInvalid option.");
                    break;
            }
        } catch (RemoteException e) {
            System.err.println("Remote communication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static void upload(File file, ManagerServer managerServer) {
        long fileSize = file.length();
        int numberOfChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);

        int numberOfThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfChunks; i++) {
            int chunkNumber = i;
            executor.submit(() -> {
                try {
                    byte[] chunkData = readChunk(file, chunkNumber);
                    managerServer.storeFileChunk(fileName, chunkNumber, chunkData);
                } catch (IOException e) {
                    System.err.println("Error reading file chunk: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Unexpected error while uploading chunk: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Error waiting for task completion: " + e.getMessage());
            executor.shutdownNow();
        }
    }

    private static byte[] readChunk(File file, int chunkNumber) throws IOException {
        long offset = (long) chunkNumber * CHUNK_SIZE;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(offset);
            byte[] chunkData = new byte[CHUNK_SIZE];
            int bytesRead = raf.read(chunkData);
            if (bytesRead < CHUNK_SIZE) {
                byte[] actualData = new byte[bytesRead];
                System.arraycopy(chunkData, 0, actualData, 0, bytesRead);
                return actualData;
            }
            return chunkData;
        }
    }
}
