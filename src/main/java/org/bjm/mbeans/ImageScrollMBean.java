package org.bjm.mbeans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 *
 * @author singh
 */
@Named(value = "imageScrollMBean")
@SessionScoped
public class ImageScrollMBean implements Serializable {

    private int size = 16;
    private int index = 1;

    private boolean prevDisabled = true;
    private boolean nextDisabled = false;

    public String prev() {
        --index;
        if (index == 1) {
            nextDisabled = false;
            prevDisabled = true;
        } else {
            nextDisabled = false;
            prevDisabled = false;
        }
        return "" + index;
    }

    public String next() {
        ++index;
        if (index >= size) {
            nextDisabled = true;
            prevDisabled = false;
            return "" + index;
        } else {
            nextDisabled = false;
            prevDisabled = false;
            return "" + index;
        }
    }

    public boolean isPrevDisabled() {
        return prevDisabled;
    }

    public void setPrevDisabled(boolean prevDisabled) {
        this.prevDisabled = prevDisabled;
    }

    public boolean isNextDisabled() {
        return nextDisabled;
    }

    public void setNextDisabled(boolean nextDisabled) {
        this.nextDisabled = nextDisabled;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
