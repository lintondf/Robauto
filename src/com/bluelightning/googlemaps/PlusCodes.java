
package com.bluelightning.googlemaps;

import java.io.FileReader;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.bluelightning.here.HereJsonRoute;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlusCodes {

    @SerializedName("items")
    @Expose
    private List<Item> items = null;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static void main(String[] args) {
        GsonBuilder builder = new GsonBuilder(); 
        builder.setPrettyPrinting(); 
        
        String plusCode = "PQ2F+4G Manning, South Carolina";
        int spc = plusCode.indexOf(' ');
        System.out.println(plusCode.substring(0, spc));
        System.out.println(plusCode.substring(spc+1));
        
        Gson gson = builder.create(); 
        try {
	        String jsonString = IOUtils.toString( new FileReader("geocode.json"));//, Charset.defaultCharset());
	        PlusCodes plus = gson.fromJson(jsonString, PlusCodes.class); 
	        System.out.println(plus.getItems().get(0).getPosition().getLat());
	        System.out.println(plus.getItems().get(0).getPosition().getLng());
        } catch (Exception x) {
        	x.printStackTrace();
        }
    }
}
