package org.bjm.dtos;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author singh
 */
public class SurveyDto {
    
    private String categoryType;
    private String categorySubType;
    @Size(min = 5, max=125)
    private String title;
    private String description;
    private Map<String, Set<String>> surveyCategoryMap;
    private Set<String> categoryTypes;
    private Set<String> categorySubTypes;

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

    public Map<String, Set<String>> getSurveyCategoryMap() {
        return surveyCategoryMap;
    }

    public void setSurveyCategoryMap(Map<String, Set<String>> surveyCategoryMap) {
        this.surveyCategoryMap = surveyCategoryMap;
    }

    public Set<String> getCategoryTypes() {
        return categoryTypes;
    }

    public void setCategoryTypes(Set<String> categoryTypes) {
        this.categoryTypes = categoryTypes;
    }

    public Set<String> getCategorySubTypes() {
        return categorySubTypes;
    }

    public void setCategorySubTypes(Set<String> categorySubTypes) {
        this.categorySubTypes = categorySubTypes;
    }

    
}
