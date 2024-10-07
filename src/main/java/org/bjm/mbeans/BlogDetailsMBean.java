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
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.collections.Blog;
import org.bjm.collections.BlogComment;
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
@Named(value = "blogDetailsMBean")
@ViewScoped
public class BlogDetailsMBean implements Serializable {
    
    private static final Logger LOGGER=Logger.getLogger(BlogDetailsMBean.class.getName());
    
    private Blog blog;
    private String blogCreatedOn;
    private BlogComment blogComment;
    private List<BlogComment> otherBlogComments;
    
    
    
    @PostConstruct
    public void init(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String blogIdStr=request.getParameter("blogId");
        ObjectId blogId=new ObjectId(blogIdStr);
        Bson filter=Filters.eq("_id", blogId);
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Blog> blogColl=mongoDatabase.getCollection("Blog", Blog.class);
        blog=blogColl.find(filter).first();
        DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        blogCreatedOn = dateTimeFormatter.format(blog.getPublishedOn());
        blogComment=new BlogComment();
    }
    
    public String postComment(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute("access");
        blogComment.setBlogCommenterAccessId(access.getId());
        blogComment.setBlogCommenterEmail(access.getEmail());
        blogComment.setDated(LocalDateTime.now());
        blogComment.setBlogId(blog.getId());
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<BlogComment> blogCommentColl=mongoDatabase.getCollection("BlogComment", BlogComment.class);
        InsertOneResult blogCommentResult= blogCommentColl.insertOne(blogComment);
        LOGGER.info(String.format("BlogComment created with ID: %s", blogCommentResult.getInsertedId()));
        FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, "Comment posted successfully","Comment posted successfully"));
        return null;
    }
    
    private void loadOtherBlogComments(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        Bson filter=Filters.eq("blogId", blog.getId());
        MongoCollection<BlogComment> blogCommentColl=mongoDatabase.getCollection("BlogComment", BlogComment.class);
        Iterable<BlogComment> blogCommentItrble=blogCommentColl.find(filter);
        Iterator<BlogComment> blogCommentItr=blogCommentItrble.iterator();
        otherBlogComments=new ArrayList<>();
        Map<ObjectId, ImageVO> blogCommenterImageMap=new HashMap<>();
        while(blogCommentItr.hasNext()){
            BlogComment bc=blogCommentItr.next();
            otherBlogComments.add(bc);
            Bson filterAccess=Filters.eq("email", bc.getBlogCommenterEmail());
            MongoCollection<Access> accessColl=mongoDatabase.getCollection("Access", Access.class);
            Access blogCommenterAccess=accessColl.find(filterAccess).first();
            String imgType=blogCommenterAccess.getProfileFile().substring(blogCommenterAccess.getProfileFile().indexOf('.')+1);
            ImageVO blogCommenterImageVO=new ImageVO(imgType, blogCommenterAccess.getImage().getData());
            blogCommenterImageMap.put(blogCommenterAccess.getId(), blogCommenterImageVO);
        }
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        session.setAttribute(BjmConstants.BLOG_COMMENTER_IMAGE_MAP, blogCommenterImageMap);
        LOGGER.info(String.format("Count of other Blog Comments for Blog ID: %s is : %d", blog.getId(), otherBlogComments.size()));
    }    

    public String getBlogCreatedOn() {
        return blogCreatedOn;
    }

    public void setBlogCreatedOn(String blogCreatedOn) {
        this.blogCreatedOn = blogCreatedOn;
    }

    
    
    public BlogComment getBlogComment() {
        return blogComment;
    }

    public void setBlogComment(BlogComment blogComment) {
        this.blogComment = blogComment;
    }

    public List<BlogComment> getOtherBlogComments() {
        loadOtherBlogComments();
        return otherBlogComments;
    }

    public void setOtherBlogComments(List<BlogComment> otherBlogComments) {
        this.otherBlogComments = otherBlogComments;
    }
    
    
    
}
