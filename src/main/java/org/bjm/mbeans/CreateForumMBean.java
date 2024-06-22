package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.flow.FlowScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.collections.Activity;
import org.bjm.collections.ActivityType;
import org.bjm.collections.Forum;
import org.bjm.collections.ForumCategory;
import org.bjm.dtos.ForumDto;
import org.bjm.ejbs.EmailEjbLocal;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;

/**
 *
 * @author singh
 */
@Named(value = "createForumMBean")
@FlowScoped(value = "CreateForum")
public class CreateForumMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(CreateForumMBean.class.getName());
    
    private ForumDto forumDto;
    @Inject
    private ActivityMBean activityMBean;
    @Inject        
    private EmailEjbLocal emailEjbLocal;
    
    private int forumDescriptionChars;
    
    @PostConstruct
    public void init(){
        forumDto = new ForumDto();
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient =(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider= PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<ForumCategory> forumCatColl=mongoDatabase.getCollection("ForumCategory", ForumCategory.class);
        Iterable<ForumCategory> forumCatItrble=forumCatColl.find();
        Iterator<ForumCategory> forumCatItr = forumCatItrble.iterator();
        forumDto.setForumCategoryMap(new HashMap<>());
        forumDto.getForumCategoryMap().put("--Select One--", null);
        while(forumCatItr.hasNext()){
            ForumCategory fc=forumCatItr.next();
            Set<String> mapKey=forumDto.getForumCategoryMap().keySet();
            if(!mapKey.contains(fc.getType())){
                Set<String> valueSet=new HashSet<>();
                valueSet.add("--Select One--");
                valueSet.add(fc.getSubType());
                forumDto.getForumCategoryMap().put(fc.getType(), valueSet);
            }else{
                Set<String> valueSet=forumDto.getForumCategoryMap().get(fc.getType());
                valueSet.add(fc.getSubType());
            }
        }
        forumDto.setCategoryTypes(forumDto.getForumCategoryMap().keySet());
        //forumDescriptionChars=Integer.parseInt(servletContext.getInitParameter("forumDescriptionChars"));
        LOGGER.info(String.format("ForumDto forumCatgoryMap populated with %d records", forumDto.getForumCategoryMap().size()));
    }
    
    public void ajaxTypeListener(AjaxBehaviorEvent abe){
        forumDto.setCategorySubTypes(forumDto.getForumCategoryMap().get(forumDto.getCategoryType()));
    }
    
    public String prepareForum(){
        return "CreateForumConfirm?faces-redirect=true";
    }
    
    public String amendForum(){
        return "CreateForum?faces-redirect=true";
    }
    
    private  void submitForum(){
        Forum forum=new Forum();
        forum.setCategoryType(forumDto.getCategoryType());
        forum.setCategorySubType(forumDto.getCategorySubType());
        forum.setTitle(forumDto.getTitle());
        forum.setDescription(forumDto.getDescription());
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute("access");
        forum.setForumCreatorEmail(access.getEmail());
        forum.setCreatedOn(LocalDateTime.now());
        forum.setUpdatedOn(LocalDateTime.now());
                
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient= (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Forum> forumColl=mongoDatabase.getCollection("Forum", Forum.class);
        InsertOneResult forumResult=forumColl.insertOne(forum);
        LOGGER.info(String.format("Forum created with ID: %s",forumResult.getInsertedId()));
        //Activity now
        Activity activity=new Activity();
        activity.setActivityType(ActivityType.FORUM_CREATED.toString());
        activity.setActivityId(forumResult.getInsertedId().asObjectId().getValue());
        activity.setDescription("Forum Created "+forum.getTitle());
        activity.setOwnerEmail(access.getEmail());
        activity.setCreatedOn(LocalDateTime.now());
        activityMBean.addActivity(activity);
        //Last Step - send Email
        emailEjbLocal.sendForumCreatedEmail(access, forum);
        forumDto=new ForumDto();//To erase the previous data as we are working in a session.
    }
    
    public String getReturnValue(){
        submitForum();
        return "/flowreturns/CreateForum-return?faces-redirect=true";
    }
    
    public ForumDto getForumDto() {
        return forumDto;
    }

    public void setForumDto(ForumDto forumDto) {
        this.forumDto = forumDto;
    }

    public int getForumDescriptionChars() {
        return forumDescriptionChars;
    }

    public void setForumDescriptionChars(int forumDescriptionChars) {
        this.forumDescriptionChars = forumDescriptionChars;
    }
    
    
    
    
    
    
}
