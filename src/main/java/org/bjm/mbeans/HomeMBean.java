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
import org.bjm.collections.Forum;
import org.bjm.collections.Survey;
import org.bjm.collections.SurveyFromForum;
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
@Named(value = "homeMBean")
@ViewScoped
public class HomeMBean implements Serializable {
    
    private static final Logger LOGGER=Logger.getLogger(HomeMBean.class.getName());
    
    private List<Forum> userForums;
    private List<Survey> userSurveys;
    private List<SurveyFromForum> userSurveysFromForums;
    
    @PostConstruct
    public void init(){
        ServletContext servletContext=(ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries((MongoClientSettings.getDefaultCodecRegistry()), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute("access");
        
        //User Forums
        Bson filterForum=Filters.eq("forumCreatorEmail", access.getEmail());
        MongoCollection<Forum> forumColl=mongoDatabase.getCollection("Forum", Forum.class);
        Iterable<Forum> forumItrble=forumColl.find(filterForum);
        Iterator<Forum> forumItr=forumItrble.iterator();
        userForums = new ArrayList<>();
        while(forumItr.hasNext()){
            userForums.add(forumItr.next());
        }
        LOGGER.info(String.format("Count of Forums by User %s is %d", access.getEmail(), userForums.size()));
        
        //User SurveysForums
        Bson filterSurvey=Filters.eq("surveyCreatorEmail", access.getEmail());
        MongoCollection<Survey> surveyColl=mongoDatabase.getCollection("Survey", Survey.class);
        Iterable<Survey> surveyItrble=surveyColl.find(filterSurvey);
        Iterator<Survey> surveyItr=surveyItrble.iterator();
        userSurveys = new ArrayList<>();
        while(surveyItr.hasNext()){
            userSurveys.add(surveyItr.next());
        }
        LOGGER.info(String.format("Count of Surveys by User %s is %d", access.getEmail(), userSurveys.size()));
        
        
        //User SurveysFromForums
        Bson filterSurveyFromForum=Filters.eq("surveyCreatorEmail", access.getEmail());
        MongoCollection<SurveyFromForum> surveyFromForumColl=mongoDatabase.getCollection("SurveyFromForum", SurveyFromForum.class);
        Iterable<SurveyFromForum> surveyFromForumItrble=surveyFromForumColl.find(filterSurveyFromForum);
        Iterator<SurveyFromForum> surveyFromForumItr=surveyFromForumItrble.iterator();
        userSurveysFromForums = new ArrayList<>();
        while(surveyFromForumItr.hasNext()){
            userSurveysFromForums.add(surveyFromForumItr.next());
        }
        LOGGER.info(String.format("Count of SurveysFromForums by User %s is %d", access.getEmail(), userSurveys.size()));
        
    }
    
    
    
    public String goHome(){
        return "/home/userHome?faces-redirect=true";
    }

    public List<Forum> getUserForums() {
        return userForums;
    }

    public void setUserForums(List<Forum> userForums) {
        this.userForums = userForums;
    }

    public List<Survey> getUserSurveys() {
        return userSurveys;
    }

    public void setUserSurveys(List<Survey> userSurveys) {
        this.userSurveys = userSurveys;
    }

    public List<SurveyFromForum> getUserSurveysFromForums() {
        return userSurveysFromForums;
    }

    public void setUserSurveysFromForums(List<SurveyFromForum> userSurveysFromForums) {
        this.userSurveysFromForums = userSurveysFromForums;
    }
    
    
    
}
