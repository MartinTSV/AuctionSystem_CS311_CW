import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.crypto.SecretKey;
import java.security.KeyStore;

/* The purpose of this class is to provide the necessary methods for encryption and decryption. */

public class KeyManager {

    public void StoreToKeyStore(SecretKey keyToStore, String password, String filepath, String alias) {
        try {
            File file = new File(filepath);
            KeyStore javaKeyStore = KeyStore.getInstance("JCEKS");
            if (!file.exists()) {
                javaKeyStore.load(null, null);
            } else {
                javaKeyStore.load(new FileInputStream(file), password.toCharArray());
            }

            javaKeyStore.setKeyEntry(alias, keyToStore, password.toCharArray(), null);
            OutputStream writeStream = new FileOutputStream(filepath);
            javaKeyStore.store(writeStream, password.toCharArray());
            writeStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

// public static SecretKey generateKey(String encryptionType) {
// try {
// KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionType);
// SecretKey myKey = keyGenerator.generateKey();
// return myKey;
// } catch (Exception e) {
// e.printStackTrace();
// }
// return null;
// }

// public static byte[] encryptString(String dataToEncrypt, SecretKey myKey,
// Cipher cipher) {
// try {
// byte[] text = dataToEncrypt.getBytes(UNICODE_FORMAT);
// cipher.init(Cipher.ENCRYPT_MODE, myKey);
// byte[] textEncrypted = cipher.doFinal(text);
// return textEncrypted;

// } catch (Exception e) {
// e.printStackTrace();
// }
// return null;
// }

// public static String decryptString(byte[] dataToDecrypt, SecretKey myKey,
// Cipher cipher) {
// try {
// cipher.init(Cipher.DECRYPT_MODE, myKey);
// byte[] textDecrypted = cipher.doFinal(dataToDecrypt);
// String result = new String(textDecrypted);
// return result;
// } catch (Exception e) {
// e.printStackTrace();
// }
// return null;
// }
// }