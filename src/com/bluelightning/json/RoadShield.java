
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class RoadShield implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("region")
    @Expose
    private String region;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("category")
    @Expose
    private String category;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("label")
    @Expose
    private String label;
    private final static long serialVersionUID = 850059940584576910L;

    /**
     * 
     * (Required)
     * 
     */
    public String getRegion() {
        return region;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getCategory() {
        return category;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getLabel() {
        return label;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("region", region).append("category", category).append("label", label).toString();
    }

}
