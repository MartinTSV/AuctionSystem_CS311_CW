import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public class SellerClient {

    /*
     * 1. This class is responsible for creating an auction. It needs to create an
     * item, containing it's description, starting bid price and a buyout price. It
     * will create a unique id for the auction.
     * 
     * 2. The seller will also be able to close the auction.
     */
    public static void main(String[] args) {
        SellerClient client = new SellerClient();
        int choice;
        System.out.println("\n*********************************************************************************");
        System.out.println("\nYou are now using the seller client.");
        Scanner scan = new Scanner(System.in);
        try {
            /* Binding to server */
            String name = "myserver";
            Registry registry = LocateRegistry.getRegistry("localhost");
            AddrItem server = (AddrItem) registry.lookup(name);

            while (true) {
                System.out.println("\n|!| Please select one of the fucntions below:\n1. Create an auction item."
                        + "\n2. Close a listed auction.");
                choice = scan.nextInt();
                if (choice == 1) {
                    client.openAuction(server);
                } else if (choice == 2) {
                    client.closeAuction(server);
                } else {
                    System.out.println("\n\t|!| Please enter a valid function number. |!|");
                }

            }
        } catch (Exception e) {
            System.out.println("\n\t|*| Client closed. |*|");
            scan.close();
        }
    }

    public void closeAuction(AddrItem server) {
        Scanner s = new Scanner(System.in);

        System.out.println("|!| Please enter the id of the auction you'd like to close: ");
        int uniqueId = s.nextInt();
        try {
            /* Getting sessiong key from server */
            server.generateSessionKey();
            SecretKey aesKey = server.getKey();

            /* Creating encrypter. */
            Cipher encrypter = Cipher.getInstance("AES");
            encrypter.init(Cipher.ENCRYPT_MODE, aesKey);
            /* Creating decrypter. */
            Cipher decrypter = Cipher.getInstance("AES");
            decrypter.init(Cipher.DECRYPT_MODE, aesKey);

            SealedObject clientReq = new SealedObject(2, encrypter);
            SealedObject sealedObject = server.closeAuction(uniqueId, clientReq);

            /* Needed for object comparison */
            String invalidId = new String("invalid id");

            if (sealedObject.getObject(decrypter).equals(invalidId)) {
                System.out.println("You have entered an invalid auction id.");
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
        Scanner s = new Scanner(System.in);
        System.out.println("\nIn order to create an auction please fill the information below.");

        System.out.println("Enter the item's name: ");
        String itemName = s.nextLine();

        System.out.println("Enter a short description: ");
        String itemDescription = s.nextLine();

        System.out.println("Enter bidding starting price: ");
        int startingPrice = s.nextInt();

        System.out.println("Enter buyout price: ");
        int buyoutPrice = s.nextInt();

        try {
            /* Getting sessiong key from server */
            server.generateSessionKey();
            SecretKey aesKey = server.getKey();

            /* Creating encrypter. */
            Cipher encrypter = Cipher.getInstance("AES");
            encrypter.init(Cipher.ENCRYPT_MODE, aesKey);

            /* Creating decrypter */
            Cipher decrypter = Cipher.getInstance("AES");
            decrypter.init(Cipher.DECRYPT_MODE, aesKey);

            SealedObject clientReq = new SealedObject("name", encrypter); // Dummy clientReq
            SealedObject sealedItem = server.createAuction(itemName, itemDescription, startingPrice, buyoutPrice,
                    clientReq);
            int uniqueId = (Integer) sealedItem.getObject(decrypter);

            System.out.println("\n\t|*| Your auction has been created, this is your unique id: " + uniqueId + " |*|\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
