
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class ValidityPeriod implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("from")
    @Expose
    private String from;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("until")
    @Expose
    private String until;
    private final static long serialVersionUID = 2117679347866254898L;

    /**
     * 
     * (Required)
     * 
     */
    public String getFrom() {
        return from;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getUntil() {
        return until;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setUntil(String until) {
        this.until = until;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("from", from).append("until", until).toString();
    }

}
