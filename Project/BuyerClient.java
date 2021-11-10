import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public class BuyerClient {
    private ArrayList<Auction> currentAuctions = new ArrayList<Auction>();

    public ArrayList<Auction> getCurrentAuctions() {
        return currentAuctions;
    }

    public void updateCurrentAuctions(ArrayList<Auction> currentAuctions) {
        this.currentAuctions = currentAuctions;
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

    public static Cipher getDecrypter(SecretKey aesKey) {
        try {
            Cipher encrypter = Cipher.getInstance("AES");
            encrypter.init(Cipher.DECRYPT_MODE, aesKey);

            return encrypter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void fetchAuctions(AddrItem server) {
        try {
            server.generateSessionKey();
            SecretKey aesKey = server.getKey();

            SealedObject sealedObject = server.viewAuctions();

            Cipher decrypter = getDecrypter(aesKey);

            if (sealedObject.getObject(decrypter).equals("empty list")) {
                System.out.println("No active auctions, check again later.");
            } else {
                @SuppressWarnings("unchecked")
                ArrayList<Auction> auctions = (ArrayList<Auction>) sealedObject.getObject(decrypter);
                updateCurrentAuctions(auctions);
                AuctionItem item;
                for (Auction auction : auctions) {
                    item = auction.getItem();
                    System.out.println(" _____________________________");
                    System.out.println("|Auction ID: " + auction.getAuctionId());
                    System.out.println("|Item Title: " + item.getITitle());
                    System.out.println("|Item Description: " + item.getIDesc());
                    System.out.println("|Current Price: " + auction.getCurrentPrice() + "\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            BuyerClient client = new BuyerClient();
            String name = "myserver";
            Registry registry = LocateRegistry.getRegistry("localhost");
            AddrItem server = (AddrItem) registry.lookup(name);

            client.fetchAuctions(server);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}