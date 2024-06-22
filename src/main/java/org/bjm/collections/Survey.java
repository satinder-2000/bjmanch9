package org.bjm.collections;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class Survey {
    
    private ObjectId _id;
    private String surveyCreatorEmail;
    private String categoryType;
    private String categorySubType;
    @Size(min = 5, max = 125)
    private String title;
    private String description;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public String getSurveyCreatorEmail() {
        return surveyCreatorEmail;
    }

    public void setSurveyCreatorEmail(String surveyCreatorEmail) {
        this.surveyCreatorEmail = surveyCreatorEmail;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getCategorySubType() {
        return categorySubType;
    }

    public void setCategorySubType(String categorySubType) {
        this.categorySubType = categorySubType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
    
    
    
}
