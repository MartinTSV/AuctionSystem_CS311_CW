import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        try {
            String name = "myserver";
            Registry registry = LocateRegistry.getRegistry("localhost");
            AddrItem server = (AddrItem) registry.lookup(name);

            AuctionItem item = server.getSpec(1, 2);
            System.out.println("desc: " + item.getIDesc() + "\ntitle: " + item.getITitle());
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}