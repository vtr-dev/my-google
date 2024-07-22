package registry;

import java.rmi.Naming;
import java.rmi.Remote;

public class RegistryUtils {

    public static final String IP_REGISTRY = "127.0.0.1";
    public static final int PORT_REGISTRY = 1099;


    public static Remote lookupServer(String alias, String ip, int port){
        try {
            return Naming.lookup("rmi://" + ip + ":" + port + "/" + alias);
        } catch (Exception e) {
            throw new RuntimeException("Error lookup Server", e);
        }
    }

    public static void lock() {
        Object lock = new Object();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException("Error lock Server",e);
            }
        }
    }
}
