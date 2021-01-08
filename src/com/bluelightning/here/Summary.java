
package com.bluelightning.here;

import java.io.Serializable;

import com.bluelightning.Here2;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Summary implements Serializable
{

    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("length")
    @Expose
    private Integer length;
    @SerializedName("baseDuration")
    @Expose
    private Integer baseDuration;
    private final static long serialVersionUID = 934581292423732588L;

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

    public Integer getBaseDuration() {
        return baseDuration;
    }

    public void setBaseDuration(Integer baseDuration) {
        this.baseDuration = baseDuration;
    }

    public String toString() {
    	return String.format("%d m; %s; %.1f MPH", length, Here2.toPeriod(duration), (double)length/(double)duration * Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR );
    }
}
