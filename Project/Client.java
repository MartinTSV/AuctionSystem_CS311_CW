// import java.rmi.registry.LocateRegistry;
// import java.rmi.registry.Registry;

// import javax.crypto.Cipher;
// import javax.crypto.SealedObject;
// import javax.crypto.SecretKey;
// import java.util.Scanner;

// public class Client {
// public static void main(String[] args) {
// try {
// String name = "myserver";
// Registry registry = LocateRegistry.getRegistry("localhost");
// AddrItem server = (AddrItem) registry.lookup(name);

// Scanner scan = new Scanner(System.in);
// System.out.println("Enter the id of an item: ");
// int itemId = scan.nextInt();

// SecretKey aesKey = server.getKey();
// // Creating encrypter.
// Cipher encrypter = Cipher.getInstance("AES");
// encrypter.init(Cipher.ENCRYPT_MODE, aesKey);
// // Creating decrypter.
// Cipher decrypter = Cipher.getInstance("AES");
// decrypter.init(Cipher.DECRYPT_MODE, aesKey);

// SealedObject clientReq = new SealedObject(2, encrypter);
// SealedObject sealedItem = server.getSpec(itemId, clientReq);
// if (sealedItem.getObject(decrypter).equals("invalid id")) {
// System.out.println("Invalid id.");
// scan.close();
// } else {
// AuctionItem item = (AuctionItem) sealedItem.getObject(decrypter);

// System.out.println("title: " + item.getITitle() + "\ndescription: " +
// item.getIDesc());
// scan.close();
// }

// } catch (Exception e) {
// System.err.println("Exception:");
// e.printStackTrace();
// }
// }
// }