package org.bjm.utils;

/**
 *
 * @author singh
 */
public class PieChartData {
    
    private int agreeCt;
    private int disagreeCt;
    private int unconfirmedCt;

    public PieChartData(int agreeCt, int disagreeCt, int unconfirmedCt) {
        this.agreeCt = agreeCt;
        this.disagreeCt = disagreeCt;
        this.unconfirmedCt = unconfirmedCt;
    }

    public int getAgreeCt() {
        return agreeCt;
    }

    public void setAgreeCt(int agreeCt) {
        this.agreeCt = agreeCt;
    }

    public int getDisagreeCt() {
        return disagreeCt;
    }

    public void setDisagreeCt(int disagreeCt) {
        this.disagreeCt = disagreeCt;
    }

    public int getUnconfirmedCt() {
        return unconfirmedCt;
    }

    public void setUnconfirmedCt(int unconfirmedCt) {
        this.unconfirmedCt = unconfirmedCt;
    }

    
    
    
    
}
