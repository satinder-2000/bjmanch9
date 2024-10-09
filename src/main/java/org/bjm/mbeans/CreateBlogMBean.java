package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.collections.Activity;
import org.bjm.collections.ActivityType;
import org.bjm.collections.Blog;
import org.bjm.collections.Forum;
import org.bjm.collections.UserBlog;
import org.bjm.dtos.BlogDto;
import org.bjm.ejbs.BjManchEmailEjbLocal;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

/**
 *
 * @author singh
 */
@Named(value = "createBlogMBean")
@FlowScoped(value = "CreateBlog")
public class CreateBlogMBean implements Serializable {
    
    private static final Logger LOGGER=Logger.getLogger(CreateBlogMBean.class.getName());
    
    @Inject
    private ActivityMBean activityMBean;
    
    private BlogDto blogDto;
    
    @Inject
    private BjManchEmailEjbLocal emailEjbLocal;
    
    @PostConstruct
    public void init(){
        blogDto=new BlogDto();
        LOGGER.info("new BloDto initialised");
    }
    
    public String prepareBlog(){
        return "CreateBlogConfirm?faces-redirect=true";
    }
    
    public String amendBlog(){
        return "CreateBlog?faces-redirect=true";
    }
    
    private void submitBlog(){
        Blog blog=new Blog();
        blog.setTitle(blogDto.getTitle());
        blog.setText(blogDto.getText());
        HttpServletRequest httpServletRequest=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=httpServletRequest.getSession();
        Access access= (Access)session.getAttribute("access");
        blog.setPublishedByEmail(access.getEmail());
        blog.setPublishedByAccessId(access.getId().toString());
        blog.setPublishedOn(LocalDateTime.now());
        blog.setSummary(blogDto.getSummary());
        
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient= (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Blog> blogColl=mongoDatabase.getCollection("Blog", Blog.class);
        InsertOneResult blogResult=blogColl.insertOne(blog);
        LOGGER.info(String.format("Blog created with ID: %s",blogResult.getInsertedId()));
        //Activity now
        Activity activity=new Activity();
        activity.setActivityType(ActivityType.BLOG_CREATED.toString());
        activity.setActivityId(blogResult.getInsertedId().asObjectId().getValue());
        activity.setDescription("Blog Created "+blog.getTitle());
        activity.setOwnerEmail(access.getEmail());
        activity.setCreatedOn(LocalDateTime.now());
        activityMBean.addActivity(activity);
        //Last Step - send Email
        emailEjbLocal.sendBlogCreatedEmail(access, blog);
        
    }
    
    public String getReturnValue(){
        submitBlog();
        return "/flowreturns/CreateBlog-return?faces-redirect=true";
    }

    public BlogDto getBlogDto() {
        return blogDto;
    }

    public void setBlogDto(BlogDto blogDto) {
        this.blogDto = blogDto;
    }
    
    
    
    
}
