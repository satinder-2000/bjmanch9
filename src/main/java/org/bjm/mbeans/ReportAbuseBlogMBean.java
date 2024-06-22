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
import org.bjm.collections.BlogAbuse;
import org.bjm.collections.BlogComment;
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
@Named(value = "reportAbuseBlogMBean")
@ViewScoped
public class ReportAbuseBlogMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(ReportAbuseBlogMBean.class.getName());
    
    private BlogComment blogComment;
    private BlogAbuse blogAbuse;
    
    @PostConstruct
    public void init(){
        blogAbuse=new BlogAbuse();
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String commentId=request.getParameter("commentId");
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<BlogComment> blogCommentColl=mongoDatabase.getCollection("BlogComment", BlogComment.class);
        Bson filter=Filters.eq("_id", new ObjectId(commentId));
        blogComment=blogCommentColl.find(filter).first();
        blogAbuse.setBlogCommentId(new ObjectId(commentId));
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute("access");
        blogAbuse.setReportedByAccessId(access.getId());
        blogAbuse.setReportedByEmail(access.getEmail());
        LOGGER.info("ForumAbuse initialised.");
    
    }
    
    public String reportAbuse(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<BlogAbuse> blogAbuseColl=mongoDatabase.getCollection("BlogAbuse", BlogAbuse.class);
        blogAbuse.setReportedOn(LocalDateTime.now());
        InsertOneResult blogAbuseResult=blogAbuseColl.insertOne(blogAbuse);
        LOGGER.info(String.format("BlogAbuse created with ID: %s", blogAbuseResult.getInsertedId()));
        FacesContext.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_INFO, "Abuse reported", "Abuse reported"));
        return null;
    }
    

    public BlogComment getBlogComment() {
        return blogComment;
    }

    public void setBlogComment(BlogComment blogComment) {
        this.blogComment = blogComment;
    }

    public BlogAbuse getBlogAbuse() {
        return blogAbuse;
    }

    public void setBlogAbuse(BlogAbuse blogAbuse) {
        this.blogAbuse = blogAbuse;
    }
    
    
    
}
