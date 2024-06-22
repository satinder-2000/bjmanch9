package org.bjm.mbeans;

import jakarta.inject.Named;

/**
 *
 * @author singh
 */
@Named(value = "tempMBean")
public class TempMBean {
    
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    
    
}
