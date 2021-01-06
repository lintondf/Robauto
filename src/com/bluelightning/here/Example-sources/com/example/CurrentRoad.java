
package com.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CurrentRoad implements Serializable
{

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("name")
    @Expose
    private List<Name> name = new ArrayList<Name>();
    @SerializedName("number")
    @Expose
    private List<Number> number = new ArrayList<Number>();
    private final static long serialVersionUID = 8925420832530807568L;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Name> getName() {
        return name;
    }

    public void setName(List<Name> name) {
        this.name = name;
    }

    public List<Number> getNumber() {
        return number;
    }

    public void setNumber(List<Number> number) {
        this.number = number;
    }

}
