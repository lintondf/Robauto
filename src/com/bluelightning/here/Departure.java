
package com.bluelightning.here;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Departure implements Serializable
{

    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("place")
    @Expose
    private Place place;
    private final static long serialVersionUID = -6217590620752586597L;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

}
