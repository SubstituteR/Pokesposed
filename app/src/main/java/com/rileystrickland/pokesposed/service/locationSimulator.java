package com.rileystrickland.pokesposed.service;

import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.rileystrickland.pokesposed.interfaces.locationSimulatorListener;

import java.util.ArrayList;
import java.util.Random;

import de.robv.android.xposed.XposedBridge;

public class locationSimulator {
    private static final String TAG = locationSimulator.class.getSimpleName();
    public locationSimulatorListener listener;
    private LatLng stationaryLatLng;


    locationSimulator(locationSimulatorListener listener) {
        this.listener = listener;
    }


    private ArrayList<LatLng> coords = new ArrayList<>();
    private LatLng currentLatLng = null;
    private LatLng nextLatLng = null;
    private int movementMode = movementModes.Teleport;
    private double ang = 0;
    private int speed = 8;
    private double speedVariance = 1.5;
    private double calculatedSpeed = 0;
    private Random rngesus = new Random();
    private Handler looper = null;

    public LatLng getCurrentLatLng()

    {
        return currentLatLng;
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
        if (currentLatLng == null)
        {
            currentLatLng = serviceSettings.savedLatLng;
        }
        NextLatLng();
        listener.onLocationChanged(currentLatLng, (float) ang);
    }

    private void NextLatLng()
    {
        nextLatLng = null;

        if (coords.isEmpty())
        {
            // Add jitter to the calculated GPS location. No real device
            // would stand at the exact same GPS location for ages
            if (stationaryLatLng == null) {
                stationaryLatLng = currentLatLng;
            }

            if (Math.random() > .7) {
                Log.d(TAG, "Randomizing stationary GPS location..");
                randomizeLocation(stationaryLatLng, 2);
            } else {
                Log.d(TAG, "Skipping randomization of stationary GPS location..");
            }
            return;
        }

        if (movementMode == movementModes.Walk)
        {
            stationaryLatLng = null;
            calculatedSpeed = getNextSpeed();
            nextLatLng = coords.get(0); //get 1st
                    if (inDistance())
                    {
                        currentLatLng = nextLatLng;
                        if (serviceSettings.walkloop && coords.size() > 1)
                        {
                            cycleFirstLatLng();
                        }else{

                            removeFirstLatLng();
                        }
                    }else{
                        currentLatLng = LatLngFromLatLng(currentLatLng, calculatedSpeed, ang);
                    }
        }else{
            currentLatLng = coords.get(coords.size() - 1); //get last, we're tp'ing
            clearLatLng();
        }
    }

    // Stolen from http://gis.stackexchange.com/a/68275
    private void randomizeLocation(LatLng latLng, int radius) {
        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double lonRandomization = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double latRandomization = x / Math.cos(latLng.longitude);

        double newLat = latRandomization + latLng.latitude;
        double newLon = lonRandomization + latLng.longitude;

        double dist = distance(latLng.latitude, newLat, latLng.longitude, newLon, 1, 1);

        if (dist < radius) {
            Log.d(TAG, String.format("Dist %s is a safe jump.. changing currentLatLng", dist));
            currentLatLng = new LatLng(newLat, newLon);
        } else {
            Log.d(TAG, String.format("Dist %s is an unsafe jump.. not changing currentLatLng to old pos", dist));
            currentLatLng = stationaryLatLng;
        }
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
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
        Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude,nextLatLng.latitude,nextLatLng.longitude,d);

        if (d[0] <= 1.5 * calculatedSpeed)
        {
            return true;
        }else{
            ang = d[1];
            return false;
        }
    }



}

