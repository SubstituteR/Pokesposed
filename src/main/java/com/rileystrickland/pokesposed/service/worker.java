package com.rileystrickland.pokesposed.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.google.android.gms.maps.model.LatLng;
import com.rileystrickland.pokesposed.networkCommands;
import java.util.ArrayList;


public class worker extends Service implements locationSimulator.listener {


    private ArrayList<Messenger> clients = new ArrayList<>();
    private locationSimulator LocationSimulator = new locationSimulator(this);
    private Messenger mMessenger = new Messenger(new IncomingHandler());
    private Context context = null;

    private void dropClient(Messenger messenger)
    {
        clients.remove(messenger);
        if (clients.isEmpty())
        {
            this.stopSelf(); //force cleanup
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case messageCode.MSG_CON:

                    if (message.replyTo != null) {
                        clients.add(message.replyTo);

                        Bundle bundle = new Bundle();
                        bundle.putDouble("x", (float) serviceSettings.savedLatLng.latitude);
                        bundle.putDouble("y", (float) serviceSettings.savedLatLng.longitude);
                        bundle.putBoolean("he", serviceSettings.hookEnabled);
                        bundle.putInt("mm", serviceSettings.movementMode);

                        if (!networkCommands.sendPrefernces(message.replyTo, bundle))
                        {
                            dropClient(message.replyTo);
                        }
                    }
                    break;

                case messageCode.MSG_DSC:
                    if (message.replyTo != null) {
                        dropClient(message.replyTo);
                    }
                    break;

                case messageCode.MSG_ADD_LL:
                    Bundle data = message.getData();
                    if (data.containsKey("x") && data.containsKey("y")) {
                        LatLng addLatLng = new LatLng(data.getDouble("x", 0), data.getDouble("y", 0)); //shouldn't use the defaults due to check above
                        LocationSimulator.addLatLng(addLatLng);
                    }
                    break;

                case messageCode.MSG_CLEAR_LL:
                    LocationSimulator.clearLatLng();
                    break;

                case messageCode.MSG_PREF:
                    Bundle bundle = message.getData();
                    serviceSettings.hookEnabled = bundle.getBoolean("he");
                    if (serviceSettings.hookEnabled)
                    {
                        LocationSimulator.Start();
                    }else{
                        LocationSimulator.Stop();
                    }
                    serviceSettings.movementMode = bundle.getInt("mm");
                    serviceSettings.Save(context);

                    bundle = new Bundle();
                    bundle.putDouble("x", (float) serviceSettings.savedLatLng.latitude);
                    bundle.putDouble("y", (float) serviceSettings.savedLatLng.longitude);
                    bundle.putBoolean("he", serviceSettings.hookEnabled);
                    bundle.putInt("mm", serviceSettings.movementMode);

                    for (int i = clients.size() - 1; i >= 0; i--) {
                        if (!networkCommands.sendPrefernces(clients.get(i), bundle))
                        {
                            dropClient(clients.get(i));
                        }
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
        if (serviceSettings.hookEnabled)
        {
            LocationSimulator.Start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceSettings.Save(context);
        LocationSimulator.Stop();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    /*
    EVENT CODE
     */
    @Override
    public void onLocationChanged(LatLng newLatLng) {
        serviceSettings.savedLatLng = newLatLng;
        for (int i = clients.size() - 1; i >= 0; i--) {
            if (!networkCommands.sendPosition(clients.get(i), newLatLng))
            {
                dropClient(clients.get(i));
            }
        }
    }

    @Override
    public void onFirstCoordRemoved() {
        for (int i = clients.size() - 1; i >= 0; i--) {
            if (!networkCommands.sendFirstCleared(clients.get(i)))
            {
                dropClient(clients.get(i));
            }
        }
    }

    @Override
    public void onCoordCleared() {
        for (int i = clients.size() - 1; i >= 0; i--) {
            if (!networkCommands.sendClearLatLng(clients.get(i)))
            {
                dropClient(clients.get(i));
            }
        }
    }

    @Override
    public void onCoordAdded(LatLng newLatLng)
    {
        for (int i = clients.size() - 1; i >= 0; i--) {
            if (!networkCommands.sendNewLatLng(clients.get(i), newLatLng))
            {
                dropClient(clients.get(i));
            }
        }
    }

}
