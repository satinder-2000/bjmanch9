package org.bjm.tests;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author singh
 */
public class NameCheapEmailTest {
    
    public static void main(String[] args){
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "mail.privateemail.com");
        prop.put("mail.smtp.port", "587");
        // SSL Factory
        //prop.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory"); 
        
        Authenticator auth = new Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication("contact@bjmanch.org","Wether@lDr69");
            }
        };
        Session session=Session.getInstance(prop, auth);
        
        sendEmail(session, "admin@bjmanch.org", "Howdy", "Jakarta Mail Demo");
        
    }
    
    public static void sendEmail(Session session, String toEmail, String subject, String body){
        try{
            MimeMessage msg = new MimeMessage(session);
            //msg.addHeader("Content-type", "text/html");
            //msg.addHeader("format", "flowed");
            //msg.addHeader("Content-Transfer-Encoding", "8bit");
            
            msg.setFrom(new InternetAddress("contact@bjmanch.org"));
            msg.setReplyTo(InternetAddress.parse("contact@bjmanch.org",false));
            msg.setSubject(subject);
            msg.setContent(body,"text/html");
            msg.setSentDate(new Date());
            
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            System.out.println("Message is ready");
            Transport.send(msg);
            System.out.println("EMail Sent Successfully!!");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
}
