import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements AddrItem {
    public Server() {
        super();
    }

    public AuctionItem getSpec(int itemId, int clientId) {
        AuctionItem item = new AuctionItem(itemId, "Test", "Test");
        System.out.println("client request handled");
        return item;
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "myserver";
            AddrItem stub = (AddrItem) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}