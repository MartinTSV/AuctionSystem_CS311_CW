import java.util.ArrayList;
import java.util.Random;

/* Manages every list needed for a functioning auctioning system */
public class ServerDataManager {
    /*
     * The choice of two lists is due to extensibility for possible future purposes.
     */
    private ArrayList<Auction> auctions = new ArrayList<Auction>();
    private ArrayList<AuctionItem> auctionItems = new ArrayList<AuctionItem>();

    private Random rand = new Random();

    /* Fills @auctioItems with test objects. */
    public void fillAuctionItems() {
        if (auctionItems.size() <= 0) {
            int id;
            AuctionItem item;
            String titleArray[] = { "Bike", "Sunglasses", "Helmet", "Keyboard", "Guitar" };
            String descriptionArray[] = { "Very fast.", "Black sunglasses.", "Predator Helmet 2009.",
                    "Ducky, Cherry Switches.", "Ibanez Les Paul." };
            String publisherArray[] = { "Martin", "Maria", "Mr. Mathers", "Kumar", "Ajay" };

            for (int i = 0; i < 5; i++) {
                id = rand.nextInt(10000);
                item = new AuctionItem(id, titleArray[i], descriptionArray[i], publisherArray[i]);
                auctionItems.add(item);
            }
        }
    }

    /* Fills @auctions with test objects. */
    public void fillAuctions() {
        if (auctions.size() <= 0) {
            int id;
            Auction auction;
            for (int i = 0; i < 5; i++) {
                id = rand.nextInt(10000);
                auction = new Auction(id, auctionItems.get(i), rand.nextInt(1000), rand.nextInt(1000) + 2000);
                auctions.add(auction);
            }
        }
    }

    public ArrayList<Auction> getAuctions() {
        return auctions;
    }

    public ArrayList<AuctionItem> getItems() {
        return auctionItems;
    }

    /* Removes auction frtom @auctions */
    public void removeAuction(Auction auction) {
        auctions.remove(auction);
    }

    /* Removes item from @auctionItems */
    public void removeItem(AuctionItem item) {
        auctionItems.remove(item);
    }

    /* Adds an auction to @auctions */
    public void addAuction(Auction auction) {
        auctions.add(auction);
    }

    /* Adds an item to @auctionItems */
    public void addItem(AuctionItem item) {
        auctionItems.add(item);
    }
}
