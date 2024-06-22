package org.bjm.collections;

import java.time.LocalDateTime;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class BlogAbuse {
    
    private ObjectId _id;
    private ObjectId blogCommentId;
    private ObjectId reportedByAccessId;
    private String reportedByEmail;
    private String reportText;
    private LocalDateTime reportedOn;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getBlogCommentId() {
        return blogCommentId;
    }

    public void setBlogCommentId(ObjectId blogCommentId) {
        this.blogCommentId = blogCommentId;
    }

    public ObjectId getReportedByAccessId() {
        return reportedByAccessId;
    }

    public void setReportedByAccessId(ObjectId reportedByAccessId) {
        this.reportedByAccessId = reportedByAccessId;
    }

    public String getReportedByEmail() {
        return reportedByEmail;
    }

    public void setReportedByEmail(String reportedByEmail) {
        this.reportedByEmail = reportedByEmail;
    }

    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        this.reportText = reportText;
    }

    public LocalDateTime getReportedOn() {
        return reportedOn;
    }

    public void setReportedOn(LocalDateTime reportedOn) {
        this.reportedOn = reportedOn;
    }
    
    
}
