package data;

import data.impl.DataServerImpl;
import data.interfaces.DataServer;
import monitor.interfaces.MonitorServer;
import registry.RegistryUtils;
import registry.interfaces.RegistryServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static registry.RegistryUtils.IP_REGISTRY;
import static registry.RegistryUtils.PORT_REGISTRY;

public class Data {

    private static String ip;
    private static int port = 1099;

    public static void main(String[] args) {
        try {

            RegistryServer registryServer = (RegistryServer) RegistryUtils.lookupServer("RegistryServer", IP_REGISTRY, PORT_REGISTRY);

            MonitorServer monitorServer = (MonitorServer) registryServer.lookupServer("MonitorServer");

            String dataServerName = monitorServer.getUniqueServerName();

            ip = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())[0].getHostAddress();
            System.out.println("IP DATA SERVER: " + ip);
            System.setProperty("java.rmi.server.hostname", ip);
            Registry registry = /*LocateRegistry.createRegistry(port);*/LocateRegistry.getRegistry(ip, port);

            DataServer dataServer = new DataServerImpl(dataServerName, monitorServer);
            registry.rebind(dataServerName, dataServer);

            registryServer.registry(dataServerName, dataServer);

            System.out.println("DataServer " + dataServerName + " is running...");
            RegistryUtils.lock();
        } catch (RemoteException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
