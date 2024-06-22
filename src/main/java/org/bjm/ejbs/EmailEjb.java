package org.bjm.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.URLName;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.collections.Forum;
import org.bjm.collections.LokSabhaNominate;
import org.bjm.collections.Survey;
import org.bjm.collections.SurveyFromForum;
import org.bjm.collections.User;
import org.bjm.collections.VidhanSabhaNominate;

/**
 *
 * @author singh
 */
@Stateless
public class EmailEjb implements EmailEjbLocal {
    
    private static final Logger LOGGER= Logger.getLogger(EmailEjb.class.getName());
    
    @Resource(mappedName = "java:comp/env/mail/bjm")//Tomee
    //@Resource(lookup = "mail/bjm")//Glassfish
    private Session mailSession;
    
    @Resource(name = "webURI")
    private String webURI;
    
    @Resource(name = "createAccessURI")
    private String createAccessURI;
    
    @Resource(name = "forumCreatedURI")
    private String forumCreatedURI;
    
    @Resource(name = "surveyCreatedURI")
    private String surveyCreatedURI;
    
    @Resource(name = "surveyCreatedFromForumURI")
    private String surveyCreatedFromForumURI;
    
    @PostConstruct
    public void init(){
        String username=mailSession.getProperty("mail.smtp.user");
        String password=mailSession.getProperty("mail.smtp.password");
        
        final URLName url= new URLName(mailSession.getProperty("mail.transport.mail"), mailSession.getProperty("mail.smtp.host"),
        -1, null, username, null);
        
        mailSession.setPasswordAuthentication(url, new PasswordAuthentication(username, password));
        LOGGER.info("MailSession set successfully!!");
    }
    

    @Override
    public void sendUserRegisteredEmail(Access access) {
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(access.getEmail()).append("</h2>");
        htmlMsg.append("<p>Congratulations on registering yourself successfully!!").append(".</p>");
        htmlMsg.append("<p>As a final step, please create your account password by following the link below:</p>");
        String createAccess=String.format(createAccessURI, access.getEmail());
        htmlMsg.append("<a href=\"").append(webURI).append(createAccess).append("\">")
                .append(webURI).append(createAccess)
                .append("</a>");
        
        htmlMsg.append("<p>Best Wishes, <br/>www.bjmanch.in Admin</p>");
        htmlMsg.append("</body></html>");
        MimeBodyPart htmlPart=new MimeBodyPart();
        try{
            htmlPart.setContent(htmlMsg.toString(), "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(access.getEmail()));
            mimeMessage.setContent(multipart);
            mimeMessage.setSubject("User Registration");
            Transport.send(mimeMessage);
            LOGGER.info("Email sent successfully....");
        
        }catch(MessagingException ex){
            LOGGER.severe(ex.getMessage());
        }
    }

    @Override
    public void sendAccessCreatedEmail(Access access) {
        MimeMessage mimeMessage= new MimeMessage(mailSession);
        Multipart multipart= new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(access.getEmail()).append("</h2>");
        htmlMsg.append("<p>Congratulations on completing your access details successfully!!").append(".</p>");
        htmlMsg.append("<p>You may now proceed to the website and login to your account.</p>");
        htmlMsg.append("<a href=\"").append(webURI).append("\">")
                .append(webURI)
                .append("</a>");
        htmlMsg.append("<p>Best Wishes, <br/>www.bjmanch.in Admin</p>");
        htmlMsg.append("</body></html>");
        MimeBodyPart htmlPart = new MimeBodyPart();
        try{
            htmlPart.setContent( htmlMsg.toString(), "text/html; charset=utf-8" );
            multipart.addBodyPart(htmlPart);
            mimeMessage.setRecipient(Message.RecipientType.TO,new InternetAddress(access.getEmail()));
            mimeMessage.setContent(multipart);
            mimeMessage.setSubject("Welcome. Access Confirmed!!");
            Transport.send(mimeMessage);
            LOGGER.info("Email sent successfully....");
        }catch(MessagingException ex){
            LOGGER.severe(ex.getMessage());
        }
    }

    @Override
    public void sendForumCreatedEmail(Access access, Forum forum) {
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(access.getEmail()).append("</h2>");
        htmlMsg.append("<p>Congratulations on creating a new Forum successfully!!").append(".</p>");
        htmlMsg.append("<p>You may wish to view your Forum at the link provided below:</p>");
        String forumCreated=String.format(forumCreatedURI, forum.getId().toString(), access.getEmail());
        htmlMsg.append("<a href=\"").append(webURI).append(forumCreated).append("\">")
                .append(webURI).append(forumCreated)
                .append("</a>");
        //htmlMsg.append("<p>"+accessCreate+"</p>");
        htmlMsg.append("<p>Best Wishes, <br/>www.bjmanch.in Admin</p>");
        htmlMsg.append("</body></html>");
        MimeBodyPart htmlPart = new MimeBodyPart();
        try {
            htmlPart.setContent( htmlMsg.toString(), "text/html; charset=utf-8" );
            multipart.addBodyPart(htmlPart);
            mimeMessage.setRecipient(Message.RecipientType.TO,new InternetAddress(access.getEmail()));
            mimeMessage.setContent(multipart);
            mimeMessage.setSubject("Forum Created");
            Transport.send(mimeMessage);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    @Override
    public void sendSurveyCreatedEmail(Access access, Survey survey) {
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(access.getEmail()).append("</h2>");
        htmlMsg.append("<p>Congratulations on creating a new Survey successfully!!").append(".</p>");
        htmlMsg.append("<p>You may wish to view your Survey at the link provided below:</p>");
        String surveyCreated = String.format(surveyCreatedURI, survey.getId().toString(), access.getEmail());
        htmlMsg.append("<a href=\"").append(webURI).append(surveyCreated).append("\">")
                .append(webURI).append(surveyCreated)
                .append("</a>");
        //htmlMsg.append("<p>"+accessCreate+"</p>");
        htmlMsg.append("<p>Best Wishes, <br/>www.bjmanch.in Admin</p>");
        htmlMsg.append("</body></html>");
        MimeBodyPart htmlPart = new MimeBodyPart();
        try {
            htmlPart.setContent(htmlMsg.toString(), "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(access.getEmail()));
            mimeMessage.setContent(multipart);
            mimeMessage.setSubject("Survey Created");
            Transport.send(mimeMessage);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }
    
    @Override
    public void sendSurveyCreatedFromForumEmail(Access access, SurveyFromForum surveyFromForum) {
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(access.getEmail()).append("</h2>");
        htmlMsg.append("<p>Congratulations on creating a new Survey From Forum successfully!!").append(".</p>");
        htmlMsg.append("<p>You may wish to view your Survey From Forum at the link provided below:</p>");
        String surveyCreated = String.format(surveyCreatedFromForumURI, surveyFromForum.getId().toString(), access.getEmail());
        htmlMsg.append("<a href=\"").append(webURI).append(surveyCreated).append("\">")
                .append(webURI).append(surveyCreated)
                .append("</a>");
        //htmlMsg.append("<p>"+accessCreate+"</p>");
        htmlMsg.append("<p>Best Wishes, <br/>www.bjmanch.in Admin</p>");
        htmlMsg.append("</body></html>");
        MimeBodyPart htmlPart = new MimeBodyPart();
        try {
            htmlPart.setContent(htmlMsg.toString(), "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(access.getEmail()));
            mimeMessage.setContent(multipart);
            mimeMessage.setSubject("Survey From Forum Created");
            Transport.send(mimeMessage);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

    @Override
    public void sendNewLokSabhaNominationEmail(User user, LokSabhaNominate lokSabhaNominate) {
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(user.getEmail()).append("</h2>");
        htmlMsg.append("<p>You have successfully nominated new Candidate ").append(lokSabhaNominate.getCandidateName()).append(" for your Constituency ").append(user.getLokSabha());
        htmlMsg.append("<p>Best Wishes, <br/>www.bjmanch.in Admin</p>");
        htmlMsg.append("</body></html>");
        MimeBodyPart htmlPart = new MimeBodyPart();
        try {
            htmlPart.setContent(htmlMsg.toString(), "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            mimeMessage.setContent(multipart);
            mimeMessage.setSubject("New Lok Sabha Nomination");
            Transport.send(mimeMessage);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    @Override
    public void sendLokSabhaReNominationEmail(User user, LokSabhaNominate lokSabhaNominate) {
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(user.getEmail()).append("</h2>");
        htmlMsg.append("<p>You have successfully nominated Candidate ").append(lokSabhaNominate.getCandidateName()).append(" for your Constituency ").append(user.getLokSabha());
        htmlMsg.append("<p>Best Wishes, <br/>www.bjmanch.in Admin</p>");
        htmlMsg.append("</body></html>");
        MimeBodyPart htmlPart = new MimeBodyPart();
        try {
            htmlPart.setContent(htmlMsg.toString(), "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            mimeMessage.setContent(multipart);
            mimeMessage.setSubject("Lok Sabha Nomination");
            Transport.send(mimeMessage);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    @Override
    public void sendNewVidhanSabhaNominationEmail(User user, VidhanSabhaNominate vidhanSabhaNominate) {
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(user.getEmail()).append("</h2>");
        htmlMsg.append("<p>You have successfully nominated new Candidate ").append(vidhanSabhaNominate.getCandidateName()).append(" for your Constituency ").append(user.getVidhanSabha());
        htmlMsg.append("<p>Best Wishes, <br/>www.bjmanch.in Admin</p>");
        htmlMsg.append("</body></html>");
        MimeBodyPart htmlPart = new MimeBodyPart();
        try {
            htmlPart.setContent(htmlMsg.toString(), "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            mimeMessage.setContent(multipart);
            mimeMessage.setSubject("New Vidhan Sabha Nomination");
            Transport.send(mimeMessage);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    @Override
    public void sendVidhanSabhaReNominationEmail(User user, VidhanSabhaNominate vidhanSabhaNominate) {
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(user.getEmail()).append("</h2>");
        htmlMsg.append("<p>You have successfully nominated Candidate ").append(vidhanSabhaNominate.getCandidateName()).append(" for your Constituency ").append(user.getVidhanSabha());
        htmlMsg.append("<p>Best Wishes, <br/>www.bjmanch.in Admin</p>");
        htmlMsg.append("</body></html>");
        MimeBodyPart htmlPart = new MimeBodyPart();
        try {
            htmlPart.setContent(htmlMsg.toString(), "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            mimeMessage.setContent(multipart);
            mimeMessage.setSubject("Vidhan Sabha Nomination");
            Transport.send(mimeMessage);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    @Override
    public void sendContactUsEmail(String adminEmail, String userEmail, String subject, String message) {
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(adminEmail).append("</h2>");
        htmlMsg.append("<p>Please address the following feedback from ").append(userEmail).append(" on urgent basis.");
        htmlMsg.append("<br/><p>").append(message).append("</p><br/>");
        htmlMsg.append("<p>Best Wishes, <br/>www.bjmanch.in Admin</p>");
        htmlMsg.append("</body></html>");
        MimeBodyPart htmlPart = new MimeBodyPart();
        try {
            htmlPart.setContent(htmlMsg.toString(), "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(adminEmail));
            mimeMessage.setRecipient(Message.RecipientType.CC, new InternetAddress(userEmail));
            mimeMessage.setContent(multipart);
            mimeMessage.setSubject(subject);
            Transport.send(mimeMessage);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    
    
    
}
