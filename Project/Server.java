import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RMISecurityManager;
import java.rmi.server.UnicastRemoteObject;
import javax.crypto.SealedObject;
import java.net.*;

public class Server extends UnicastRemoteObject implements AddrItem {
    private static final int PORT = 0;

    public Server() throws Exception {
        super(PORT,
                new CSFactory(),
                new SSFactory());
    }

    public AuctionItem getSpec(int itemId, int clientReq) {

        AuctionItem item = new AuctionItem(itemId, "Test", "Test");
        System.out.println("client request handled");
        return item;
    }

    public static void main(String[] args) {
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new RMISecurityManager());
            }
            try {
                Registry registry = LocateRegistry.createRegistry(PORT,
                        new CSFactory(),
                        new SSFactory());

                Server server = new Server();
                registry.bind("myserver", server);

            } catch (Exception e) {
                System.err.println("Exception:");
                e.printStackTrace();
            }
        }
    }
