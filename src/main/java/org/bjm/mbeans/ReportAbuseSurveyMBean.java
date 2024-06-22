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
import org.bjm.collections.ForumAbuse;
import org.bjm.collections.SurveyAbuse;
import org.bjm.collections.SurveyVote;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;


/**
 *
 * @author singh
 */
@Named(value = "reportAbuseSurveyMBean")
@ViewScoped
public class ReportAbuseSurveyMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(ReportAbuseSurveyMBean.class.getName());
    
    private SurveyVote surveyVote;
    private SurveyAbuse surveyAbuse;
    
    @PostConstruct
    public void init(){
        surveyAbuse=new SurveyAbuse();
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String voteId=request.getParameter("voteId");
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        Bson filter=Filters.eq("_id", new ObjectId(voteId));
        MongoCollection<SurveyVote> surveyVoteColl=mongoDatabase.getCollection("SurveyVote", SurveyVote.class);
        surveyVote=surveyVoteColl.find(filter).first();
        surveyAbuse=new SurveyAbuse();
        surveyAbuse.setSurveyVoteId(new ObjectId(voteId));
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute("access");
        surveyAbuse.setReportedByAccessId(access.getId());
        surveyAbuse.setReportedByEmail(access.getEmail());
        LOGGER.info("SurveyAbuse initialised.");
    }
    
    public String reportAbuse(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<SurveyAbuse> surveyAbuseColl=mongoDatabase.getCollection("SurveyAbuse", SurveyAbuse.class);
        surveyAbuse.setReportedOn(LocalDateTime.now());
        InsertOneResult surveyAbuseResult=surveyAbuseColl.insertOne(surveyAbuse);
        LOGGER.info(String.format("SurveyAbuse created with ID: %s", surveyAbuseResult.getInsertedId()));
        FacesContext.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_INFO, "Abuse reported", "Abuse reported"));
        return null;
    }

    public SurveyVote getSurveyVote() {
        return surveyVote;
    }

    public void setSurveyVote(SurveyVote surveyVote) {
        this.surveyVote = surveyVote;
    }

    public SurveyAbuse getSurveyAbuse() {
        return surveyAbuse;
    }

    public void setSurveyAbuse(SurveyAbuse surveyAbuse) {
        this.surveyAbuse = surveyAbuse;
    }
    
    
    
    
    
}
