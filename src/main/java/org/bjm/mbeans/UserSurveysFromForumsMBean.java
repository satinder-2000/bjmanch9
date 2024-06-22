package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.collections.Survey;
import org.bjm.collections.SurveyFromForum;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

/**
 *
 * @author singh
 */
@Named(value = "userSurveysFromForumsMBean")
@ViewScoped
public class UserSurveysFromForumsMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(UserSurveysFromForumsMBean.class.getName());
    private List<SurveyFromForum> userSurveysFromForums;
    
    @PostConstruct
    public void init(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<SurveyFromForum> surveyFromForumColl=mongoDatabase.getCollection("SurveyFromForum", SurveyFromForum.class);
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access)session.getAttribute("access");
        Bson filter= Filters.eq("surveyCreatorEmail", access.getEmail());
        Iterable<SurveyFromForum> surveyFromForumItrble=surveyFromForumColl.find(filter);
        userSurveysFromForums=new ArrayList<>();
        Iterator<SurveyFromForum> surveyFromForumItr = surveyFromForumItrble.iterator();
        while(surveyFromForumItr.hasNext()){
            userSurveysFromForums.add(surveyFromForumItr.next());
        }
        LOGGER.info(String.format("User %s has %d SurveysFromForums", access.getEmail(), userSurveysFromForums.size()));
    }

    public List<SurveyFromForum> getUserSurveysFromForums() {
        return userSurveysFromForums;
    }

    public void setUserSurveysFromForums(List<SurveyFromForum> userSurveysFromForums) {
        this.userSurveysFromForums = userSurveysFromForums;
    }
    
    
    
    
}
