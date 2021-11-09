import java.io.Serializable;

public class Auction implements Serializable {

    private int auctionId;
    private int buyout;
    private int startingPrice;
    private int currentPrice;

    private AuctionItem item;
    private String currentBidder;

    public Auction(int auctionId, AuctionItem item, int startingPrice, int buyout) {
        this.auctionId = auctionId;
        this.item = item;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.buyout = buyout;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public int getBuyout() {
        return buyout;
    }

    public int getStartingPrice() {
        return startingPrice;
    }

    public AuctionItem getItem() {
        return item;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public String getCurrentBidder() {
        return currentBidder;
    }

    public void newBid(int newPrice, String newBidder) {
        currentPrice = newPrice;
        currentBidder = newBidder;
    }
}
