import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import javax.crypto.SealedObject;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Client {
    private static final int PORT = 0;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry((
                    InetAddress.getLocalHost().getHostName()), PORT, new CSFactory());
            AddrItem server = (AddrItem) registry.lookup("myserver");

            int itemId = 1;
            int clientReq = 1;

            AuctionItem test;

            test = server.getSpec(itemId, clientReq);
             System.out.println("desc: " + test.getIDesc() + "\ntitle: " +
             test.getITitle());

        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}