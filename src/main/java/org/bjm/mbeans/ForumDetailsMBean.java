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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.collections.Forum;
import org.bjm.collections.ForumComment;
import org.bjm.collections.User;
import org.bjm.utils.BjmConstants;
import org.bjm.utils.ImageVO;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;

/**
 *
 * @author singh
 */
@Named(value = "forumDetailsMBean")
@ViewScoped
public class ForumDetailsMBean implements Serializable{
    
    private static final Logger LOGGER=Logger.getLogger(ForumDetailsMBean.class.getName());
    private int commentChars;
    private Forum forum;
    private String userComment;
    private List<ForumComment> otherForumComments;
    
    @PostConstruct
    public void init(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String forumIdStr=request.getParameter("forumId");
        ObjectId forumId=new ObjectId(forumIdStr);
        Bson filter=Filters.eq("_id", forumId);
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Forum> forumColl=mongoDatabase.getCollection("Forum", Forum.class);
        forum=forumColl.find(filter).first();
        LOGGER.info(String.format("Forum with ID: %s Loaded successfully.", forum.getId()));
        MongoCollection<Access> accessColl=mongoDatabase.getCollection("Access", Access.class);
        Bson filterAccess=Filters.eq("email", forum.getForumCreatorEmail());
        Access forumCreator=accessColl.find(filterAccess).first();
        HttpSession session=request.getSession();
        session.setAttribute(BjmConstants.FORUM_CREATOR_ACCESS, forumCreator);
    }
    
    public String postForumComment(){
        if(userComment==null || userComment.trim().isEmpty()){
           FacesContext.getCurrentInstance().addMessage("usercomment", new FacesMessage(FacesMessage.SEVERITY_ERROR, "No comment entered", "No comment entered"));
        }else{
            ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
            MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
            ForumComment forumComment = new ForumComment();
            forumComment.setComment(userComment);
            forumComment.setForumId(forum.getId());
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            HttpSession session = request.getSession();
            Access access = (Access) session.getAttribute("access");
            forumComment.setForumCommenterAccessId(access.getId());
            Bson filter = Filters.eq("email", access.getEmail());
            MongoCollection<User> userColl = mongoDatabase.getCollection("User", User.class);
            User user = userColl.find(filter).first();
            forumComment.setForumCommenterEmail(user.getEmail());
            forumComment.setDated(LocalDateTime.now());
            MongoCollection<ForumComment> forumCommentColl = mongoDatabase.getCollection("ForumComment", ForumComment.class);
            InsertOneResult fcResult = forumCommentColl.insertOne(forumComment);
            LOGGER.info(String.format("New ForumComment added with ID: %s", fcResult.getInsertedId()));
            FacesContext.getCurrentInstance().addMessage("usercomment", new FacesMessage(FacesMessage.SEVERITY_INFO, "Comment added successfully!!", "Comment added successfully!!"));
        }
        return null;
    }
    
    private void loadOtherForumComments(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        Bson filter=Filters.eq("forumId", forum.getId());
        MongoCollection<ForumComment> forumCommentColl = mongoDatabase.getCollection("ForumComment", ForumComment.class);
        Iterable<ForumComment> forumCommentItrble=forumCommentColl.find(filter);
        Iterator<ForumComment> forumCommentItr = forumCommentItrble.iterator();
        otherForumComments=new ArrayList<>();
        Map<ObjectId, ImageVO> forumCommenterImageMap=new HashMap<>();
        while(forumCommentItr.hasNext()){
            ForumComment fc=forumCommentItr.next();
            otherForumComments.add(fc);
            Bson filterAccess=Filters.eq("email", fc.getForumCommenterEmail());
            MongoCollection<Access> accessColl=mongoDatabase.getCollection("Access", Access.class);
            Access forumCommenterAccess=accessColl.find(filterAccess).first();
            String imageType=forumCommenterAccess.getProfileFile().substring(forumCommenterAccess.getProfileFile().indexOf('.')+1);
            ImageVO forumCommenterImageVO=new ImageVO(imageType, forumCommenterAccess.getImage().getData());
            forumCommenterImageMap.put(forumCommenterAccess.getId(), forumCommenterImageVO);        
        }
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        session.setAttribute(BjmConstants.FORUM_COMMENTER_IMAGE_MAP, forumCommenterImageMap);
        LOGGER.info(String.format("Count of other Forum Comments for Forum ID: %s is : 5d", forum.getId(), otherForumComments.size()));
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public int getCommentChars() {
        return commentChars;
    }

    public void setCommentChars(int commentChars) {
        this.commentChars = commentChars;
    }
    
    public List<ForumComment> getOtherForumComments(){
        loadOtherForumComments();
        return otherForumComments;
    }
    
    public void setOtherForumComments(List<ForumComment> otherForumComments){
        this.otherForumComments=this.otherForumComments;
    }
    
    
    
    
}
