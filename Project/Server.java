import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SealedObject;

public class Server implements AddrItem {

    private static final int PORT = 1234;
    private SecretKey aesKey;

    public Server() {
        super();

    }

    public SealedObject getSpec(int itemId, SealedObject clientReq){
        try {
            AuctionItem auctionItem = new AuctionItem(itemId, "Test", "Test");

            //Creating encryption
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            SealedObject item = new SealedObject(auctionItem, cipher);

            System.out.println("client request handled");
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void generateSessionKey(){
        try{
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            this.aesKey = kgen.generateKey();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public SecretKey getKey() {
        return aesKey;
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "myserver";
            AddrItem stub = (AddrItem) UnicastRemoteObject.exportObject(s, PORT);
            Registry registry = LocateRegistry.getRegistry();

            registry.rebind(name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}