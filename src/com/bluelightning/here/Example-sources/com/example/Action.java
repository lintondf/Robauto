
package com.example;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Action implements Serializable
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
    @SerializedName("instruction")
    @Expose
    private String instruction;
    @SerializedName("offset")
    @Expose
    private Integer offset;
    @SerializedName("direction")
    @Expose
    private String direction;
    @SerializedName("severity")
    @Expose
    private String severity;
    private final static long serialVersionUID = -2166595419598938450L;

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

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
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
