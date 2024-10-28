package org.bjm.tests;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;

/**
 *
 * @author singh
 */
public class KeystoreDemo {
    
    public static void main(String[] args){
        try{
            KeyStore ks=KeyStore.getInstance(KeyStore.getDefaultType());
            char[] password= new String("Wether@lDr69").toCharArray();
            File keystoreFile=new File("/home/singh/nb-ws/bjmanch9/encrypt/BJMKeyStore.jks");
            FileInputStream fis=new FileInputStream(keystoreFile);
            ks.load(fis, password);
            
            KeyStore.ProtectionParameter protParameter= new KeyStore.PasswordProtection(password);
            //get the PrivateKey
            KeyStore.PrivateKeyEntry pKEntry=(KeyStore.PrivateKeyEntry) ks.getEntry("bjmanch.org", protParameter);
            PrivateKey myPrivateKey=pKEntry.getPrivateKey();
            String pKReextract=new String(myPrivateKey.getEncoded(),StandardCharsets.UTF_8);
            
            System.out.println(pKReextract);
            
            
            
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    
    
    
}
