import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public class BuyerClient {
    private static String uuid = UUID.randomUUID().toString();

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

    public void placeBid(AddrItem server) {
        try {
            int id;
            int bid;

            Scanner sInteger = new Scanner(System.in); // Scans ID of the item.
            System.out.println("Enter an id of the item you would like to bid for: ");
            id = sInteger.nextInt();
            System.out.println("Enter your bid: ");
            bid = sInteger.nextInt();

            server.generateSessionKey();
            SecretKey aesKey = server.getKey();

            Cipher encrypter = getEncrypter(aesKey);
            Cipher decrypter = getDecrypter(aesKey);

            SealedObject clientReq = new SealedObject(uuid, encrypter);
            SealedObject sealedObject = server.placeBid(id, bid, clientReq);

            if (sealedObject.getObject(decrypter).equals("invalid bid")) {
                System.out.println("\n\t|!| Please enter a sum higher than the current bid. |!|");
            } else if (sealedObject.getObject(decrypter).equals("buyout reached")) {
                System.out.println("\n\t|$| You won the auction for: " + bid + "$ |$|");
            } else if (sealedObject.getObject(decrypter).equals("invalid item")) {
                System.out.println("\n\t|!| You have entered an invalid auction id or item might have been sold. |!|");
            } else {
                Auction auction = (Auction) sealedObject.getObject(decrypter);
                System.out.println("\n\t|$| Bid successfully placed! |$| " + auction);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            System.out.println("\nYou are now using the buyer client, logged as: " + uuid);
            System.out.println("Connection to server established.");

            String choice;
            while (true) {
                Scanner scan = new Scanner(System.in);
                System.out.println("\n|!| Please select one of the fucntions below:\n1. List auctions."
                        + "\n2. Place a bid." + "\n3. Exit client.");

                choice = scan.nextLine();
                if (choice.equals("1")) {
                    client.fetchAuctions(server);
                } else if (choice.equals("2")) {
                    client.placeBid(server);
                } else if (choice.equals("3")) {
                    System.out.println("\n\t|*| Client closed. |*|");
                    scan.close();
                    return;
                } else {
                    System.out.println("\n\t|!| Please enter a valid function number. |!|");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}