package registry;

import registry.impl.RegistryServerImpl;
import registry.interfaces.RegistryServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static registry.RegistryUtils.IP_REGISTRY;
import static registry.RegistryUtils.PORT_REGISTRY;

public class RegistryService {

    public static void main(String[] args) {
        try {

            System.setProperty("java.rmi.server.hostname", IP_REGISTRY);
            Registry registry = LocateRegistry.createRegistry(PORT_REGISTRY);

            RegistryServer registryServer = new RegistryServerImpl();
            registry.rebind("RegistryServer", registryServer);

            System.out.println("Locate registry is running...");

            RegistryUtils.lock();

        } catch (Exception e) {
            throw new RuntimeException("Erro Registry .", e);
        }

    }

}
