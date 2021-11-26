import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import java.util.UUID;

/*
 * 1. This class is responsible for creating an auction. It needs to create an
 * item, containing it's description, starting bid price and a buyout price. It
 * will create a unique id for the auction.
 * 
 * 2. The seller will also be able to close the auction.
 */
public class SellerClient {

    private static String uuid = UUID.randomUUID().toString();
    private static SecretKey key; // Server public key.
    private static SecretKey privateKey; // Client private key.

    private static KeyManager km = new KeyManager();

    public String getUUID() {
        return uuid;
    }

    public static void closeAuction(AddrItem server) {
        Scanner s = new Scanner(System.in);
        System.out.println("\n|!| Please enter the id of the auction you'd like to close: ");
        int uniqueId = s.nextInt();
        try {

            /* Initiating encrypter and decrypter */
            Cipher encrypter = km.getEncrypter(key, "DES");
            Cipher decrypter = km.getDecrypter(privateKey, "DES"); // Uses a private key to decrypt.

            SealedObject clientReq = new SealedObject(uuid, encrypter);
            SealedObject sealedObject = server.closeAuction(uniqueId, clientReq);

            if (sealedObject.getObject(decrypter).equals("invalid id")) {
                System.out.println("\n\t|!| You have entered an invalid auction id. |!|");
            } else {
                Auction auction = (Auction) sealedObject.getObject(decrypter);

                if (auction.getCurrentBidder().equals("None")) {
                    System.out.println(
                            "\n\t|*| Auction has been closed, no bidders were found and won't appear in your closed auctions. |*|");
                } else {
                    System.out.println("\n\t|$| Auction has been closed for " + auction.getCurrentPrice() + "$."
                            + "\n\t|$| Winner is " + auction.getCurrentBidder());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fetchClosedAuctions(AddrItem server) {
        try {

            Cipher encrypter = km.getEncrypter(key, "DES");
            Cipher decrypter = km.getDecrypter(privateKey, "DES");

            SealedObject clientReq = new SealedObject(uuid, encrypter);

            SealedObject sealedObject = server.viewClosedAuctions(clientReq);

            if (sealedObject.getObject(decrypter).equals("empty")) {
                System.out.println("\n\t|!| You have no closed auctions. |!|");
            } else {
                @SuppressWarnings("unchecked")
                ArrayList<Auction> auctions = (ArrayList<Auction>) sealedObject.getObject(decrypter);

                AuctionItem item;
                for (Auction auction : auctions) {
                    if (!auction.getCurrentBidder().equals("None")) {
                        item = auction.getItem();
                        System.out.println(" _____________________________");
                        System.out.println("|Item Title: " + item.getITitle());
                        System.out.println("|Item Description: " + item.getIDesc());
                        System.out.println("|Sold for: " + auction.getCurrentPrice() + "$");
                        System.out.println("|Sold to: " + auction.getCurrentBidder() + "\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openAuction(AddrItem server) {

        Scanner sString = new Scanner(System.in).useDelimiter("\n");// Needed for spaces.
        Scanner sInteger = new Scanner(System.in); // Separate scanner for integers.
        System.out.println("\nIn order to create an auction please fill the information below.");

        System.out.println("Enter the item's name: ");
        String itemName = sString.next();

        System.out.println("Enter a short description: ");
        String itemDescription = sString.next();

        System.out.println("Enter bidding starting price: ");
        int startingPrice = sInteger.nextInt();

        System.out.println("Enter buyout price: ");
        int buyoutPrice = sInteger.nextInt();

        try {

            /* Getting sessiong key from server */
            Cipher encrypter = km.getEncrypter(key, "DES");
            Cipher decrypter = km.getDecrypter(privateKey, "DES"); // Private key for decryption.

            SealedObject clientReq = new SealedObject(uuid, encrypter); // Dummy clientReq
            SealedObject sealedItem = server.createAuction(itemName, itemDescription, startingPrice, buyoutPrice,
                    clientReq);
            if (sealedItem.getObject(decrypter).equals("invalid bid")) {
                System.out.println("\n\t|!| Bid is less than starting price, auction creation is canceled. |!|");
            } else {
                int uniqueId = (Integer) sealedItem.getObject(decrypter);

                System.out.println(
                        "\n\t|*| Your auction has been created, this is your unique id: " + uniqueId + " |*|\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Challenges server to decrypt uuid and challenge, combine them and return them
     * encrypted with client's private key.
     */
    public static Boolean authenticateServer(AddrItem server) {
        try {
            /* Forge challenge elements for server verification. */
            Cipher encrypter = km.getEncrypter(key, "DES");
            Cipher decrypter = km.getDecrypter(privateKey, "DES");

            int challengeInt = new Random().nextInt(10000); // Generate a challenge
            String challengeSt = String.valueOf(challengeInt); // Turn challenge to string.

            SealedObject clientReq = new SealedObject(uuid, encrypter);
            SealedObject challengeSeal = new SealedObject(challengeSt, encrypter);
            /* Verify server response. */
            String serverResponse = (String) server.verifyServer(challengeSeal, clientReq).getObject(decrypter);
            String clientChallenge = uuid + challengeSt;

            return serverResponse.equals(clientChallenge);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Client accepting server's challenge to decrypt and apply to identificator by
     * decrypting with it's own private key in function @challengeClient() and
     * return the result to server for verification in function @verifyClient().
     */
    public static Boolean authenticateClient(AddrItem server) {
        try {
            Cipher decrypter = km.getDecrypter(privateKey, "DES");
            Cipher encrypter = km.getEncrypter(key, "DES");

            SealedObject clientReq = new SealedObject(uuid, encrypter);
            String serverChallenge = (String) server.challengeClient(clientReq).getObject(privateKey);
            serverChallenge = uuid + serverChallenge;
            Boolean result = (Boolean) server.verifyClient(new SealedObject(serverChallenge, encrypter), clientReq)
                    .getObject(decrypter);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String choice;
        try {
            /* Binding to server */
            String name = "myserver";
            Registry registry = LocateRegistry.getRegistry("localhost");
            AddrItem server = (AddrItem) registry.lookup(name);
            /* Getting public key from server */
            String filepath = "keystore.keystore";
            key = km.LoadFromKeyStore(filepath, "password", "serverPublic");
            /* Generating private key from server */
            privateKey = km.generateSessionKey();
            km.StoreToKeyStore(privateKey, "password", filepath, uuid);

            if (!authenticateServer(server)) {
                System.out.println("\nCouldn't authenticate server. |!|");
            } else if (!authenticateClient(server)) {
                System.out.println("\nServer couldn't authenticate this client. |!|");
            } else {
                System.out.println("|*| Connection to server established. |*|");
                System.out.println("You are now using the seller client, logged as: " + uuid);

                while (true) {
                    Scanner scan = new Scanner(System.in);
                    System.out.println("\n|!| Please select one of the fucntions below:\n1. Create an auction item."
                            + "\n2. Close a listed auction." + "\n3. View your closed auctions." + "\n4. Exit client.");
                    choice = scan.nextLine();
                    if (choice.equals("1")) {
                        openAuction(server);
                    } else if (choice.equals("2")) {
                        closeAuction(server);
                    } else if (choice.equals("3")) {
                        fetchClosedAuctions(server);
                    } else if (choice.equals("4")) {
                        System.out.println("\n\t|*| Client closed. |*|");
                        scan.close();
                        return;
                    } else {
                        System.out.println("\n\t|!| Please enter a valid function number. |!|");
                    }
                }
            }

        } catch (

        Exception e) {
            e.printStackTrace();
        }
    }
}
