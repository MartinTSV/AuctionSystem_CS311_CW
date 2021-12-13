
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.View;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.Message.TransientFlag;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;

public class Replica {

    private JChannel channel;
    private RpcDispatcher dispatcher;
    private RequestOptions opts;

    private ArrayList<AuctionItem> auctionItems = new ArrayList<AuctionItem>();
    private static ArrayList<Auction> auctions = new ArrayList<Auction>();

    private RspList rspList;

    private void start() {
        try {
            channel = new JChannel();
            /* Setting response mode to receive responses from every member in cluster */
            opts = new RequestOptions(ResponseMode.GET_ALL, 1000L).setTransientFlags(TransientFlag.DONT_LOOPBACK);
            /* Initiating dispatcher */
            dispatcher = new RpcDispatcher(channel, this);
            channel.connect("MyCluster");

            rspList = dispatcher.callRemoteMethods(null, "getReplicaAuctions", new Object[] {}, new Class[] {}, opts);
            /*
             * If majority returns null create an empty list, else auctions is equal to what
             * majority returns
             */
            auctions = majority() == null ? new ArrayList<>() : (ArrayList<Auction>) majority();
        } catch (Exception e) {
            e.printStackTrace();
            channel.close();
        }
    }

    /* Gets the response from the first node in cluster */
    private Object majority() throws Exception {
        Map<Integer, Object> result = new HashMap<>();

        /* If there are no results return an empty list */
        if (rspList.getResults().isEmpty()) {
            return null;
        } else {
            for (Object response : rspList.getResults()) {
                if (!result.containsValue(response))
                    result.put(1, response);
                else {
                    continue;
                }
            }
            return result.get(1);
        }
    }

    /* Gets the current state of auctions in cluster */
    public List<Auction> getReplicaAuctions() {
        List<Auction> response = new ArrayList<>();
        for (Auction auction : auctions)
            response.add(auction);
        return response;
    }

    public void closeAuction(Auction auction) {
        for (Auction currAuction : auctions) {
            if (currAuction.getAuctionId() == auction.getAuctionId()) {
                currAuction.changeStatus();
                System.out.println("[*] Auction: " + currAuction.getAuctionId() + " was closed.");
            }
        }

    }

    public void addAuction(Auction auction) {
        auctions.add(auction);
        System.out.println("[*] Added auction: " + auction.getItem().getITitle());
    }

    public void applyBid(Auction auction, String sold, int bid, String uuid) {
        for (Auction currAuction : auctions) {
            if (currAuction.getAuctionId() == auction.getAuctionId()) {
                if (sold.equals("sold")) {
                    currAuction.newBid(bid, uuid);
                    currAuction.changeStatus();
                    System.out.println("[*] Auction " + currAuction.getAuctionId() + " was sold to: " + uuid);
                } else {
                    currAuction.newBid(bid, uuid);
                    System.out.println("[*] Bid by " + uuid + " was placed for auction " + currAuction.getAuctionId());
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Replica().start();
    }
}