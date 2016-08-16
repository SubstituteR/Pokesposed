package com.rileystrickland.pokesposed.service;

import android.location.Location;
import android.os.Handler;
import com.google.android.gms.maps.model.LatLng;
import com.rileystrickland.pokesposed.interfaces.locationSimulatorListener;

import java.util.ArrayList;
import java.util.Random;

public class locationSimulator {
    public locationSimulatorListener listener;


    locationSimulator(locationSimulatorListener listener) {
        this.listener = listener;
    }


    private ArrayList<LatLng> coords = new ArrayList<>();
    private LatLng lastLatLng = null;
    private LatLng nextLatLng = null;
    private int movementMode = movementModes.Teleport;
    private double ang = 0;
    private int speed = 8;
    private double speedVariance = 1.5;
    private double calculatedSpeed = 0;
    private Random rngesus = new Random();
    private Handler looper = null;

    public LatLng getLastLatLng()

    {
        return lastLatLng;
    }

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

    private void cycleFirstLatLng()
    {
        if (coords.size() > 0)
        {
            coords.add(coords.get(0));
            coords.remove(0);
        }
        listener.onFirstCoordCycled();
    }

    public void clearLatLng()
    {
        coords.clear();
        listener.onCoordCleared();
    }

    public void undoLatLng()
    {
        if (coords.size() > 0)
        {
            coords.remove(coords.size() - 1);
        }
        listener.onCoordUndo();
    }


    private void Update()
    {
        if (lastLatLng == null)
        {
            lastLatLng = serviceSettings.savedLatLng;
        }
        NextLatLng();
        listener.onLocationChanged(lastLatLng, (float) ang);
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
            calculatedSpeed = getNextSpeed();
            nextLatLng = coords.get(0); //get 1st
                    if (inDistance())
                    {
                        lastLatLng = nextLatLng;
                        if (serviceSettings.walkloop && coords.size() > 1)
                        {
                            cycleFirstLatLng();
                        }else{

                            removeFirstLatLng();
                        }
                    }else{
                        lastLatLng = LatLngFromLatLng(lastLatLng, calculatedSpeed, ang);
                    }
        }else{
            lastLatLng = coords.get(coords.size() - 1); //get last, we're tp'ing
            clearLatLng();
        }

    }

    private double getNextSpeed()
    {
        double rngesus_variance = rngesus.nextFloat() * speedVariance;

        if (rngesus.nextBoolean())
        {
            rngesus_variance = -rngesus_variance;
        }
        return ((speed + rngesus_variance) * 1000) / (60*60);
    }

    public void setSpeed(int speed, double speedVariance)
    {
        this.speed = speed;
        this.speedVariance = speedVariance;
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

        if (d[0] <= 1.5 * calculatedSpeed)
        {
            return true;
        }else{
            ang = d[1];
            return false;
        }
    }



}

