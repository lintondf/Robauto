
package com.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Span implements Serializable
{

    @SerializedName("offset")
    @Expose
    private Integer offset;
    @SerializedName("names")
    @Expose
    private List<Name__> names = new ArrayList<Name__>();
    @SerializedName("length")
    @Expose
    private Integer length;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("speedLimit")
    @Expose
    private Double speedLimit;
    private final static long serialVersionUID = -2930164166401118166L;

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public List<Name__> getNames() {
        return names;
    }

    public void setNames(List<Name__> names) {
        this.names = names;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Double getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(Double speedLimit) {
        this.speedLimit = speedLimit;
    }

}
