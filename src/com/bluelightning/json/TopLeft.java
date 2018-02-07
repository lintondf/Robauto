
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class TopLeft implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("latitude")
    @Expose
    private Double latitude;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    private final static long serialVersionUID = 5089997675986960966L;

    /**
     * 
     * (Required)
     * 
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("latitude", latitude).append("longitude", longitude).toString();
    }

}
