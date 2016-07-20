package com.rileystrickland.pokesposed;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class MessengerService extends Service {


    private ArrayList<Messenger> clients = new ArrayList<>();

    private Messenger mMessenger = new Messenger(new IncomingHandler());
    private static Handler Looper = new Handler();

    private  Runnable loopTask = new Runnable() {
        @Override
        public void run() {


            LocationManager.Update();
            for (int i=0;i<clients.size();i++)
            {
                sendPosition(clients.get(i));
                Log.i("ERRR","SENT CLIENT INFO");
            }

            if (clients.size() == 0)
            {
                Looper.removeCallbacks(loopTask);
                stopSelf();
            }else{
                Looper.postDelayed(loopTask, 100);
            }

        }
    };

    private void sendPosition(Messenger client)
    {
        Message message = Message.obtain(null, messageCode.MSG_POS);
        Bundle bundle = new Bundle();
        bundle.putDouble("x", 29.479938);
        bundle.putDouble("y",  -81.130524);
        message.setData(bundle);
        try {client.send(message);}
        catch (Throwable e){
            clients.remove(client);
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case messageCode.MSG_CON:

                    if (message.replyTo != null)
                    {
                        clients.add(message.replyTo);
                        if (clients.size() == 1)
                        {
                            Looper.post(loopTask);
                        }
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
                        LocationManager.addLatLng(addLatLng);
                    }

                default:
                    super.handleMessage(message);
            }
        }
    }

    public MessengerService() {

    }

    @Override
    public void onCreate() {
        //FlatSettings.Load();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }
}
