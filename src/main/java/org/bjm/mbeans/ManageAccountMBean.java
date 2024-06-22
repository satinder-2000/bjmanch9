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
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import org.bjm.utils.PasswordUtil;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

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
    
    @PostConstruct 
   public void init(){
       
    
    }
    
    
    
    public String changePasswordReq(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access)session.getAttribute("access");
        accessDto=new AccessDto();
        accessDto.setEmail(access.getEmail());
        return "/home/changePassword?faces-redirect=true";
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
        userDto= new UserDto();
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
        MongoCollection<User> userColl = mongoDatabase.getCollection("User",User.class);
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access)session.getAttribute("access");
        Bson filter=Filters.eq("email", access.getEmail());
        User user= userColl.find(filter).first();
        MongoCollection<Document> userCollDoc=mongoDatabase.getCollection("User");
        Document query = new Document().append("email",  access.getEmail());
        Bson updates=Updates.combine(
                    Updates.set("_id", user.getId()),
                Updates.set("createdOn", user.getCreatedOn()),
                Updates.set("dob", user.getDob()),
                Updates.set("email", user.getEmail()),
                Updates.set("firstName", user.getFirstName()),
                Updates.set("gender", user.getGender()),
                Updates.set("lastName", user.getLastName()),
                Updates.set("profileFile", access.getProfileFile()),
                Updates.set("stateCode", userDto.getStateCode()),
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

    

    
    
    
    
}
