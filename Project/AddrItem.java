import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public interface AddrItem extends Remote {
    public SealedObject getSpec(int itemId, SealedObject clientReq) throws RemoteException;
    public SecretKey getKey() throws RemoteException;
    public void generateSessionKey() throws RemoteException;
}