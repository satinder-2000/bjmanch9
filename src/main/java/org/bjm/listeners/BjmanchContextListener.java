package org.bjm.listeners;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.Properties;

/**
 *
 * @author singh
 */
@WebListener
public class BjmanchContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext=sce.getServletContext();
        String connectionString=servletContext.getInitParameter("MONGODB_URL");
        MongoClient mongoClient=MongoClients.create(connectionString);
        servletContext.setAttribute("mongoClient", mongoClient);
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", servletContext.getInitParameter("mailSMTPAuth"));
        prop.put("mail.smtp.starttls.enable", servletContext.getInitParameter("smtpStartTlsEnabled"));
        prop.put("mail.smtp.host", servletContext.getInitParameter("mailSMTPHost"));
        prop.put("mail.smtp.port", servletContext.getInitParameter("mailSMTPPort"));
        // SSL Factory
        prop.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory"); 
        
        Authenticator auth = new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(servletContext.getInitParameter("mailSMTPUser"),servletContext.getInitParameter("password"));
            }
        };
        Session emailSession=Session.getInstance(prop, auth);
        servletContext.setAttribute("emailSession", emailSession);
    }
    
    
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext servletContext=sce.getServletContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        mongoClient.close();
        Session emailSession=(Session)servletContext.getAttribute("emailSession");
        emailSession=null;
    }

    

    
}
