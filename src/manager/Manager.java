package manager;

import loadbalancer.interfaces.LoadBalancerServer;
import manager.impl.ManagerServerImpl;
import manager.interfaces.ManagerServer;
import registry.RegistryUtils;
import registry.interfaces.RegistryServer;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static registry.RegistryUtils.IP_REGISTRY;
import static registry.RegistryUtils.PORT_REGISTRY;

public class Manager {

    private static String ip;
    private static int port = 1099;

    public static void main(String[] args) {
        try {

            RegistryServer registryServer = (RegistryServer) RegistryUtils.lookupServer("RegistryServer",IP_REGISTRY, PORT_REGISTRY );

            LoadBalancerServer loadBalancerServer = (LoadBalancerServer) registryServer.lookupServer("LoadBalancerServer");

            ip = (InetAddress.getLocalHost().getHostAddress()).trim();
            System.out.println("IP MANAGER SERVER: " + ip);
            System.setProperty("java.rmi.server.hostname", ip);
            Registry registry = /*LocateRegistry.createRegistry(port);*/LocateRegistry.getRegistry(ip,port);

            ManagerServer managerServer = new ManagerServerImpl(loadBalancerServer);
            registry.rebind("ManagerServer", managerServer);

            registryServer.registry("ManagerServer", managerServer);

            System.out.println("Manager is running...");

            RegistryUtils.lock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
