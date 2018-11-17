package com.bluelightning.gps;

import com.bluelightning.gps.ISerialGps.StateListener;

public interface ISerialGps {
    public interface StateListener {
        void onGpsStateUpdated(NMEA.GpsState state);
    }

    public boolean start();
    public void stop() throws InterruptedException;
    
    public void addStateListener(StateListener stateListener);
}