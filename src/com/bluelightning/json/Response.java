
package com.bluelightning.json;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Response implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("metaInfo")
    @Expose
    private MetaInfo metaInfo;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("route")
    @Expose
    private Set<Route> route = new LinkedHashSet<Route>();
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("language")
    @Expose
    private String language;
    private final static long serialVersionUID = -8846461420655306522L;

    /**
     * 
     * (Required)
     * 
     */
    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setMetaInfo(MetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Set<Route> getRoute() {
        return route;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setRoute(Set<Route> route) {
        this.route = route;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getLanguage() {
        return language;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("metaInfo", metaInfo).append("route", route).append("language", language).toString();
    }

}
