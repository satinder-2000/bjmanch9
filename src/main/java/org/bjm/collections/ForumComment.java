package org.bjm.collections;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class ForumComment {
    
    private ObjectId _id;
    @Size(min = 2, max = 2500)
    private String comment;
    private LocalDateTime dated;
    private ObjectId forumId;
    private String forumCommenterEmail;
    private ObjectId forumCommenterAccessId;

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

    public ObjectId getForumId() {
        return forumId;
    }

    public void setForumId(ObjectId forumId) {
        this.forumId = forumId;
    }

    public String getForumCommenterEmail() {
        return forumCommenterEmail;
    }

    public void setForumCommenterEmail(String forumCommenterEmail) {
        this.forumCommenterEmail = forumCommenterEmail;
    }

    

    public ObjectId getForumCommenterAccessId() {
        return forumCommenterAccessId;
    }

    public void setForumCommenterAccessId(ObjectId forumCommenterAccessId) {
        this.forumCommenterAccessId = forumCommenterAccessId;
    }
    
    
}
