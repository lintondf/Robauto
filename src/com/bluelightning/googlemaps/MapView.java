
package com.bluelightning.googlemaps;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MapView {

    @SerializedName("west")
    @Expose
    private Double west;
    @SerializedName("south")
    @Expose
    private Double south;
    @SerializedName("east")
    @Expose
    private Double east;
    @SerializedName("north")
    @Expose
    private Double north;

    public Double getWest() {
        return west;
    }

    public void setWest(Double west) {
        this.west = west;
    }

    public Double getSouth() {
        return south;
    }

    public void setSouth(Double south) {
        this.south = south;
    }

    public Double getEast() {
        return east;
    }

    public void setEast(Double east) {
        this.east = east;
    }

    public Double getNorth() {
        return north;
    }

    public void setNorth(Double north) {
        this.north = north;
    }

}
