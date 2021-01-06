
package com.example;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Arrival implements Serializable
{

    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("place")
    @Expose
    private Place_ place;
    private final static long serialVersionUID = -70633855491068625L;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Place_ getPlace() {
        return place;
    }

    public void setPlace(Place_ place) {
        this.place = place;
    }

}
