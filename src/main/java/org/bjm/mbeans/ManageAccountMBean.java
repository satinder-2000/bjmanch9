package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.collections.LokSabha;
import org.bjm.collections.State;
import org.bjm.collections.User;
import org.bjm.collections.VidhanSabha;
import org.bjm.dtos.AccessDto;
import org.bjm.dtos.UserDto;
import org.bjm.utils.ConvertPngToJpg;
import org.bjm.utils.PasswordUtil;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bjm.ejbs.BjManchEmailEjbLocal;

/**
 *
 * @author singh
 */
@Named(value = "manageAccountMBean")
@SessionScoped
public class ManageAccountMBean implements Serializable {
    
    private static final Logger LOGGER =Logger.getLogger(ManageAccountMBean.class.getName());
    
    private AccessDto accessDto;
    private UserDto userDto;
    private User user;
    
    private Part profileImage;
    
    @PostConstruct 
    public void init(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access)session.getAttribute("access");
        accessDto=new AccessDto();
        accessDto.setEmail(access.getEmail());
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<User> userColl=mongoDatabase.getCollection("User", User.class);
        Bson filter=Filters.eq("email", access.getEmail());
        user=userColl.find(filter).first();
        userDto=new UserDto();
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setMobile(user.getMobile());
        userDto.setPhone(user.getPhone());
        userDto.setStateName(user.getStateName());
        userDto.setLokSabha(user.getLokSabha());
        userDto.setVidhanSabha(user.getVidhanSabha());
    }
    
    public String changePasswordReq(){
        return "/home/changePassword?faces-redirect=true";
    }
    
    public String changePersonalDetailsReq(){
        return "/home/changePersonalDetails?faces-redirect=true";
    }
    
    public String changeProfileImageReq(){
        return "/home/changeProfileImage?faces-redirect=true";
    }
    
    public String submitNewPassword(){
        if (!accessDto.getPassword().equals(accessDto.getConfirmPassword())){
            FacesContext.getCurrentInstance().addMessage("password", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords do not match","Passwords do not match"));
            return null;
        }else{
            ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
            MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB"));
            HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            HttpSession session=request.getSession();
            Access access=(Access)session.getAttribute("access");
            MongoCollection<Document> accessColl=mongoDatabase.getCollection("Access");
            Document query = new Document().append("_id",  access.getId());
            Bson updates=Updates.combine(
                    Updates.set("createdOn", access.getCreatedOn()),
                    Updates.set("email", access.getEmail()),
                    Updates.set("failedAttempts", access.getFailedAttempts()),
                    Updates.set("image", access.getImage()),
                    Updates.set("password", PasswordUtil.generateSecurePassword(accessDto.getPassword(), access.getEmail())),
                    Updates.set("profileFile", access.getProfileFile()),
                    Updates.set("updatedOn", LocalDateTime.now())
            );
            UpdateOptions options=new UpdateOptions().upsert(true);
            
            UpdateResult result = accessColl.updateOne(query, updates, options);
            LOGGER.info(String.format(" For Access ID %s the Upserted ID is %s",access.getId(),result.getUpsertedId()));
            FacesContext.getCurrentInstance().addMessage("password", new FacesMessage(FacesMessage.SEVERITY_INFO, "Password changed successfully","Password changed successfully"));
            return null;
        }
    }
    
    public String changeStateReq(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<State> stateColl=mongoDatabase.getCollection("State", State.class);
        userDto.setAllStates(new ArrayList<>());
        State dummy=new State();
        dummy.setCode("--");
        dummy.setName("--Select One--");
        userDto.getAllStates().add(dummy);
        Iterable<State> stateItrble=stateColl.find();
        Iterator<State> stateItr=stateItrble.iterator();
        while(stateItr.hasNext()){
            userDto.getAllStates().add(stateItr.next());
        }
        return "/home/changeState?faces-redirect=true";        
    }
    
    public void constituencyListener(AjaxBehaviorEvent event){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<LokSabha> lokSabhaColl=mongoDatabase.getCollection("LokSabha", LokSabha.class);
        Bson filter=Filters.eq("stateCode", userDto.getStateCode());
        //While we are here let's set the stateName in UserDto
        for(State state:userDto.getAllStates()){
            if(userDto.getStateCode().equals(state.getCode())){
                userDto.setStateName(state.getName());
                break;
            }
           
        }    
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
    
    public String submitNewState(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access)session.getAttribute("access");
        MongoCollection<Document> userCollDoc=mongoDatabase.getCollection("User");
        Document query = new Document().append("email",  access.getEmail());
        List<State> allStates= userDto.getAllStates();
        for(State state: allStates){
            if (state.getCode().equals(userDto.getStateCode())){
                userDto.setStateName(state.getName());
                break;
            }
        }
        Bson updates=Updates.combine(
                Updates.set("_id", user.getId()),
                Updates.set("createdOn", user.getCreatedOn()),
                Updates.set("dob", user.getDob()),
                Updates.set("email", user.getEmail()),
                Updates.set("firstName", user.getFirstName()),
                Updates.set("gender", user.getGender()),
                Updates.set("lastName", user.getLastName()),
                Updates.set("profileFile", access.getProfileFile()),
                Updates.set("stateName", userDto.getStateName()),
                Updates.set("lokSabha", userDto.getLokSabha()),
                Updates.set("vidhanSabha", userDto.getVidhanSabha()),
                Updates.set("mobile", user.getMobile()),
                Updates.set("phone", user.getPhone()),
                Updates.set("updatedOn", LocalDateTime.now())
            );
            UpdateOptions options=new UpdateOptions().upsert(true);
            
            UpdateResult result = userCollDoc.updateOne(query, updates, options);
            LOGGER.info(String.format(" For User ID %s the Upserted ID is %s",user.getId(),result.getUpsertedId()));
            FacesContext.getCurrentInstance().addMessage("password", new FacesMessage(FacesMessage.SEVERITY_INFO, "State details changed successfully","State details changed successfully"));
            return null;
    }
    
    public String changePersonalDetails(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access)session.getAttribute("access");
        MongoCollection<Document> userCollDoc=mongoDatabase.getCollection("User");
        Document query = new Document().append("email",  access.getEmail());
        Bson updates=Updates.combine(
                    Updates.set("_id", user.getId()),
                Updates.set("createdOn", user.getCreatedOn()),
                Updates.set("dob", user.getDob()),
                Updates.set("email", user.getEmail()),
                Updates.set("gender", user.getGender()),
                Updates.set("profileFile", access.getProfileFile()),
                Updates.set("stateCode", user.getStateName()),
                Updates.set("lokSabha", user.getLokSabha()),
                Updates.set("vidhanSabha", user.getVidhanSabha()),
                Updates.set("firstName", userDto.getFirstName()),
                Updates.set("lastName", userDto.getLastName()),
                Updates.set("mobile", userDto.getMobile()),
                Updates.set("phone", userDto.getPhone()),
                Updates.set("updatedOn", LocalDateTime.now())
            );
            UpdateOptions options=new UpdateOptions().upsert(true);
            
            UpdateResult result = userCollDoc.updateOne(query, updates, options);
            LOGGER.info(String.format(" For User ID %s the Upserted ID is %s",user.getId(),result.getUpsertedId()));
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, "Account details updated successfully","Account details updated successfully"));
            return null;
        
    }
    
    public String changeProfileImage() {
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Access> accessColl = mongoDatabase.getCollection("Access", Access.class);
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute("access");
        Bson filter = Filters.eq("email", access.getEmail());
        Access accessDb = accessColl.find(filter).first();

        try {
            InputStream inputStream = profileImage.getInputStream();
            int imageSize = (int) profileImage.getSize();
            if (imageSize > (1024 * 1000)) {
                FacesContext.getCurrentInstance().addMessage("profileImage",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Profile Image size exceeds 1MB.", "Profile Image size exceeds 1MB."));
            } else {
                String fullFileName = profileImage.getSubmittedFileName();
                String fileType = fullFileName.substring(fullFileName.indexOf('.'));
                byte[] imageData = new byte[inputStream.available()];
                inputStream.read(imageData);
                //userDto.setProfileFile(fullFileName);
                //userDto.setImage(imageData);
                access.setProfileFile(fullFileName);
                access.setImage(new Binary(imageData));
                accessDb.setProfileFile(access.getProfileFile());
                accessDb.setImage(access.getImage());
                /*byte[] jpgData = null;
                if (fileType.equals("png")) {//convert to jpg first. Jelastic' OpenJDK doen not handle png images well and throw exception.
                    byte[] pngData = new byte[inputStream.available()];
                    jpgData = ConvertPngToJpg.convertToJpg(pngData);
                    userDto.setProfileFile(fullFileName);
                    userDto.setImage(jpgData);
                    access.setProfileFile(userDto.getProfileFile());
                    access.setImage(new Binary(userDto.getImage()));
                    accessDb.setProfileFile(access.getProfileFile());
                    accessDb.setImage(access.getImage());
                } else {
                    byte[] imageData = new byte[inputStream.available()];
                    inputStream.read(imageData);
                    userDto.setProfileFile(fullFileName);
                    userDto.setImage(imageData);
                    access.setProfileFile(userDto.getProfileFile());
                    access.setImage(new Binary(userDto.getImage()));
                    accessDb.setProfileFile(access.getProfileFile());
                    accessDb.setImage(access.getImage());
                }*/
                MongoCollection<Document> accessCollDoc = mongoDatabase.getCollection("Access");
                Document query = new Document().append("email", access.getEmail());
                Bson updates = Updates.combine(
                        Updates.set("_id", accessDb.getId()),
                        Updates.set("createdOn", accessDb.getCreatedOn()),
                        Updates.set("email", accessDb.getEmail()),
                        Updates.set("password", accessDb.getPassword()),
                        Updates.set("profileFile", accessDb.getProfileFile()),
                        Updates.set("image", accessDb.getImage()),
                        Updates.set("updatedOn", LocalDateTime.now())
                );
                UpdateOptions options = new UpdateOptions().upsert(true);

                UpdateResult result = accessCollDoc.updateOne(query, updates, options);
                LOGGER.info(String.format(" For Access ID %s the Upserted ID is %s", accessDb.getId(), result.getUpsertedId()));
                FacesContext.getCurrentInstance().addMessage("profileImage", new FacesMessage(FacesMessage.SEVERITY_INFO, "Profile Image updated successfully", "Profile Image updated successfully"));

            }
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }

        return null;

    }
        
        

    public AccessDto getAccessDto() {
        return accessDto;
    }

    public void setAccessDto(AccessDto accessDto) {
        this.accessDto = accessDto;
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

    

    
    
    
    
}
