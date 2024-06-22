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
import org.bjm.collections.ForumComment;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.types.ObjectId;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
/**
 *
 * @author singh
 */
@Named(value = "reportAbuseForumMBean")
@ViewScoped
public class ReportAbuseForumMBean implements Serializable{
    
    private static final Logger LOGGER = Logger.getLogger(ReportAbuseForumMBean.class.getName());
    
    private ForumComment forumComment;
    private ForumAbuse forumAbuse;
    
    @PostConstruct
    public void init(){
        forumAbuse=new ForumAbuse();
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String commentId=request.getParameter("commentId");
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<ForumComment> forumCommentColl=mongoDatabase.getCollection("ForumComment", ForumComment.class);
        Bson filter=Filters.eq("_id", new ObjectId(commentId));
        forumComment=forumCommentColl.find(filter).first();
        forumAbuse.setForumCommentId(new ObjectId(commentId));
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute("access");
        forumAbuse.setReportedByAccessId(access.getId());
        forumAbuse.setReportedByEmail(access.getEmail());
        LOGGER.info("ForumAbuse initialised.");
    }
    
    public String reportAbuse(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<ForumAbuse> forumAbuseColl=mongoDatabase.getCollection("ForumAbuse", ForumAbuse.class);
        forumAbuse.setReportedOn(LocalDateTime.now());
        InsertOneResult forumAbuseResult=forumAbuseColl.insertOne(forumAbuse);
        LOGGER.info(String.format("ForumAbuse created with ID: %s", forumAbuseResult.getInsertedId()));
        FacesContext.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_INFO, "Abuse reported", "Abuse reported"));
        return null;
    }

    public ForumComment getForumComment() {
        return forumComment;
    }

    public void setForumComment(ForumComment forumComment) {
        this.forumComment = forumComment;
    }

    
    public ForumAbuse getForumAbuse() {
        return forumAbuse;
    }

    public void setForumAbuse(ForumAbuse forumAbuse) {
        this.forumAbuse = forumAbuse;
    }
    
    
    
}
