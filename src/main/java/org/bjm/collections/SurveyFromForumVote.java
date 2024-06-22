package org.bjm.collections;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class SurveyFromForumVote {
    
    private ObjectId _id;
    private ObjectId surveyFromForumId;
    private String voteType;
    @Size(min = 5, max = 2500)
    private String comment;
    private String voterEmail;
    private ObjectId voterAccessId;
    private LocalDateTime dated;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getSurveyFromForumId() {
        return surveyFromForumId;
    }

    public void setSurveyFromForumId(ObjectId surveyFromForumId) {
        this.surveyFromForumId = surveyFromForumId;
    }

    public String getVoteType() {
        return voteType;
    }

    public void setVoteType(String voteType) {
        this.voteType = voteType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getVoterEmail() {
        return voterEmail;
    }

    public void setVoterEmail(String voterEmail) {
        this.voterEmail = voterEmail;
    }

    public ObjectId getVoterAccessId() {
        return voterAccessId;
    }

    public void setVoterAccessId(ObjectId voterAccessId) {
        this.voterAccessId = voterAccessId;
    }

    public LocalDateTime getDated() {
        return dated;
    }

    public void setDated(LocalDateTime dated) {
        this.dated = dated;
    }
    
    
    
}
