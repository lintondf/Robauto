
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class BoundingBox implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("topLeft")
    @Expose
    private TopLeft topLeft;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("bottomRight")
    @Expose
    private BottomRight bottomRight;
    private final static long serialVersionUID = 2974686900712716576L;

    /**
     * 
     * (Required)
     * 
     */
    public TopLeft getTopLeft() {
        return topLeft;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setTopLeft(TopLeft topLeft) {
        this.topLeft = topLeft;
    }

    /**
     * 
     * (Required)
     * 
     */
    public BottomRight getBottomRight() {
        return bottomRight;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setBottomRight(BottomRight bottomRight) {
        this.bottomRight = bottomRight;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("topLeft", topLeft).append("bottomRight", bottomRight).toString();
    }

}
