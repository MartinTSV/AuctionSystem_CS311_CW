import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SealedObject;

public class Server implements AddrItem {

    private static final int PORT = 0; // Port switch.
    private SecretKey aesKey;
    private static ServerDataManager dataManager = new ServerDataManager();
    private static ArrayList<Auction> auctions = dataManager.getAuctions();
    private static ArrayList<AuctionItem> auctionItems = dataManager.getItems();
    private Random rand = new Random();

    public Server() {
        super();
    }

    public SealedObject getSpec(int itemId, SealedObject clientReq) {
        try {
            AuctionItem auctionItem = new AuctionItem(itemId, "Test", "Test", "Test");

            // Creating encryption
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

    public SealedObject createAuction(String itemTitle, String itemDescription, int startingPrice, int buyout,
            SealedObject clientReq) {
        // Needs lock implementation.
        try {
            /* Create auction item. */
            int id = rand.nextInt(10000);
            AuctionItem item = new AuctionItem(id, itemTitle, itemDescription, "test");
            dataManager.addItem(item);
            auctionItems = dataManager.getItems();

            /* Create auction. */
            id = rand.nextInt(10000);
            Auction auction = new Auction(id, item, startingPrice, buyout);
            dataManager.addAuction(auction);
            auctions = dataManager.getAuctions();

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            SealedObject uniqueId = new SealedObject(id, cipher);
            System.out.println("seller client request handled");
            return uniqueId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SealedObject closeAuction(int uniqueId, SealedObject clientReq) {
        // Needs lock implementation.
        /* For loop for finding the correct auction */
        for (Auction auction : auctions) {
            if (auction.getAuctionId() == uniqueId) {
                auction.changeStatus(); // Changes status to closed.
                try {
                    /* Creation of an encryptor */
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey);

                    /* Sending closed auction to the SellerClient */
                    SealedObject sealedObject = new SealedObject(auction, cipher);
                    System.out.println("auction closed");
                    return sealedObject;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                continue;
            }
        }
        try {
            /* Return indicator for false ID, if no match is found in the for loop. */
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            SealedObject sealedObject = new SealedObject("invalid id", cipher);
            return sealedObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void generateSessionKey() {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            this.aesKey = kgen.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SecretKey getKey() {
        return aesKey;
    }

    public static void main(String[] args) {
        try {

            /* Adds test values to the @ServerDataManager class */
            dataManager.fillAuctionItems();
            dataManager.fillAuctions();

            auctionItems = dataManager.getItems();
            auctions = dataManager.getAuctions();

            /* Creates the server */
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