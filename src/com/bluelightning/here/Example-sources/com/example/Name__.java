
package com.example;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Name__ implements Serializable
{

    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("language")
    @Expose
    private String language;
    private final static long serialVersionUID = 2029254578202833038L;

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
