package org.bjm.collections;

import java.time.LocalDateTime;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class BlogComment {
    
    private ObjectId _id;
    private String comment;
    private LocalDateTime dated;
    private ObjectId blogId;
    private String blogCommenterEmail;
    private ObjectId blogCommenterAccessId;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getDated() {
        return dated;
    }

    public void setDated(LocalDateTime dated) {
        this.dated = dated;
    }

    public ObjectId getBlogId() {
        return blogId;
    }

    public void setBlogId(ObjectId blogId) {
        this.blogId = blogId;
    }

    public String getBlogCommenterEmail() {
        return blogCommenterEmail;
    }

    public void setBlogCommenterEmail(String blogCommenterEmail) {
        this.blogCommenterEmail = blogCommenterEmail;
    }

    public ObjectId getBlogCommenterAccessId() {
        return blogCommenterAccessId;
    }

    public void setBlogCommenterAccessId(ObjectId blogCommenterAccessId) {
        this.blogCommenterAccessId = blogCommenterAccessId;
    }
    
    
}
