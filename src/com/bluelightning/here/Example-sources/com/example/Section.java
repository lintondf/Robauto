
package com.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Section implements Serializable
{

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("actions")
    @Expose
    private List<Action> actions = new ArrayList<Action>();
    @SerializedName("turnByTurnActions")
    @Expose
    private List<TurnByTurnAction> turnByTurnActions = new ArrayList<TurnByTurnAction>();
    @SerializedName("departure")
    @Expose
    private Departure departure;
    @SerializedName("arrival")
    @Expose
    private Arrival arrival;
    @SerializedName("summary")
    @Expose
    private Summary summary;
    @SerializedName("polyline")
    @Expose
    private String polyline;
    @SerializedName("spans")
    @Expose
    private List<Span> spans = new ArrayList<Span>();
    @SerializedName("language")
    @Expose
    private String language;
    @SerializedName("transport")
    @Expose
    private Transport transport;
    private final static long serialVersionUID = 948852949883684986L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<TurnByTurnAction> getTurnByTurnActions() {
        return turnByTurnActions;
    }

    public void setTurnByTurnActions(List<TurnByTurnAction> turnByTurnActions) {
        this.turnByTurnActions = turnByTurnActions;
    }

    public Departure getDeparture() {
        return departure;
    }

    public void setDeparture(Departure departure) {
        this.departure = departure;
    }

    public Arrival getArrival() {
        return arrival;
    }

    public void setArrival(Arrival arrival) {
        this.arrival = arrival;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    public List<Span> getSpans() {
        return spans;
    }

    public void setSpans(List<Span> spans) {
        this.spans = spans;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

}
