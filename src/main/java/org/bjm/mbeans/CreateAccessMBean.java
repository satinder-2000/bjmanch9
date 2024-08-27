package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.dtos.AccessDto;
import org.bjm.utils.PasswordUtil;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bjm.ejbs.BjManchEmailEjbLocal;

/**
 *
 * @author singh
 */
@Named(value = "createAccessMBean")
@ViewScoped
public class CreateAccessMBean implements Serializable {
    
    private static final Logger LOGGER=Logger.getLogger(CreateAccessMBean.class.getName());
    
    private AccessDto accessDto;
    private Access access;
    
    @Inject
    private BjManchEmailEjbLocal emailEjbLocal;
    
    @PostConstruct
    public void init(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Access> accessCol=mongoDatabase.getCollection("Access", Access.class);
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        Bson filter=Filters.eq("email", request.getParameter("email"));
        access = accessCol.find(filter).first();
        accessDto=new AccessDto();
        accessDto.setEmail(access.getEmail());
        LOGGER.info(String.format("Access loaded for email %s", request.getParameter("email")));
    }
    
    public String createAccess(){
        if(!accessDto.getPassword().equals(accessDto.getConfirmPassword())){
            FacesContext.getCurrentInstance().addMessage("password", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Passwords not matching","Passwords not matching"));
            return null;
        }
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Access> accessColl=mongoDatabase.getCollection("Access", Access.class);
        String encodedPw=PasswordUtil.generateSecurePassword(accessDto.getPassword(), accessDto.getEmail());
        access.setPassword(encodedPw);
        access.setUpdatedOn(LocalDateTime.now());
        Bson filter= Filters.eq("email", accessDto.getEmail());
        accessColl.replaceOne(filter, access);
        LOGGER.info(String.format("Password created for Id: %s!!", access.getId()));
        FacesContext.getCurrentInstance().addMessage("password",new FacesMessage(FacesMessage.SEVERITY_INFO, "Password set successfully.","Password set successfully."));
        emailEjbLocal.sendAccessCreatedEmail(access);
        return null;
    }

    public AccessDto getAccessDto() {
        return accessDto;
    }

    public void setAccessDto(AccessDto accessDto) {
        this.accessDto = accessDto;
    }
    
    
    
    
    
}
