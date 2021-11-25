import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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

            Cipher encrypter = km.getEncrypter(key, "DES");
            Cipher decrypter = km.getDecrypter(privateKey, "DES");

            SealedObject clientReq = new SealedObject(uuid, encrypter);
            SealedObject sealedObject = server.closeAuction(uniqueId, clientReq);

            if (sealedObject.getObject(decrypter).equals("invalid id")) {
                System.out.println("\n\t|!| You have entered an invalid auction id. |!|");
            } else {
                Auction auction = (Auction) sealedObject.getObject(decrypter);
                System.out.println("\n\t|$| Auction has been sold for " + auction.getCurrentPrice() + "$."
                        + "\n\t|$| Winner is " + auction.getCurrentBidder());
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
            Cipher decrypter = km.getDecrypter(privateKey, "DES");

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

            /* Forge challenge elements for server verification. */
            Cipher encrypter = km.getEncrypter(key, "DES");
            Cipher decrypter = km.getDecrypter(privateKey, "DES");

            SealedObject clientReq = new SealedObject(uuid, encrypter);
            int challengeInt = new Random().nextInt(10000); // Generate a challenge
            String challengeSt = String.valueOf(challengeInt); // Turn challenge to string.

            /* Verify server response. */

            String serverResponse = (String) server.verifyServer(km.encryptString(challengeSt, key, "DES"), clientReq)
                    .getObject(decrypter);
            String clientChallenge = uuid + challengeSt;
            /* Verify client */
            String serverChallenge = (String) server.challengeClient(clientReq).getObject(privateKey);
            serverChallenge = uuid + serverChallenge;
            Boolean result = (Boolean) server.verifyClient(new SealedObject(serverChallenge, encrypter), clientReq)
                    .getObject(decrypter);

            if (!serverResponse.equals(clientChallenge)) {
                System.out.println("\n\t|!| Couldn't authenticate server. |!|");
            } else if (!result) {
                System.out.println("\n\t|!| Server couldn't authenticate this client. |!|");
            } else {
                System.out.println("\n\t|!| Connection to server established. |!|");
                System.out.println("\tYou are now using the seller client, logged as: " + uuid);

                while (true) {
                    Scanner scan = new Scanner(System.in);
                    System.out.println("\n|!| Please select one of the fucntions below:\n1. Create an auction item."
                            + "\n2. Close a listed auction." + "\n3. Exit client.");
                    choice = scan.nextLine();
                    if (choice.equals("1")) {
                        openAuction(server);
                    } else if (choice.equals("2")) {
                        closeAuction(server);
                    } else if (choice.equals("3")) {
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
