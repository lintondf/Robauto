
package com.bluelightning.here;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NextRoad implements Serializable
{

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("name")
    @Expose
    private List<Name_> name = new ArrayList<Name_>();
    @SerializedName("number")
    @Expose
    private List<Number_> number = new ArrayList<Number_>();
    private final static long serialVersionUID = 9148266183544450710L;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Name_> getName() {
        return name;
    }

    public void setName(List<Name_> name) {
        this.name = name;
    }

    public List<Number_> getNumber() {
        return number;
    }

    public void setNumber(List<Number_> number) {
        this.number = number;
    }

}
