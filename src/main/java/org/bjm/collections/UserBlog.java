package org.bjm.collections;

import java.time.LocalDateTime;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class UserBlog {
    
    private ObjectId _id;
    private LocalDateTime createdOn;
    private String title;
    private String summary;
    private String description;
    private String createdByAccessId;
    private String blogPublishedByEmail;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedByAccessId() {
        return createdByAccessId;
    }

    public void setCreatedByAccessId(String createdByAccessId) {
        this.createdByAccessId = createdByAccessId;
    }

    public String getBlogPublishedByEmail() {
        return blogPublishedByEmail;
    }

    public void setBlogPublishedByEmail(String blogPublishedByEmail) {
        this.blogPublishedByEmail = blogPublishedByEmail;
    }
    
    
    
}
