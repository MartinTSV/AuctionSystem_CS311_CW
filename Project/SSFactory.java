import java.io.*;
import java.net.*;
import java.rmi.server.*;
import javax.net.ssl.*;
import java.security.KeyStore;
import javax.net.ssl.*;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class SSFactory implements RMIServerSocketFactory{

    private SSLServerSocketFactory ssf = null;

    public SSFactory() throws Exception{
        try{
            SSLContext ctx;
            KeyManagerFactory kmf;
            KeyStore ks;

            char[] password = "password".toCharArray();
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("testkeys"), password);

            kmf = KeyManagerFactory.getInstance("AES");
            kmf.init(ks, password);

            ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), null, null);

            ssf = ctx.getServerSocketFactory();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public ServerSocket createServerSocket(int port) throws IOException{
        return ssf.createServerSocket(port);
    }

    public int hashCode(){
        return getClass().hashCode();
    }

    public boolean equals(Object obj){
        if (obj == this){
            return true;
        }else if (obj == null  || getClass() != obj.getClass()){
            return false;
        }
        return true;
    }
}
