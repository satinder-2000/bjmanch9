package org.bjm.collections;

import java.time.LocalDateTime;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class LokSabhaNominate {
    
    private ObjectId _id;
    private String candidateName;
    private String constituency;
    private String stateCode;
    private ObjectId nominatedByAccessId;
    private String nominatedByEmail;
    private LocalDateTime nominatedOn;
    private int nominationCount;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getConstituency() {
        return constituency;
    }

    public void setConstituency(String constituency) {
        this.constituency = constituency;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public ObjectId getNominatedByAccessId() {
        return nominatedByAccessId;
    }

    public void setNominatedByAccessId(ObjectId nominatedByAccessId) {
        this.nominatedByAccessId = nominatedByAccessId;
    }

    public String getNominatedByEmail() {
        return nominatedByEmail;
    }

    public void setNominatedByEmail(String nominatedByEmail) {
        this.nominatedByEmail = nominatedByEmail;
    }

    public LocalDateTime getNominatedOn() {
        return nominatedOn;
    }

    public void setNominatedOn(LocalDateTime nominatedOn) {
        this.nominatedOn = nominatedOn;
    }

    public int getNominationCount() {
        return nominationCount;
    }

    public void setNominationCount(int nominationCount) {
        this.nominationCount = nominationCount;
    }
    
    
    
    
}
