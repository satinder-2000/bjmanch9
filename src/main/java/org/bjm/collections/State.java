package org.bjm.collections;

import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class State {
    
    private ObjectId _id;
    private String code;
    private String name;
    private String postCodePrefix;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostCodePrefix() {
        return postCodePrefix;
    }

    public void setPostCodePrefix(String postCodePrefix) {
        this.postCodePrefix = postCodePrefix;
    }
    
    
    
}
