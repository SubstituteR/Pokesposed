package com.rileystrickland.pokesposed.application;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.rileystrickland.pokesposed.service.messageCode;

public class networkManager {

    private Messenger Messenger;
    public Messenger Service;
    private Handler handler;
    private Activity activity;

    public networkManager(Activity activity, Handler handler)
    {
        this.activity = activity;
        this.handler = handler;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Service = new Messenger(service);
            Message message = Message.obtain(null, messageCode.MSG_CON, 0, 0);
            message.replyTo = Messenger;
            try {
                Service.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Message message = Message.obtain(null, messageCode.MSG_DSC, 0, 0);
            try {
                Service.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Service = null;
        }
    };

    public void connect()
    {
        if (Messenger == null)
        {
            Messenger = new Messenger(handler);
        }
        if (Service == null)
        {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.rileystrickland.pokesposed", "com.rileystrickland.pokesposed.service.worker");
            intent.setComponent(cn);
            activity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void disconnect()
    {
        if (Service != null)
        {
            try
            {

                activity.unbindService(serviceConnection);
            }catch(Throwable e)
            {

            }
        }
    }




}
