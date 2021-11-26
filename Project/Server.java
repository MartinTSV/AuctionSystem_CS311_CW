import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SealedObject;

public class Server implements AddrItem {

    private static final int PORT = 0; // Port switch.
    private static SecretKey key;
    private static KeyManager km = new KeyManager();

    private static ServerDataManager dataManager = new ServerDataManager();
    private static ArrayList<Auction> auctions = dataManager.getAuctions();
    private static ArrayList<AuctionItem> auctionItems = dataManager.getItems();
    private static HashMap<String, String> challenges = new HashMap<String, String>();
    /*
     * A semaphore needed for preventing bid and other interaction interceptions.
     */

    private Semaphore mutex = new Semaphore(1);
    private Random rand = new Random();

    public Server() {
        super();
    }

    /*
     * Return an array list of current active auctions by encrypting it with the
     * requester's private key.
     */
    public SealedObject viewAuctions(SealedObject clientReq) {
        ArrayList<Auction> arrayList = new ArrayList<Auction>();
        for (Auction auction : auctions) {
            if (auction.getSoldStatus() != true) {
                arrayList.add(auction);
            }
        }
        try {
            /* Finding the client key */
            Cipher decrypter = km.getDecrypter(key, "DES");
            String clientUUID = (String) clientReq.getObject(decrypter);
            SecretKey clientKey = km.LoadFromKeyStore("keystore.keystore", "password", clientUUID);

            /* Initiating an encrypter with the client key */
            Cipher encrypter = km.getEncrypter(clientKey, "DES");
            SealedObject sealedObject;

            if (arrayList.size() <= 0) {
                sealedObject = new SealedObject("empty list", encrypter);
                System.out.println("buyer client request handled");
                return sealedObject;
            } else {
                sealedObject = new SealedObject(arrayList, encrypter);
                System.out.println("buyer client request handled");
                return sealedObject;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Placing a bid for active auctions, contains error handling for when the bid
     * is lower or equal to the current one.
     * 
     * Implements a mutex to prevent bid interceptions.
     */
    public SealedObject placeBid(int id, int bid, SealedObject clientReq) {
        try {
            Cipher decrypter = km.getDecrypter(key, "DES");
            String clientUUID = (String) clientReq.getObject(decrypter);
            SecretKey clientKey = km.LoadFromKeyStore("keystore.keystore", "password", clientUUID);

            Cipher encrypter = km.getEncrypter(clientKey, "DES");

            SealedObject sealedObject;

            for (Auction auction : auctions) {
                /* Checks if such auction exists */
                if (auction.getAuctionId() == id && auction.getSoldStatus() != true) {
                    if (bid <= auction.getCurrentPrice()) {
                        sealedObject = new SealedObject("invalid bid", encrypter);
                        System.out.println("buyer client request handled");
                        return sealedObject;
                    } else if (bid >= auction.getBuyout()) {
                        /* If buyout has been reached, item is sold to bidder */
                        mutex.acquire();
                        auction.newBid(bid, clientUUID);
                        auction.changeStatus(); // Sets status to sold.
                        sealedObject = new SealedObject("buyout reached", encrypter);
                        mutex.release();
                        System.out.println("buyer client request handled");
                        return sealedObject;
                    } else {
                        /* If bid is valid */
                        mutex.acquire();
                        auction.newBid(bid, clientUUID);
                        sealedObject = new SealedObject(auction, encrypter);
                        mutex.release();
                        System.out.println("buyer client request handled");
                        return sealedObject;
                    }
                }
            }
            /* Return indicator if no such auction exists. */
            sealedObject = new SealedObject("invalid item", encrypter);
            System.out.println("buyer client request handled");
            return sealedObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Creates an auction with a random id, returns the id as an encrypted object.
     */
    public SealedObject createAuction(String itemTitle, String itemDescription, int startingPrice, int buyout,
            SealedObject clientReq) {
        try {

            /* Find who the client is. */
            Cipher decrypter = km.getDecrypter(key, "DES");
            String clientUUID = (String) clientReq.getObject(decrypter);

            /* Get client private key. */
            SecretKey clientKey = km.LoadFromKeyStore("keystore.keystore", "password", clientUUID);
            Cipher encrypter = km.getEncrypter(clientKey, "DES");

            SealedObject sealedObject;
            if (startingPrice > buyout) {
                System.out.println("seller client request handled");
                sealedObject = new SealedObject("invalid bid", encrypter);
                return sealedObject;
            } else {

                int id = rand.nextInt(10000);
                AuctionItem item = new AuctionItem(id, itemTitle, itemDescription, clientUUID);
                dataManager.addItem(item);
                id = rand.nextInt(10000);
                Auction auction = new Auction(id, item, startingPrice, buyout);
                dataManager.addAuction(auction);

                sealedObject = new SealedObject(id, encrypter);

                System.out.println("seller client request handled");
                return sealedObject;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Closes an auction, also if successfully closed sends the auction to the
     * client. Requires uuid stored in clientReq in order to verify that the user
     * hosts the auction.
     * 
     * Uses a mutex to stop bids while the auction is being closed.
     */
    public SealedObject closeAuction(int auctionId, SealedObject clientReq) {
        try {
            Cipher decrypter = km.getDecrypter(key, "DES");

            String clientUUID = (String) clientReq.getObject(decrypter);

            SecretKey clientKey = km.LoadFromKeyStore("keystore.keystore", "password", clientUUID);
            Cipher encrypter = km.getEncrypter(clientKey, "DES");

            mutex.acquire();
            for (Auction auction : auctions) {
                if (auction.getAuctionId() == auctionId && auction.getItem().getPublisher().equals(clientUUID)
                        && auction.getSoldStatus() != true) {
                    auction.changeStatus(); // Changes status to closed.
                    /* Sending closed auction to the SellerClient */
                    SealedObject sealedObject = new SealedObject(auction, encrypter);
                    System.out.println("seller client request handled");
                    mutex.release();
                    return sealedObject;
                }
            }
            /* Return indicator for false ID, if no match is found in the for loop. */
            SealedObject sealedObject = new SealedObject("invalid id", encrypter);
            mutex.release();
            return sealedObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SealedObject viewClosedAuctions(SealedObject clientReq) {
        try {
            Cipher decrypter = km.getDecrypter(key, "DES");
            String clientUUID = (String) clientReq.getObject(decrypter);
            SecretKey clientKey = km.LoadFromKeyStore("keystore.keystore", "password", clientUUID);
            Cipher encrypter = km.getEncrypter(clientKey, "DES");

            ArrayList<Auction> closedAuctions = new ArrayList<Auction>();
            for (Auction auction : auctions) {
                if (auction.getItem().getPublisher().equals(clientUUID) && auction.getSoldStatus()) {
                    closedAuctions.add(auction);
                }
            }

            if (closedAuctions.size() <= 0) {
                return new SealedObject("empty", encrypter);
            } else {
                return new SealedObject(closedAuctions, encrypter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Server verification, decrypts a challenge passed by the client, encrypted
     * with server's public key, finds the client's private key and again encrypts
     * the challenge, returning it back to the client.
     */
    public SealedObject verifyServer(SealedObject challenge, SealedObject clientReq) {
        try {
            Cipher decrypter = km.getDecrypter(key, "DES");
            String clientUUID = (String) clientReq.getObject(decrypter);
            String clientChallenge = (String) challenge.getObject(decrypter);

            SecretKey clientKey = km.LoadFromKeyStore("keystore.keystore", "password", clientUUID);
            String verification = clientUUID + clientChallenge;

            SealedObject sealedObject = new SealedObject(verification, km.getEncrypter(clientKey, "DES"));
            return sealedObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Step I of client verification.
     * 
     * Sends a challenge to the client, encrypted with client's private key.
     * 
     * The challenge is stored in a @challenges, which is a hashmap that uses the
     * uuid as a key for challenge access.
     */
    public SealedObject challengeClient(SealedObject clientReq) {
        try {
            Cipher decrypter = km.getDecrypter(key, "DES");

            String clientUUID = (String) clientReq.getObject(decrypter);
            SecretKey clientKey = km.LoadFromKeyStore("keystore.keystore", "password", clientUUID);
            Cipher encrypter = km.getEncrypter(clientKey, "DES");

            String challenge = String.valueOf(new Random().nextInt(10000));

            if (challenges.containsKey(clientUUID)) {
                challenges.replace(clientUUID, challenge);
                return new SealedObject(challenge, encrypter);
            } else {
                challenges.put(clientUUID, challenge);
                return new SealedObject(challenge, encrypter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Step II of client verfication. The client returns the challenge from Part I,
     * encrypted with the server's private key.
     * 
     * returns true if the challenge matches the one from Part I.
     */
    public SealedObject verifyClient(SealedObject verification, SealedObject clientReq) {
        try {
            Cipher decrypter = km.getDecrypter(key, "DES");

            String clientVerification = (String) verification.getObject(decrypter);
            String clientUUID = (String) clientReq.getObject(decrypter);
            String serverChallenge = challenges.get(clientUUID);
            serverChallenge = clientUUID + serverChallenge;

            SecretKey clientKey = km.LoadFromKeyStore("keystore.keystore", "password", clientUUID);
            Cipher encrypter = km.getEncrypter(clientKey, "DES");

            if (serverChallenge.equals(clientVerification)) {
                System.out.println(clientUUID + " connected to server");
                return new SealedObject(true, encrypter);
            } else {
                return new SealedObject(false, encrypter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

            /* Store public key */
            key = km.generateSessionKey();
            String filepath = "keystore.keystore";
            km.StoreToKeyStore(key, "password", filepath, "serverPublic");

            registry.rebind(name, stub);
            System.out.println("Server ready");

        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

}