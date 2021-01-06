
package com.example;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Name_ implements Serializable
{

    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("language")
    @Expose
    private String language;
    private final static long serialVersionUID = -1608472000048583128L;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
