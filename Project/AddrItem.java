import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AddrItem extends Remote {
    public AuctionItem getSpec(int itemId, int clientId) throws RemoteException;
}