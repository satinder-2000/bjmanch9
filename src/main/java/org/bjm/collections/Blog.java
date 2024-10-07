package org.bjm.collections;

import java.time.LocalDateTime;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class Blog {
    
    private ObjectId _id;
    private LocalDateTime publishedOn;
    private String title;
    private String summary;
    private String text;
    private String publishedByAccessId;
    private String publishedByEmail;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public LocalDateTime getPublishedOn() {
        return publishedOn;
    }

    public void setPublishedOn(LocalDateTime publishedOn) {
        this.publishedOn = publishedOn;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPublishedByAccessId() {
        return publishedByAccessId;
    }

    public void setPublishedByAccessId(String publishedByAccessId) {
        this.publishedByAccessId = publishedByAccessId;
    }

    public String getPublishedByEmail() {
        return publishedByEmail;
    }

    public void setPublishedByEmail(String publishedByEmail) {
        this.publishedByEmail = publishedByEmail;
    }
    
    
}
