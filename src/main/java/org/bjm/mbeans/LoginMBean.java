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
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
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
/**
 *
 * @author singh
 */
@Named(value = "loginMBean")
@ViewScoped
public class LoginMBean implements Serializable {
    
    private static final Logger LOGGER=Logger.getLogger(LoginMBean.class.getName());
    private AccessDto accessDto;
    
    @PostConstruct
    public void init(){
        accessDto=new AccessDto();
    }
    
    public String performLogin(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        Bson fiter=Filters.eq("email", accessDto.getEmail());
        MongoCollection<Access> accessColl=mongoDatabase.getCollection("Access", Access.class);
        Access access=accessColl.find(fiter).first();
        String encodedPW=PasswordUtil.generateSecurePassword(accessDto.getPassword(), accessDto.getEmail());
        if(!access.getPassword().equals(encodedPW)){
            FacesContext.getCurrentInstance().addMessage("password",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Incorrect Login details", "Incorrect Login details"));
            return null;
        }else{
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            HttpSession session=request.getSession();
            session.setAttribute("access", access);
            return "/home/userHome?faces-redirect=true";
        }
    }

    public AccessDto getAccessDto() {
        return accessDto;
    }

    public void setAccessDto(AccessDto accessDto) {
        this.accessDto = accessDto;
    }
    
    
}
