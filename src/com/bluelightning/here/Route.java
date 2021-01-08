
package com.bluelightning.here;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Route implements Serializable
{

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("sections")
    @Expose
    private List<Section> sections = new ArrayList<Section>();
    private final static long serialVersionUID = 1364514526096149225L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

}
