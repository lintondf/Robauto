
package com.bluelightning.googlemaps;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FieldScore {

    @SerializedName("state")
    @Expose
    private Double state;
    @SerializedName("city")
    @Expose
    private Double city;

    public Double getState() {
        return state;
    }

    public void setState(Double state) {
        this.state = state;
    }

    public Double getCity() {
        return city;
    }

    public void setCity(Double city) {
        this.city = city;
    }

}
