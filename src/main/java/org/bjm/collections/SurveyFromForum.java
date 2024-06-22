package org.bjm.collections;

import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class SurveyFromForum extends Survey {
    
    private ObjectId forumId;

    public ObjectId getForumId() {
        return forumId;
    }

    public void setForumId(ObjectId forumId) {
        this.forumId = forumId;
    }
    
}
