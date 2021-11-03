
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeyHandler {
    private SecretKey aesKey;
    private int clientId;

    public KeyHandler(int clientId, SecretKey aesKey) {
        this.clientId = clientId;
        this.aesKey = aesKey;
    }

    public Cipher getCipher() {
        try {
            // Generate key
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            SecretKey aesKey = kgen.generateKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            return cipher;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
