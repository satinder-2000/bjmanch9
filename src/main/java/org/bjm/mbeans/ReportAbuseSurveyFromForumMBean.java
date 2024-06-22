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
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.collections.SurveyFromForumAbuse;
import org.bjm.collections.SurveyFromForumVote;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
@Named(value = "reportAbuseSurveyFromForumMBean")
@ViewScoped
public class ReportAbuseSurveyFromForumMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(ReportAbuseSurveyFromForumMBean.class.getName());
    
    private SurveyFromForumVote  surveyFromForumVote;
    private SurveyFromForumAbuse surveyFromForumAbuse;
    
    @PostConstruct
    public void init(){
        surveyFromForumAbuse=new SurveyFromForumAbuse();
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String voteId=request.getParameter("voteId");
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        Bson filter=Filters.eq("_id", new ObjectId(voteId));
        MongoCollection<SurveyFromForumVote> surveyFromForumVoteColl=mongoDatabase.getCollection("SurveyFromForumVote", SurveyFromForumVote.class);
        surveyFromForumVote=surveyFromForumVoteColl.find(filter).first();
        surveyFromForumAbuse=new SurveyFromForumAbuse();
        surveyFromForumAbuse.setSurveyFromForumVoteId(new ObjectId(voteId));
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute("access");
        surveyFromForumAbuse.setReportedByAccessId(access.getId());
        surveyFromForumAbuse.setReportedByEmail(access.getEmail());
        LOGGER.info("SurveyAbuse initialised.");
    }
    
    public String reportAbuse(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<SurveyFromForumAbuse> surveyFromForumAbuseColl=mongoDatabase.getCollection("SurveyFromForumAbuse", SurveyFromForumAbuse.class);
        surveyFromForumAbuse.setReportedOn(LocalDateTime.now());
        InsertOneResult surveyFromForumAbuseResult=surveyFromForumAbuseColl.insertOne(surveyFromForumAbuse);
        LOGGER.info(String.format("SurveyFromForumAbuse created with ID: %s", surveyFromForumAbuseResult.getInsertedId()));
        FacesContext.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_INFO, "Abuse reported", "Abuse reported"));
        return null;
    }

    public SurveyFromForumVote getSurveyFromForumVote() {
        return surveyFromForumVote;
    }

    public void setSurveyFromForumVote(SurveyFromForumVote surveyFromForumVote) {
        this.surveyFromForumVote = surveyFromForumVote;
    }

    public SurveyFromForumAbuse getSurveyFromForumAbuse() {
        return surveyFromForumAbuse;
    }

    public void setSurveyFromForumAbuse(SurveyFromForumAbuse surveyFromForumAbuse) {
        this.surveyFromForumAbuse = surveyFromForumAbuse;
    }
    
    
    
    
}
