import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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

    private String uuid = UUID.randomUUID().toString();

    public String getUUID() {
        return uuid;
    }

    public void closeAuction(AddrItem server) {
        Scanner s = new Scanner(System.in);
        System.out.println("|!| Please enter the id of the auction you'd like to close: ");
        int uniqueId = s.nextInt();
        try {
            /* Getting sessiong key from server */
            server.generateSessionKey();
            SecretKey aesKey = server.getKey();

            Cipher encrypter = getEncrypter(aesKey);
            Cipher decrypter = getDecrypter(aesKey);

            SealedObject clientReq = new SealedObject(getUUID(), encrypter);
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

    public void openAuction(AddrItem server) {

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
            server.generateSessionKey();
            SecretKey aesKey = server.getKey();

            Cipher encrypter = getEncrypter(aesKey);
            Cipher decrypter = getDecrypter(aesKey);

            SealedObject clientReq = new SealedObject(getUUID(), encrypter); // Dummy clientReq
            SealedObject sealedItem = server.createAuction(itemName, itemDescription, startingPrice, buyoutPrice,
                    clientReq);
            int uniqueId = (Integer) sealedItem.getObject(decrypter);

            System.out.println("\n\t|*| Your auction has been created, this is your unique id: " + uniqueId + " |*|\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cipher getEncrypter(SecretKey aesKey) {
        try {
            Cipher encrypter = Cipher.getInstance("AES");
            encrypter.init(Cipher.ENCRYPT_MODE, aesKey);

            return encrypter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Cipher getDecrypter(SecretKey aesKey) {
        try {
            Cipher encrypter = Cipher.getInstance("AES");
            encrypter.init(Cipher.DECRYPT_MODE, aesKey);

            return encrypter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        SellerClient client = new SellerClient();
        String choice;
        System.out.println("\n*********************************************************************************");
        System.out.println("\nYou are now using the seller client.");
        try {
            /* Binding to server */
            String name = "myserver";
            Registry registry = LocateRegistry.getRegistry("localhost");
            AddrItem server = (AddrItem) registry.lookup(name);

            while (true) {
                Scanner scan = new Scanner(System.in);
                System.out.println("\n|!| Please select one of the fucntions below:\n1. Create an auction item."
                        + "\n2. Close a listed auction.");
                choice = scan.nextLine();
                if (choice.equals("1")) {
                    client.openAuction(server);
                } else if (choice.equals("2")) {
                    client.closeAuction(server);
                } else {
                    System.out.println("\n\t|!| Please enter a valid function number. |!|");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\n\t|*| Client closed. |*|");
        }
    }
}
