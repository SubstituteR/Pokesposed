package com.rileystrickland.pokesposed.interfaces;

import android.os.Bundle;
import android.os.Messenger;

import com.google.android.gms.maps.model.LatLng;

public interface networkListener {

        void networkLocationChanged(LatLng newLatLng);

        void networkFirstCoordRemoved();

        void networkCoordCleared();

        void networkCoordAdded(LatLng newLatLng);

        void networkConnected(Messenger messenger);

        void networkDisconnected(Messenger messenger);

        void networkPreferences(Bundle bundle);

}
