package com.bluelightning;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TripPlanUpdate extends Remote {
    String update( String msg ) throws RemoteException;
}