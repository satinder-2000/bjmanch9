package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.FlowScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.collections.Activity;
import org.bjm.collections.ActivityType;
import org.bjm.collections.Forum;
import org.bjm.collections.SurveyFromForum;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.bjm.ejbs.BjManchEmailEjbLocal;

/**
 *
 * @author singh
 */
@Named(value = "createSurveyFromForumMBean")
@FlowScoped(value = "CreateSurveyFromForum")
public class CreateSurveyFromForumMBean implements Serializable {
    
    private static final Logger LOGGER= Logger.getLogger(CreateSurveyFromForumMBean.class.getName());
    private Forum forum;
    private SurveyFromForum surveyFromForum;
    
    @Inject
    private ActivityMBean activityMBean;
    @Inject
    private BjManchEmailEjbLocal emailEjbLocal;
    
    @PostConstruct
    public void init(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Forum> forumColl=mongoDatabase.getCollection("Forum", Forum.class);
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String forumId=request.getParameter("forumId");
        ObjectId forumIdObj=new ObjectId(forumId);
        Bson filter=Filters.eq("_id", forumIdObj);
        forum=forumColl.find(filter).first();
        surveyFromForum=new SurveyFromForum();
        surveyFromForum.setCategoryType(forum.getCategoryType());
        surveyFromForum.setCategorySubType(forum.getCategorySubType());
        surveyFromForum.setForumId(forum.getId());
        surveyFromForum.setTitle(forum.getTitle());
        surveyFromForum.setDescription(forum.getDescription());
        LOGGER.info(String.format("Forum loaded with ID: %s", forum.getId().toString()));
    }
    
    public String prepareSurvey(){
        return "CreateSurveyFromForumConfirm?faces-redirect=true";
    }
    
    public String amendSurvey(){
        return "CreateSurveyFromForum?faces-redirect=true";
    }
    
    private void submitSurvey(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session= request.getSession();
        Access access=(Access) session.getAttribute("access");
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<SurveyFromForum> surveyFromForumColl=mongoDatabase.getCollection("SurveyFromForum", SurveyFromForum.class);
        surveyFromForum.setSurveyCreatorEmail(access.getEmail());
        surveyFromForum.setCreatedOn(LocalDateTime.now());
        surveyFromForum.setUpdatedOn(LocalDateTime.now());
        InsertOneResult surveyFromForumResult = surveyFromForumColl.insertOne(surveyFromForum);
        LOGGER.info(String.format("New SurveyFromForum created with ID: %s", surveyFromForumResult.getInsertedId()));
        
        //Activity now
        Activity activity=new Activity();
        activity.setActivityType(ActivityType.SURVEY_FROM_FORUM_CREATED.toString());
        activity.setActivityId(surveyFromForumResult.getInsertedId().asObjectId().getValue());
        activity.setDescription("Survey From Forum Created "+surveyFromForum.getTitle());
        activity.setOwnerEmail(access.getEmail());
        activity.setCreatedOn(LocalDateTime.now());
        activityMBean.addActivity(activity);
        //Last Step - send Email
        emailEjbLocal.sendSurveyCreatedFromForumEmail(access, surveyFromForum);
        surveyFromForum=new SurveyFromForum();//To erase the previous data as we are working in a session.
    }
    
    public String getReturnValue(){
        submitSurvey();
        return "/flowreturns/CreateSurveyFromForum-return?faces-redirect=true";
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public SurveyFromForum getSurveyFromForum() {
        return surveyFromForum;
    }

    public void setSurveyFromForum(SurveyFromForum surveyFromForum) {
        this.surveyFromForum = surveyFromForum;
    }
    
    
    
    
    
}
