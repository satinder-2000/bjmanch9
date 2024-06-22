package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
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
import org.bson.conversions.Bson;

/**
 *
 * @author singh
 */
@Named(value = "searchMBean")
@SessionScoped
public class SearchMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(SearchMBean.class.getName());
    
    private String searchPhrase="";
    private List<Activity> searchResult;
    
    @PostConstruct
    public void init(){
    }
    
    public String processSearch(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider= PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Activity> activityColl=mongoDatabase.getCollection("Activity", Activity.class);
        Bson filter = Filters.text("\""+searchPhrase+"\"");
        activityColl.dropIndexes();
        activityColl.createIndex(Indexes.text("description"));
        Iterable<Activity> activityItrble=activityColl.find(filter);
        Iterator<Activity> activityItr=activityItrble.iterator();
        searchResult = new ArrayList<>();
        while(activityItr.hasNext()){
            searchResult.add(activityItr.next());
        }
        searchPhrase="";
        LOGGER.info(String.format("Count of SearchResult for Phrase %s is %d", searchPhrase, searchResult.size()));
    
        return "searchResult?faces-redirect=true";
    }

    public String getSearchPhrase() {
        return searchPhrase;
    }

    public void setSearchPhrase(String searchPhrase) {
        this.searchPhrase = searchPhrase;
    }

    public List<Activity> getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(List<Activity> searchResult) {
        this.searchResult = searchResult;
    }
    
    
    
    
    
}
