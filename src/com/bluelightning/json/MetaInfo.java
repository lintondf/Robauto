
package com.bluelightning.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class MetaInfo implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("mapVersion")
    @Expose
    private String mapVersion;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("moduleVersion")
    @Expose
    private String moduleVersion;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("interfaceVersion")
    @Expose
    private String interfaceVersion;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("availableMapVersion")
    @Expose
    private List<Object> availableMapVersion = new ArrayList<Object>();
    private final static long serialVersionUID = 5736599888183846045L;

    /**
     * 
     * (Required)
     * 
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getMapVersion() {
        return mapVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setMapVersion(String mapVersion) {
        this.mapVersion = mapVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getModuleVersion() {
        return moduleVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getInterfaceVersion() {
        return interfaceVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setInterfaceVersion(String interfaceVersion) {
        this.interfaceVersion = interfaceVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    public List<Object> getAvailableMapVersion() {
        return availableMapVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setAvailableMapVersion(List<Object> availableMapVersion) {
        this.availableMapVersion = availableMapVersion;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("timestamp", timestamp).append("mapVersion", mapVersion).append("moduleVersion", moduleVersion).append("interfaceVersion", interfaceVersion).append("availableMapVersion", availableMapVersion).toString();
    }

}
