import java.io.Serializable;

public class AuctionItem implements Serializable {
    private int itemId;
    private String itemTitle;
    private String itemDescription;
    private String publisher;

    public AuctionItem(int itemId, String itemTitle, String itemDescription, String publisher) {
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.itemDescription = itemDescription;
        this.publisher = publisher;
    }

    public int getIId() {
        return itemId;
    }

    public String getITitle() {
        return itemTitle;
    }

    public String getIDesc() {
        return itemDescription;
    }

    public String getPublisher() {
        return publisher;
    }
}
