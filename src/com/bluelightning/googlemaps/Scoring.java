
package com.bluelightning.googlemaps;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Scoring {

    @SerializedName("queryScore")
    @Expose
    private Double queryScore;
    @SerializedName("fieldScore")
    @Expose
    private FieldScore fieldScore;

    public Double getQueryScore() {
        return queryScore;
    }

    public void setQueryScore(Double queryScore) {
        this.queryScore = queryScore;
    }

    public FieldScore getFieldScore() {
        return fieldScore;
    }

    public void setFieldScore(FieldScore fieldScore) {
        this.fieldScore = fieldScore;
    }

}
