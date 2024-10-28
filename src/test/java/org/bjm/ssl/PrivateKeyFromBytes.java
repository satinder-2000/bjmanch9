package org.bjm.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

/**
 *
 * @author singh
 */
public class PrivateKeyFromBytes {
    
    public static void main(String[] args) throws NoSuchAlgorithmException, FileNotFoundException, IOException, InvalidKeySpecException{
        Path path = Paths.get("/home/singh/nb-ws/bjmanch9/ssl/Prod/Oct-25-24/PrivateKey");
        byte[] privateKeyBytes=Files.readAllBytes(path);
        KeyFactory kf= KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        JcaPEMWriter writer = new JcaPEMWriter(new PrintWriter(System.out));
        writer.writeObject(privateKey);
        writer.close();
       
        
    }
    
}
