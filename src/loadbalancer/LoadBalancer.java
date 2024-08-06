package loadbalancer;

import loadbalancer.impl.LoadBalancerServerImpl;
import loadbalancer.interfaces.LoadBalancerServer;
import registry.RegistryUtils;
import registry.interfaces.RegistryServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static registry.RegistryUtils.IP_REGISTRY;
import static registry.RegistryUtils.PORT_REGISTRY;


public class LoadBalancer {

    private static String ip;
    private static int port = 1099;

    public static void main(String[] args) {
        try {

            RegistryServer registryServer = (RegistryServer) RegistryUtils.lookupServer("RegistryServer",IP_REGISTRY, PORT_REGISTRY );

            ip = "192.168.40.122";//InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())[0].getHostAddress();
            System.out.println("IP LOADBALANCER SERVER: " + ip);
            System.setProperty("java.rmi.server.hostname", ip);
            Registry registry =/*LocateRegistry.createRegistry(port);//*/ LocateRegistry.getRegistry(ip,port);


            LoadBalancerServer loadBalancerServer = new LoadBalancerServerImpl();
            registry.rebind("LoadBalancerServer", loadBalancerServer);

            registryServer.registry("LoadBalancerServer", loadBalancerServer);

            System.out.println("LoadBalancerServer is running...");

            RegistryUtils.lock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
