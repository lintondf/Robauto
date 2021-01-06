
package com.example;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TurnByTurnAction implements Serializable
{

    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("length")
    @Expose
    private Integer length;
    @SerializedName("offset")
    @Expose
    private Integer offset;
    @SerializedName("currentRoad")
    @Expose
    private CurrentRoad currentRoad;
    @SerializedName("nextRoad")
    @Expose
    private NextRoad nextRoad;
    @SerializedName("direction")
    @Expose
    private String direction;
    @SerializedName("severity")
    @Expose
    private String severity;
    private final static long serialVersionUID = -2214989185480899024L;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public CurrentRoad getCurrentRoad() {
        return currentRoad;
    }

    public void setCurrentRoad(CurrentRoad currentRoad) {
        this.currentRoad = currentRoad;
    }

    public NextRoad getNextRoad() {
        return nextRoad;
    }

    public void setNextRoad(NextRoad nextRoad) {
        this.nextRoad = nextRoad;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

}
