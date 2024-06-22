package org.bjm.collections;

import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class LokSabha {
    
    private ObjectId _id;
    private String stateCode;
    private String constituency;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getConstituency() {
        return constituency;
    }

    public void setConstituency(String constituency) {
        this.constituency = constituency;
    }
    
    
    
}
