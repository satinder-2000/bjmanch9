package org.bjm.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 *
 * @author singh
 */
public class BlogDto {
    
    @Size(min = 5, max=125)
    private String title;
    @Size(min = 5, max=250)
    private String summary;
    @NotEmpty
    private String text;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    
}
