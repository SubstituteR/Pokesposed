package com.rileystrickland.pokesposed.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class worker extends Service implements locationSimulator.listener {


    private ArrayList<Messenger> clients = new ArrayList<>();
    private locationSimulator LocationSimulator = new locationSimulator(this);
    private Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case messageCode.MSG_CON:

                    if (message.replyTo != null)
                    {
                        clients.add(message.replyTo);
                        sendPrefences(message.replyTo);
                    }
                    break;

                case messageCode.MSG_DSC:
                    if (message.replyTo != null)
                    {
                        clients.remove(message.replyTo);
                    }
                    break;

                case messageCode.MSG_ADD_LL:
                    Bundle data = message.getData();
                    if (data.containsKey("x") && data.containsKey("y"))
                    {
                        LatLng addLatLng = new LatLng(data.getDouble("x",0),data.getDouble("y",0)); //shouldn't use the defaults due to check above
                        LocationSimulator.addLatLng(addLatLng);
                    }
                    break;

                case messageCode.MSG_CLEAR_LL:
                    LocationSimulator.clearLatLng();
                    break;

                default:
                    super.handleMessage(message);
            }
        }
    }

    //Do I even need this?
    public worker() {

    }
    /*
    IT'S HIGH NOON
     */
    @Override
    public void onCreate() {
        super.onCreate();
        serviceSettings.Load(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceSettings.Save(this);
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
    public void onLocationChanged(LatLng nLatLng)
    {
        for (int i=clients.size()-1;i>0;i--)
        {
            sendPosition(clients.get(i), nLatLng);
        }
    }

    @Override
    public void onFirstCoordRemoved()
    {
        for (int i=clients.size()-1;i>0;i--)
        {
            sendFirstCleared(clients.get(i));
        }
    }

    @Override
    public void onCoordCleared()
    {
        for (int i=clients.size()-1;i>0;i--)
        {
            sendCleared(clients.get(i));
        }
    }

    /*
    NETWORK CODE
     */

    private void sendPrefences(Messenger messenger)
    {
        Message message = Message.obtain(null,messageCode.MSG_PREF);
        Bundle resBundle = new Bundle();
        resBundle.putFloat("x", (float) serviceSettings.savedLatLng.latitude);
        resBundle.putFloat("y", (float) serviceSettings.savedLatLng.longitude);
        resBundle.putBoolean("he", serviceSettings.hookEnabled);
        resBundle.putInt("mm", serviceSettings.movementMode);
        message.setData(resBundle);
        send(messenger, message);
    }

    private void sendPosition(Messenger messenger, LatLng position)
    {
        Message message = Message.obtain(null, messageCode.MSG_POS);
        Bundle bundle = new Bundle();
        bundle.putDouble("x", position.latitude);
        bundle.putDouble("y",  position.longitude);
        message.setData(bundle);
        send(messenger, message);
    }

    private void sendFirstCleared(Messenger messenger)
    {
        Message message = Message.obtain(null,messageCode.MSG_DEL_LL);
        send(messenger, message);
    }

    private void sendCleared(Messenger messenger)
    {
        Message message = Message.obtain(null,messageCode.MSG_CLEAR_LL);
        send(messenger, message);
    }

    private void send(Messenger messenger, Message message)
    {
        try {messenger.send(message);}
        catch (Throwable e){
            clients.remove(messenger);
        }
    }
}
