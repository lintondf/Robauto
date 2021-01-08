
package com.bluelightning.here;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Place_ implements Serializable
{

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("location")
    @Expose
    private Location_ location;
    @SerializedName("originalLocation")
    @Expose
    private OriginalLocation_ originalLocation;
    private final static long serialVersionUID = -5282499458816745353L;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Location_ getLocation() {
        return location;
    }

    public void setLocation(Location_ location) {
        this.location = location;
    }

    public OriginalLocation_ getOriginalLocation() {
        return originalLocation;
    }

    public void setOriginalLocation(OriginalLocation_ originalLocation) {
        this.originalLocation = originalLocation;
    }

}
