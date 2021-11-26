import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.crypto.SecretKey;
import java.security.KeyStore;

/* The purpose of this class is to provide the necessary methods for encryption and decryption. */

public class KeyManager {

    /* Generates a 56 bit DES key. */
    public SecretKey generateSessionKey() {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("DES");
            kgen.init(56);
            SecretKey key = kgen.generateKey();
            return key;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Stores @key to a keystore file. */
    public void StoreToKeyStore(SecretKey key, String password, String filepath, String alias) {
        try {
            File file = new File(filepath);
            KeyStore javaKeyStore = KeyStore.getInstance("JCEKS");
            if (!file.exists()) {
                javaKeyStore.load(null, null); // Create a new file if one doesn't exist.
            } else {
                javaKeyStore.load(new FileInputStream(file), password.toCharArray());
            }

            javaKeyStore.setKeyEntry(alias, key, password.toCharArray(), null);
            OutputStream writeStream = new FileOutputStream(filepath);
            javaKeyStore.store(writeStream, password.toCharArray());
            writeStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Load a key from keystore file, by finding it by @alias */
    public SecretKey LoadFromKeyStore(String filepath, String password, String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            InputStream readStream = new FileInputStream(filepath);
            keyStore.load(readStream, password.toCharArray());
            SecretKey key = (SecretKey) keyStore.getKey(alias, password.toCharArray());
            readStream.close();
            return key;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Creates a Cipher with ENCRYPT_MODE initiated. */
    public Cipher getEncrypter(SecretKey key, String encryptionType) {
        try {
            Cipher encrypter = Cipher.getInstance(encryptionType);
            encrypter.init(Cipher.ENCRYPT_MODE, key);

            return encrypter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Creates a Cipher with DECRYPT_MODE initiated. */
    public Cipher getDecrypter(SecretKey key, String encryptionType) {
        try {
            Cipher decrypter = Cipher.getInstance(encryptionType);
            decrypter.init(Cipher.DECRYPT_MODE, key);

            return decrypter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}