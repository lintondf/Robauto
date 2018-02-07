
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Note implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("type")
    @Expose
    private String type;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("code")
    @Expose
    private String code;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("text")
    @Expose
    private String text;
    private final static long serialVersionUID = 5276429519214570300L;

    /**
     * 
     * (Required)
     * 
     */
    public String getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getCode() {
        return code;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getText() {
        return text;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("code", code).append("text", text).toString();
    }

}
