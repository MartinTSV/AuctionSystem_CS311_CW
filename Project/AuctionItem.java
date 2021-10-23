import java.io.Serializable;

public class AuctionItem implements Serializable {
    private int itemId;
    private String itemTitle;
    private String itemDescription;

    public AuctionItem(int itemId, String itemTitle, String itemDescription) {
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.itemDescription = itemDescription;
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
}
