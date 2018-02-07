
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class End implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("linkId")
    @Expose
    private String linkId;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("mappedPosition")
    @Expose
    private MappedPosition mappedPosition;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("originalPosition")
    @Expose
    private OriginalPosition originalPosition;
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
    @SerializedName("spot")
    @Expose
    private Double spot;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("sideOfStreet")
    @Expose
    private String sideOfStreet;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("mappedRoadName")
    @Expose
    private String mappedRoadName;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("label")
    @Expose
    private String label;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("shapeIndex")
    @Expose
    private Double shapeIndex;
    private final static long serialVersionUID = -7235346277649430183L;

    /**
     * 
     * (Required)
     * 
     */
    public String getLinkId() {
        return linkId;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    /**
     * 
     * (Required)
     * 
     */
    public MappedPosition getMappedPosition() {
        return mappedPosition;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setMappedPosition(MappedPosition mappedPosition) {
        this.mappedPosition = mappedPosition;
    }

    /**
     * 
     * (Required)
     * 
     */
    public OriginalPosition getOriginalPosition() {
        return originalPosition;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setOriginalPosition(OriginalPosition originalPosition) {
        this.originalPosition = originalPosition;
    }

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
    public Double getSpot() {
        return spot;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setSpot(Double spot) {
        this.spot = spot;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getSideOfStreet() {
        return sideOfStreet;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setSideOfStreet(String sideOfStreet) {
        this.sideOfStreet = sideOfStreet;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getMappedRoadName() {
        return mappedRoadName;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setMappedRoadName(String mappedRoadName) {
        this.mappedRoadName = mappedRoadName;
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

    /**
     * 
     * (Required)
     * 
     */
    public Double getShapeIndex() {
        return shapeIndex;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setShapeIndex(Double shapeIndex) {
        this.shapeIndex = shapeIndex;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("linkId", linkId).append("mappedPosition", mappedPosition).append("originalPosition", originalPosition).append("type", type).append("spot", spot).append("sideOfStreet", sideOfStreet).append("mappedRoadName", mappedRoadName).append("label", label).append("shapeIndex", shapeIndex).toString();
    }

}
