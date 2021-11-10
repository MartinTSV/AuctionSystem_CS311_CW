import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

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
    private Semaphore mutex = new Semaphore(1);
    private Random rand = new Random();

    public Server() {
        super();
    }

    public SealedObject getSpec(int itemId, SealedObject clientReq) {
        try {
            for (AuctionItem item : auctionItems) {
                if (item.getIId() == itemId) {
                    // Creating encryption
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey);

                    SealedObject sealedObject = new SealedObject(item, cipher);

                    System.out.println("client request handled");
                    return sealedObject;
                }
            }
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            String s = "invalid id";
            SealedObject sealedObject = new SealedObject(s, cipher);
            System.out.println("client request handled");
            return sealedObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SealedObject viewAuctions() {
        ArrayList<Auction> arrayList = new ArrayList<Auction>();
        for (Auction auction : auctions) {
            if (auction.getSoldStatus() != true) {
                arrayList.add(auction);
            }
        }
        try {
            // Initiate cipher and create empty sealed object.
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            SealedObject sealedObject;

            if (arrayList.size() <= 0) {
                sealedObject = new SealedObject("empty list", cipher);
                System.out.println("buyer client request handled");
                return sealedObject;
            } else {
                sealedObject = new SealedObject(arrayList, cipher);
                System.out.println("buyer client request handled");
                return sealedObject;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SealedObject createAuction(String itemTitle, String itemDescription, int startingPrice, int buyout,
            SealedObject clientReq) {
        // Needs lock implementation.
        try {
            Cipher encrypter = Cipher.getInstance("AES");
            encrypter.init(Cipher.ENCRYPT_MODE, aesKey);

            Cipher decrypter = Cipher.getInstance("AES");
            decrypter.init(Cipher.DECRYPT_MODE, aesKey);

            String clientUUID = (String) clientReq.getObject(decrypter);

            int id = rand.nextInt(10000);
            AuctionItem item = new AuctionItem(id, itemTitle, itemDescription, clientUUID);
            dataManager.addItem(item);
            id = rand.nextInt(10000);
            Auction auction = new Auction(id, item, startingPrice, buyout);
            dataManager.addAuction(auction);

            SealedObject uniqueId = new SealedObject(id, encrypter);

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
        try {
            Cipher decrypter = Cipher.getInstance("AES");
            decrypter.init(Cipher.DECRYPT_MODE, aesKey);
            String clientUUID = (String) clientReq.getObject(decrypter);
            mutex.acquire();
            for (Auction auction : auctions) {
                if (auction.getAuctionId() == uniqueId && auction.getItem().getPublisher().equals(clientUUID)) {
                    auction.changeStatus(); // Changes status to closed.
                    /* Creation of an encryptor */
                    Cipher encrypter = Cipher.getInstance("AES");
                    encrypter.init(Cipher.ENCRYPT_MODE, aesKey);

                    /* Sending closed auction to the SellerClient */
                    SealedObject sealedObject = new SealedObject(auction, encrypter);
                    System.out.println("auction closed");
                    mutex.release();
                    return sealedObject;
                }
            }
            /* Return indicator for false ID, if no match is found in the for loop. */
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            SealedObject sealedObject = new SealedObject("invalid id", cipher);
            mutex.release();
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
            System.out.println("Part_A Test ID: " + auctionItems.get(0).getIId());
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}