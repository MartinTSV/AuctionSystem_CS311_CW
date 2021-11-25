import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public interface AddrItem extends Remote {
    public SealedObject getSpec(int itemId, SealedObject clientReq) throws RemoteException;

    public SealedObject createAuction(String itemTitle, String itemDescription, int startingPrice, int buyout,
            SealedObject clientReq) throws RemoteException;

    public SealedObject closeAuction(int uniqueId, SealedObject clientReq) throws RemoteException;

    public SealedObject viewAuctions() throws RemoteException;

    public SealedObject verifyServer(byte[] challenge, SealedObject clientReq) throws RemoteException;

    public SealedObject placeBid(int id, int bid, SealedObject clientReq) throws RemoteException;
}