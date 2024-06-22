package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.InsertOneResult;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.bjm.collections.Activity;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;

/**
 *
 * @author singh
 */
@Named(value = "activityMBean")
@ApplicationScoped
public class ActivityMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(ActivityMBean.class.getName());
    private List<Activity> activityList;
    private String activityListSize;
    
    @PostConstruct
    public void init(){
        activityList = new ArrayList<>();
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Activity> activityColl=mongoDatabase.getCollection("Activity", Activity.class);
        activityListSize=servletContext.getInitParameter("activityListSize");
        Iterable<Activity> activityItrble = activityColl.find().sort(Sorts.descending("createdOn")).limit(Integer.parseInt(activityListSize));
        Iterator<Activity> activityItr=activityItrble.iterator();
        while(activityItr.hasNext()){
            activityList.add(activityItr.next());
        }
        
    }
    
    public void addActivity(Activity activity){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Activity> activityColl=mongoDatabase.getCollection("Activity", Activity.class);
        InsertOneResult activityResult = activityColl.insertOne(activity);
        LOGGER.info(String.format("Activity created with ID: %s", activityResult.getInsertedId()));
        int maxSize=Integer.parseInt(activityListSize);
        if (activityList.size()==maxSize){//accomodate new one at the expense of the oldest record - at the bottom of the List
            activityList.remove(0);
        }
        activityList.add(activity);
    }

    public List<Activity> getActivityList() {
        return activityList;
    }

    public void setActivityList(List<Activity> activityList) {
        this.activityList = activityList;
    }
    
    
    
    
}
