import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.crypto.SealedObject;

public interface AddrItem extends Remote {

    public SealedObject createAuction(String itemTitle, String itemDescription, int startingPrice, int buyout,
            SealedObject clientReq) throws RemoteException;

    public SealedObject closeAuction(int uniqueId, SealedObject clientReq) throws RemoteException;

    public SealedObject viewClosedAuctions(SealedObject clientReq) throws RemoteException;

    public SealedObject viewAuctions(SealedObject clientReq) throws RemoteException;

    public SealedObject verifyServer(SealedObject challenge, SealedObject clientReq) throws RemoteException;

    public SealedObject placeBid(int id, int bid, SealedObject clientReq) throws RemoteException;

    public SealedObject verifyClient(SealedObject verification, SealedObject clientReq) throws RemoteException;

    public SealedObject challengeClient(SealedObject clientReq) throws RemoteException;
}