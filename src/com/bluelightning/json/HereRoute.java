
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * http://www.jsonschema2pojo.org/
 * 
 */
public class HereRoute implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("response")
    @Expose
    private Response response;
    private final static long serialVersionUID = 737441466106976720L;

    /**
     * 
     * (Required)
     * 
     */
    public Response getResponse() {
        return response;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("response", response).toString();
    }

}
