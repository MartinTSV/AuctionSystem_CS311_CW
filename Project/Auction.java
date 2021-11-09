import java.io.Serializable;

public class Auction implements Serializable {

    private int auctionId;
    private int buyout;
    private int startingPrice;
    private int currentPrice;

    private AuctionItem item;
    private String currentBidder;
    private boolean sold;

    public Auction(int auctionId, AuctionItem item, int startingPrice, int buyout) {
        this.auctionId = auctionId;
        this.item = item;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.buyout = buyout;
        this.sold = false;
        this.currentBidder = "None";
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

    public boolean getSoldStatus() {
        return sold;
    }

    public String getCurrentBidder() {
        return currentBidder;
    }

    public void changeStatus() {
        sold = true;
    }

    public void newBid(int newPrice, String newBidder) {
        currentPrice = newPrice;
        currentBidder = newBidder;
    }
}
