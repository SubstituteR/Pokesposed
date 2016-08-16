package com.rileystrickland.pokesposed.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.rileystrickland.pokesposed.interfaces.locationSimulatorListener;
import com.rileystrickland.pokesposed.networkCommands;

import java.util.ArrayList;


public class worker extends Service implements locationSimulatorListener {


    private ArrayList<Messenger> clients = new ArrayList<>();
    private locationSimulator LocationSimulator = new locationSimulator(this);
    private Messenger mMessenger = new Messenger(new IncomingHandler());
    private Context context = null;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            Bundle bundle;
            switch (message.what) {
                case messageCode.MSG_CON:
                    if (message.replyTo != null) {
                        clients.add(message.replyTo);

                        bundle = new Bundle();
                        bundle.putDouble("x", (float) serviceSettings.savedLatLng.latitude);
                        bundle.putDouble("y", (float) serviceSettings.savedLatLng.longitude);
                        bundle.putBoolean("he", serviceSettings.hookEnabled);
                        bundle.putInt("mm", serviceSettings.movementMode);
                        bundle.putInt("ms", serviceSettings.movementSpeed);
                        bundle.putDouble("mv", serviceSettings.movementVariance);
                        bundle.putBoolean("wl", serviceSettings.walkloop);
                        bundle.putBoolean("hr", serviceSettings.hookRunning);
                        networkCommands.sendPrefernces(message.replyTo, bundle);
                    }
                    break;

                case messageCode.MSG_ADD_LL:
                    bundle = message.getData();
                    if (bundle.containsKey("x") && bundle.containsKey("y")) {
                        LatLng addLatLng = new LatLng(bundle.getDouble("x", 0), bundle.getDouble("y", 0)); //shouldn't use the defaults due to check above
                        LocationSimulator.addLatLng(addLatLng);
                    }
                    break;

                case messageCode.MSG_CLEAR_LL:
                    LocationSimulator.clearLatLng();
                    break;

                case messageCode.MSG_UNDO_LL:
                    LocationSimulator.undoLatLng();
                    break;

                case messageCode.MSG_PREF:
                    bundle = message.getData();
                    serviceSettings.hookEnabled = bundle.getBoolean("he", true);
                    if (serviceSettings.hookEnabled)
                    {
                        LocationSimulator.Start();
                    }else{
                        LocationSimulator.Stop();
                    }
                    serviceSettings.movementMode = bundle.getInt("mm", movementModes.Teleport);
                    serviceSettings.movementSpeed = bundle.getInt("ms", 8);
                    serviceSettings.movementVariance = bundle.getDouble("mv", 0.5);
                    serviceSettings.walkloop = bundle.getBoolean("wl", false);
                    LocationSimulator.setSpeed(serviceSettings.movementSpeed, serviceSettings.movementVariance);
                    serviceSettings.Save(context);

                    bundle = new Bundle();
                    bundle.putDouble("x", (float) serviceSettings.savedLatLng.latitude);
                    bundle.putDouble("y", (float) serviceSettings.savedLatLng.longitude);
                    bundle.putBoolean("he", serviceSettings.hookEnabled);
                    bundle.putInt("mm", serviceSettings.movementMode);
                    bundle.putInt("ms", serviceSettings.movementSpeed);
                    bundle.putDouble("mv", serviceSettings.movementVariance);
                    bundle.putBoolean("wl", serviceSettings.walkloop);
                    bundle.putBoolean("hr", serviceSettings.hookRunning);
                    for (Messenger messenger : clients)
                    {
                        if (!networkCommands.sendPrefernces(messenger, bundle))
                        {

                        }
                    }
                    break;

                case messageCode.MSG_PP:
                    serviceSettings.hookRunning = !serviceSettings.hookRunning;
                    if (serviceSettings.hookRunning)
                    {
                        LocationSimulator.Start();

                    }else{
                        LocationSimulator.Stop();
                    }
                    for (Messenger messenger : clients)
                    {
                        networkCommands.sendPlayPause(messenger);
                    }
                    break;

                default:
                    super.handleMessage(message);
            }
        }
    }

    public worker() {
        context = this;
    }

    /*
    IT'S HIGH NOON
     */
    @Override
    public void onCreate() {
        super.onCreate();
        serviceSettings.Load(context);
        LocationSimulator.setSpeed(serviceSettings.movementSpeed, serviceSettings.movementVariance);
        if (serviceSettings.hookEnabled)
        {
            LocationSimulator.Start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try
        {
            serviceSettings.savedLatLng = LocationSimulator.getLastLatLng();
            LocationSimulator.Stop();
            serviceSettings.Save(context);
            Log.i("pinfo", "SAVED");
        }catch (Throwable e)
            {

            }
        return false;
    }

    /*
    EVENT CODE
     */
    @Override
    public void onLocationChanged(LatLng newLatLng, float bearing) {
        serviceSettings.savedLatLng = newLatLng;
        for (Messenger messenger : clients)
        {
            networkCommands.sendPosition(messenger, newLatLng, bearing);
        }
    }

    @Override
    public void onFirstCoordRemoved() {
        for (Messenger messenger : clients)
        {
            networkCommands.sendFirstCleared(messenger);
        }
    }

    @Override
    public void onFirstCoordCycled() {
        for (Messenger messenger : clients)
        {
            networkCommands.sendFirstCycled(messenger);
        }
    }
    @Override
    public void onCoordCleared() {
        for (Messenger messenger : clients)
        {
            networkCommands.sendClearLatLng(messenger);
        }
    }

    @Override
    public void onCoordUndo() {
        for (Messenger messenger : clients)
        {
            networkCommands.sendUndoLatLng(messenger);
        }
    }

    @Override
    public void onCoordAdded(LatLng newLatLng)
    {

        for (Messenger messenger : clients)
        {
            networkCommands.sendNewLatLng(messenger, newLatLng);
        }

    }

}
