package monitor;


import loadbalancer.interfaces.LoadBalancerServer;
import monitor.impl.MonitorServerImpl;
import monitor.interfaces.MonitorServer;

import registry.RegistryUtils;
import registry.interfaces.RegistryServer;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static registry.RegistryUtils.IP_REGISTRY;
import static registry.RegistryUtils.PORT_REGISTRY;

public class Monitor {

    private static String ip;
    private static int port = 1099;

    public static void main(String[] args) {
        try {

            RegistryServer registryServer = (RegistryServer) RegistryUtils.lookupServer("RegistryServer",IP_REGISTRY, PORT_REGISTRY );

            LoadBalancerServer loadBalancerServer = (LoadBalancerServer) registryServer.lookupServer("LoadBalancerServer");

            ip = (InetAddress.getLocalHost().getHostAddress()).trim();
            System.out.println("IP MONITOR SERVER: " + ip);

            System.setProperty("java.rmi.server.hostname", ip);
            Registry registry =/* LocateRegistry.createRegistry(port);*/LocateRegistry.getRegistry(ip,port);

            MonitorServer monitorServer = new MonitorServerImpl(loadBalancerServer);
            registry.rebind("MonitorServer", monitorServer);

            registryServer.registry("MonitorServer", monitorServer);

            System.out.println("MonitorServer is running...");

            RegistryUtils.lock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
