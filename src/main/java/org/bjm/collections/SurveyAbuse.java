package org.bjm.collections;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class SurveyAbuse {
    
    private ObjectId _id;
    private ObjectId surveyVoteId;
    private ObjectId reportedByAccessId;
    private String reportedByEmail;
    @Size(min = 5, max = 2500)
    private String reportText;
    private LocalDateTime reportedOn;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getSurveyVoteId() {
        return surveyVoteId;
    }

    public void setSurveyVoteId(ObjectId surveyVoteId) {
        this.surveyVoteId = surveyVoteId;
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
