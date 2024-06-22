package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.bjm.collections.Blog;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;

/**
 *
 * @author singh
 */
@Named(value = "blogSummaryMBean")
@ViewScoped
public class BlogSummaryMBean implements Serializable{
    
    private static final Logger LOGGER=Logger.getLogger(BlogSummaryMBean.class.getName());
    
    private List<Blog> allBlogs;
    
    @PostConstruct
    public void init(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry= fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Blog> blogColl=mongoDatabase.getCollection("Blog", Blog.class);
        Iterable<Blog> blogItrble=blogColl.find();
        Iterator<Blog> blogItr=blogItrble.iterator();
        allBlogs=new ArrayList<>();
        while(blogItr.hasNext()){
           allBlogs.add(blogItr.next());
        }
        LOGGER.info(String.format("Count of Blogs loaded : %d", allBlogs.size()));
    }

    public List<Blog> getAllBlogs() {
        return allBlogs;
    }

    public void setAllBlogs(List<Blog> allBlogs) {
        this.allBlogs = allBlogs;
    }

}
