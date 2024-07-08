package client;

import manager.interfaces.ManagerServer;
import registry.RegistryUtils;
import registry.interfaces.RegistryServer;

import java.io.File;
import java.io.FileInputStream;

import static registry.RegistryUtils.IP_REGISTRY;
import static registry.RegistryUtils.PORT_REGISTRY;


public class Client {

    private static final int CHUNK_SIZE = 1024 * 1024; // Tamanho do chunk em bytes (1MB)

    public static void main(String[] args) {


        String filePath = "/home/heloisasouza/SD/";
        String fileName = "a";

        try {

            RegistryServer registryServer = (RegistryServer) RegistryUtils.lookupServer("RegistryServer",IP_REGISTRY, PORT_REGISTRY );

            ManagerServer managerServer = (ManagerServer) registryServer.lookupServer("ManagerServer");

            // Ler o arquivo e enviá-lo em chunks
            File file = new File(filePath+fileName);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            int chunkNumber = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                // Copiar o buffer lido para um array do tamanho exato do chunk lido
                byte[] chunkData = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunkData, 0, bytesRead);

                // Enviar o chunk para o servidor
                managerServer.storeFileChunk(fileName, chunkNumber, chunkData);
                chunkNumber++;
            }

            fis.close();
            System.out.println("Arquivo enviado com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}