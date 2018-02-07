
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class TruckRestrictions implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("height")
    @Expose
    private Double height;
    private final static long serialVersionUID = -1489422198664853086L;

    /**
     * 
     * (Required)
     * 
     */
    public Double getHeight() {
        return height;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setHeight(Double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("height", height).toString();
    }

}
