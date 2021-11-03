import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.rmi.server.UnicastRemoteObject;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public class Client {
    public static void main(String[] args) {
        try {
            String name = "myserver";
            Registry registry = LocateRegistry.getRegistry("localhost");
            AddrItem server = (AddrItem) registry.lookup(name);

            server.generateSessionKey();
            SecretKey aesKey = server.getKey();
            //Creating encrypter.
            Cipher encrypter = Cipher.getInstance("AES");
            encrypter.init(Cipher.ENCRYPT_MODE, aesKey);
            //Creating decrypter.
            Cipher decrypter = Cipher.getInstance("AES");
            decrypter.init(Cipher.DECRYPT_MODE, aesKey);

            SealedObject clientReq = new SealedObject(2, encrypter);
            SealedObject sealedItem = server.getSpec(1, clientReq);

            AuctionItem item = (AuctionItem) sealedItem.getObject(decrypter);

            System.out.println("desc: " + item.getIDesc() + "\ntitle: " + item.getITitle());
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}