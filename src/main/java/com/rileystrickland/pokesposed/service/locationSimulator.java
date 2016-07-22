package com.rileystrickland.pokesposed.service;

import android.location.Location;
import android.os.Handler;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class locationSimulator {
    public interface listener {
        void onLocationChanged(LatLng newLatLng);
        void onFirstCoordRemoved();
        void onCoordCleared();
        void onCoordAdded(LatLng newLatLng);
    }
    public listener listener;


    locationSimulator(listener listener)
    {
        this.listener = listener;
    }


    private ArrayList<LatLng> coords = new ArrayList<>();
    private LatLng lastLatLng = null;
    private LatLng nextLatLng = null;
    private int movementMode = movementModes.Teleport;
    private double ang = 0;
    private final double speed = 1.38889; //1.38 m/s = 5 km/h
    private Handler looper = null;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Update();
            movementMode = serviceSettings.movementMode;
            looper.postDelayed(runnable, 1000);
        }
    };

    public void Start()
    {
        if (looper == null)
        {
            looper = new Handler();
            looper.post(runnable);
        }
    }

    public void Stop()
    {
        if (looper != null)
        {
            looper.removeCallbacks(runnable);
            looper = null;
        }
    }
    public void addLatLng(LatLng newLatLng)
    {
        coords.add(newLatLng);
        listener.onCoordAdded(newLatLng);
    }


    private void removeFirstLatLng()
    {
        if (coords.size() > 0)
        {
            coords.remove(0);
        }
        listener.onFirstCoordRemoved();
    }

    public void clearLatLng()
    {
        coords.clear();
        listener.onCoordCleared();
    }



    private void Update()
    {

        if (lastLatLng == null)
        {
            lastLatLng = serviceSettings.savedLatLng;
        }
        NextLatLng();
        listener.onLocationChanged(lastLatLng);
    }

    private void NextLatLng()
    {
        nextLatLng = null;

        if (coords.isEmpty())
        {
            return; //nothing can change
        }

        if (movementMode == movementModes.Walk)
        {
            nextLatLng = coords.get(0); //get 1st
                    if (inDistance())
                    {
                        ang = -1;
                        lastLatLng = nextLatLng;
                        removeFirstLatLng();
                    }else{
                        lastLatLng = LatLngFromLatLng(lastLatLng,1,ang);
                    }
        }else{
            lastLatLng = coords.get(coords.size() - 1); //get last, we're tp'ing
            ang = -1;
            clearLatLng();
        }

    }


//                             ___
//Rest is math...math is hard /-.-\
    private static double radiansFromDegrees(double degrees)
    {
        return degrees * (Math.PI/180.0);
    }
    private static double degreesFromRadians(double radians)
    {
        return radians * (180.0/Math.PI);
    }
    /*

    I had a bearing from LatLng function in here, but I guess I suck at mathematics ¯\_(ツ)_/¯
    Using Location right now, want to move away from it in the future.

     */

    private static LatLng LatLngFromLatLng(LatLng fromLocation, double distance, double bearingDegrees) //from http://stackoverflow.com/a/17545955
    {
        double distanceKm = distance / 1000.0;
        double distanceRadians = distanceKm / 6371;
        //6,371 = Earth's radius in km
        double bearingRadians = radiansFromDegrees(bearingDegrees);
        double fromLatRadians = radiansFromDegrees(fromLocation.latitude);
        double fromLonRadians = radiansFromDegrees(fromLocation.longitude);

        double toLatRadians = Math.asin( Math.sin(fromLatRadians) * Math.cos(distanceRadians)
                + Math.cos(fromLatRadians) * Math.sin(distanceRadians) * Math.cos(bearingRadians) );

        double toLonRadians = fromLonRadians + Math.atan2(Math.sin(bearingRadians)
                * Math.sin(distanceRadians) * Math.cos(fromLatRadians), Math.cos(distanceRadians)
                - Math.sin(fromLatRadians) * Math.sin(toLatRadians));

        // adjust toLonRadians to be in the range -180 to +180...
        toLonRadians = ((toLonRadians + 3*Math.PI) % (2*Math.PI) ) - Math.PI;

        return new LatLng(degreesFromRadians(toLatRadians), degreesFromRadians(toLonRadians));
    }

    private boolean inDistance() //since we check this before updating, we can take the bearing value.
    {
        float[] d = {0,0};
        Location.distanceBetween(lastLatLng.latitude, lastLatLng.longitude,nextLatLng.latitude,nextLatLng.longitude,d);

        if (d[0] <= (1.5 * (speed * 10)))
        {
            return true;
        }else{
            ang = d[1];
            return false;
        }
    }



}

