package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.flow.FlowScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.bjm.collections.Access;
import org.bjm.collections.LokSabha;
import org.bjm.collections.State;
import org.bjm.collections.User;
import org.bjm.collections.VidhanSabha;
import org.bjm.dtos.UserDto;
import org.bjm.utils.BjmConstants;
import org.bjm.utils.ConvertPngToJpg;
import org.bjm.utils.ImageUtil;
import org.bjm.utils.ImageVO;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bjm.ejbs.BjManchEmailEjbLocal;

/**
 *
 * @author singh
 */
@Named(value = "userRegisterMBean")
@FlowScoped(value = "UserRegister")
public class UserRegisterMBean implements Serializable{
    
    private static final Logger LOGGER = Logger.getLogger(UserRegisterMBean.class.getName());
    
    private UserDto userDto;
    private Part profileImage;
    
    @Inject
    private BjManchEmailEjbLocal emailEjbLocal;
    
    @PostConstruct
    public void init(){
        userDto= new UserDto();
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<State> stateColl=mongoDatabase.getCollection("State", State.class);
        Iterable<State> stateItrble=stateColl.find();
        Iterator<State> stateItr=stateItrble.iterator();
        userDto.setAllStates(new ArrayList<>());
        State dummy=new State();
        dummy.setCode("--");
        dummy.setName("--Select One--");
        userDto.getAllStates().add(dummy);
        while(stateItr.hasNext()){
            userDto.getAllStates().add(stateItr.next());
        }
        LOGGER.info(String.format("UserDto initialided with count of State : %d", userDto.getAllStates().size()));
    
    }
    
    public void constituencyListener(AjaxBehaviorEvent event){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<LokSabha> lokSabhaColl=mongoDatabase.getCollection("LokSabha", LokSabha.class);
        Bson filter=Filters.eq("stateCode", userDto.getStateCode());
        Iterable<LokSabha> lokSabhaItrble=lokSabhaColl.find(filter);
        Iterator<LokSabha> lokSabhaItr=lokSabhaItrble.iterator();
        userDto.setLokSabhas(new ArrayList());
        while(lokSabhaItr.hasNext()){
            userDto.getLokSabhas().add(lokSabhaItr.next());
        }
        LOGGER.info(String.format("UserDto initialided with LokSabha count %d for StateCode : %s", userDto.getLokSabhas().size(),userDto.getStateCode()));
        
        MongoCollection<VidhanSabha> vidhanSabhaColl=mongoDatabase.getCollection("VidhanSabha", VidhanSabha.class);
        Iterable<VidhanSabha> vidhanSabhaItrble=vidhanSabhaColl.find(filter);
        Iterator<VidhanSabha> vidhanSabhaItr=vidhanSabhaItrble.iterator();
        userDto.setVidhanSabhas(new ArrayList());
        while(vidhanSabhaItr.hasNext()){
            userDto.getVidhanSabhas().add(vidhanSabhaItr.next());
        }
        LOGGER.info(String.format("UserDto initialided with VidhanSabha count %d for StateCode : %s", userDto.getVidhanSabhas().size(),userDto.getStateCode()));
        
    }
    
    public String processData(){
        //Before doing anything we need to ensure that the User is not registered already
        if(isUserRegisteredAlready()){
            FacesContext.getCurrentInstance().addMessage("email", 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email already registered.","Email already registered."));
            return null;
        }
        //Good to proceed
        //Set State in UserDto
        for(State s: userDto.getAllStates()){
            if(s.getCode().equals(userDto.getStateCode())){
                userDto.setState(s);
                break;
            }
        }
        //Profile File next
        processProfileFile();
        
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        session.setAttribute("userDto", userDto);//Why do we need to put the Dto in Session??
        return "UserRegisterConfirm?faces-redirect=true";
    }
    
    private boolean isUserRegisteredAlready() {
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient =(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry= fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<User> userColl=mongoDatabase.getCollection("User", User.class);
        Bson filter = Filters.eq("email", userDto.getEmail());
        return (userColl.find(filter).first()!=null); 
        /*ServletContext servletContext = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<User> userColl = mongoDatabase.getCollection("User", User.class);
        Bson filter=Filters.eq("email", userDto.getEmail());
        if(userColl.find(filter).first()!=null){
            return true;
        }else{
            return false;
        }*/
    }
    
    private void processProfileFile(){
        BufferedImage profileBufferedImage=null;
        if (profileImage==null){//User did not Upload the Profile file. Make Avatar with the Initials
            try{
                char[] chars = new char[2];
                String imageSizeStr=FacesContext.getCurrentInstance().getExternalContext().getInitParameter("imageSize");
                String imageFormat=FacesContext.getCurrentInstance().getExternalContext().getInitParameter("imageFormat");
                int imageSize=Integer.parseInt(imageSizeStr);
                chars[0]= userDto.getFirstName().charAt(0);
                chars[1]= userDto.getLastName().charAt(0);
                String text=new String(chars);
                profileBufferedImage=ImageUtil.drawIcon(imageSize, text);
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                ImageIO.write(profileBufferedImage, imageFormat, baos);
                baos.flush();
                byte[] jpgData=baos.toByteArray();
                baos.close();
                userDto.setProfileFile(text+"."+imageFormat);
                userDto.setImage(jpgData);
            }catch(IOException ex){
                LOGGER.severe(ex.getMessage());
            }
        
        }else{
            try{
                InputStream inputStream=profileImage.getInputStream();
                int imageSize=(int)profileImage.getSize();
                if(imageSize > (1024*1000)){
                    FacesContext.getCurrentInstance().addMessage("profileImage", 
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Profile Image size exceeds 1MB.", "Profile Image size exceeds 1MB."));
                }else{
                    String fullFileName=profileImage.getSubmittedFileName();
                    //String fileType=fullFileName.substring(fullFileName.indexOf('.'));
                    byte[] imageData=new byte[inputStream.available()];
                    inputStream.read(imageData);
                    userDto.setProfileFile(fullFileName);
                    userDto.setImage(imageData);
                    /*byte[] jpgData=null;
                    if(fileType.equals("png")){//convert to jpg first. Jelastic' OpenJDK doen not handle png images well and throw exception.
                        byte[] pngData=new byte[inputStream.available()];
                        jpgData = ConvertPngToJpg.convertToJpg(pngData);
                        userDto.setProfileFile(fullFileName);
                        userDto.setImage(jpgData);
                    }else{
                        byte[] imageData=new byte[inputStream.available()];
                        inputStream.read(imageData);
                        userDto.setProfileFile(fullFileName);
                        userDto.setImage(imageData);
                    }*/
                
                }
            }catch(IOException ex){
                LOGGER.severe(ex.getMessage());
                throw new RuntimeException(ex.getMessage());
            }
        
        }
        //Set ImageVO in the Session
        String imgType=userDto.getProfileFile().substring(userDto.getProfileFile().indexOf('.')+1);
        ImageVO userImageVO=new ImageVO(imgType, userDto.getImage());
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession(true);
        session.setAttribute(BjmConstants.USER_IMAGE, userImageVO);
    }
    
    public String amendUser(){
        return "/flowreturns/UserRegister?faces-redirect=true";
    }
    
    private void submitUser(){
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setGender(userDto.getGender());
        user.setDob(LocalDate.parse(userDto.getDob(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        user.setMobile(userDto.getMobile());
        user.setPhone(userDto.getPhone());
        user.setStateName(userDto.getState().getName());
        user.setLokSabha(userDto.getLokSabha());
        user.setVidhanSabha(userDto.getVidhanSabha());
        user.setCreatedOn(LocalDateTime.now());
        user.setUpdatedOn(LocalDateTime.now());
        
        Access access = new Access();
        access.setEmail(userDto.getEmail());
        access.setProfileFile(userDto.getProfileFile());
        access.setImage(new Binary(userDto.getImage()));
        access.setFailedAttempts(0);
        access.setCreatedOn(LocalDateTime.now());
        access.setUpdatedOn(LocalDateTime.now());
        
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient =(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry= fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<User> userColl=mongoDatabase.getCollection("User", User.class);
        InsertOneResult userResult= userColl.insertOne(user);
        MongoCollection<Access> accessColl=mongoDatabase.getCollection("Access", Access.class);
        InsertOneResult accessResult=accessColl.insertOne(access);
        
        emailEjbLocal.sendUserRegisteredEmail(access);
        LOGGER.info(String.format("User created with ID %s along with Access %s", userResult.getInsertedId(),accessResult.getInsertedId()));
    }
    
    public String getReturnValue(){
        submitUser();
        return "/flowreturns/UserRegister-return?faces-redirect=true";
    }

    public UserDto getUserDto() {
        return userDto;
    }

    public void setUserDto(UserDto userDto) {
        this.userDto = userDto;
    }

    public Part getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Part profileImage) {
        this.profileImage = profileImage;
    }

    private void sendUserRegisteredEmail(Access access) {
        ServletContext servletContext= (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
        Session mailSession=(Session) servletContext.getAttribute("mailSession");
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(access.getEmail()).append("</h2>");
        htmlMsg.append("<p>Congratulations on registering yourself successfully!!").append(".</p>");
        htmlMsg.append("<p>As a final step, please create your account password by following the link below:</p>");
        String createAccessURI=FacesContext.getCurrentInstance().getExternalContext().getInitParameter("createAccessURI");
        String createAccess=String.format(createAccessURI, access.getEmail());
        String webURI = FacesContext.getCurrentInstance().getExternalContext().getRealPath(("/"));
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
    
    
    
    
}
