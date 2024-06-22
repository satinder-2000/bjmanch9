package org.bjm.utils;

import java.io.Serializable;

/**
 *
 * @author singh
 */
public class ImageVO implements Serializable {
    
    private String imgType;
    
    public byte[] image;
    
    public ImageVO(String imgType,byte[] image){
        this.imgType=imgType;
        this.image=image;
        
    }

    public String getImgType() {
        return imgType;
    }

    public void setImgType(String imgType) {
        this.imgType = imgType;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
    
 }