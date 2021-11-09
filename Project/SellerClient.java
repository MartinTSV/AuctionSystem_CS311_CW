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
        Scanner scan = new Scanner(System.in);

        System.out.println("In order to create an auction please fill the information below.");

        System.out.println("Enter the item's name: ");
        String itemName = scan.nextLine();

        System.out.println("Enter a short description: ");
        String itemDescription = scan.nextLine();

        System.out.println("Enter bidding starting price: ");
        int startingPrice = scan.nextInt();

        System.out.println("Enter buyout price: ");
        int buyoutPrice = scan.nextInt();

        try {
            String name = "myserver";
            Registry registry = LocateRegistry.getRegistry("localhost");
            AddrItem server = (AddrItem) registry.lookup(name);
            server.generateSessionKey();

            SecretKey aesKey = server.getKey();
            // Creating encrypter.
            Cipher encrypter = Cipher.getInstance("AES");
            encrypter.init(Cipher.ENCRYPT_MODE, aesKey);

            Cipher decrypter = Cipher.getInstance("AES");
            decrypter.init(Cipher.DECRYPT_MODE, aesKey);

            SealedObject clientReq = new SealedObject("name", encrypter); // Dummy clientReq
            SealedObject sealedItem = server.createAuction(itemName, itemDescription, startingPrice, buyoutPrice,
                    clientReq);
            int uniqueId = (Integer) sealedItem.getObject(decrypter);

            System.out.println("\n\t* Your auction has been created, this is your unique id: " + uniqueId + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
