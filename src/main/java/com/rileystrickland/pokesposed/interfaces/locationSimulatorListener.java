package com.rileystrickland.pokesposed.interfaces;

import com.google.android.gms.maps.model.LatLng;

public interface locationSimulatorListener {
    void onLocationChanged(LatLng newLatLng, float bearing);
    void onFirstCoordRemoved();
    void onCoordCleared();
    void onCoordAdded(LatLng newLatLng);
    void onCoordUndo();
    void onFirstCoordCycled();
}
