
package com.bluelightning.here;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Transport implements Serializable
{

    @SerializedName("mode")
    @Expose
    private String mode;
    private final static long serialVersionUID = 568060757169637425L;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}
