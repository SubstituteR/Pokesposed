package com.rileystrickland.pokesposed.hook;


import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import android.app.AndroidAppHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.rileystrickland.pokesposed.service.messageCode;

import java.nio.ByteBuffer;

public class run implements IXposedHookLoadPackage  {
    private Location nloc = null;
    private boolean connected = false;
    private LoadPackageParam lpparam = null;
    private Messenger Messenger = null;
    private Messenger Service = null;
    private int[] lastStatus = null;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            try
            {
                Bundle bundle;
                switch (message.what) {

                    case messageCode.MSG_POS:
                        bundle = message.getData();
                        if (nloc != null)
                        {
                            nloc.setLatitude(bundle.getDouble("x", 0));
                            nloc.setLongitude(bundle.getDouble("y", 0));
                            nloc.setBearing(bundle.getFloat("b", 0));
                            callLocation();
                        }
                        break;

                    case messageCode.MSG_PREF:
                        bundle = message.getData();
                        hookSettings.hookEnabled = bundle.getBoolean("he", true);
                        break;
                    default:
                        super.handleMessage(message);

                }
            }catch(Throwable e){}
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Service = new Messenger(service);
            Message message = Message.obtain(null, messageCode.MSG_CON, 0, 0);
            message.replyTo = Messenger;
            try {Service.send(message);} catch (RemoteException e) {}
            connected = true;
        }





        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
        }
    };


    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        this.lpparam = lpparam;

        if (lpparam.packageName.equals("com.rileystrickland.pokesposed"))
        {
            Class<?> clazz = XposedHelpers.findClass("com.rileystrickland.pokesposed.application.Global", lpparam.classLoader);
            XposedHelpers.setStaticBooleanField(clazz, "loaded", true);
        }


        if (lpparam.packageName.equals("com.nianticlabs.pokemongo"))
        {
            XposedBridge.log("Poke Mongo Detected.");
            hookUnity();
            hookLocation();
            hookNetwork();
        }



    }


    private void hookUnity() {

        findAndHookConstructor("com.unity3d.player.UnityPlayerNativeActivity", lpparam.classLoader, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (connect()) {
                    Toast.makeText(AndroidAppHelper.currentApplication(), "Pokesposed Loaded!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(AndroidAppHelper.currentApplication(), "Pokesposed NOT Loaded!", Toast.LENGTH_SHORT).show();
                }
            }});
    }


    private boolean connect()
    {
        Context context = AndroidAppHelper.currentApplication();
        if (context != null)
        {
            if (Messenger == null)
            {
                Messenger = new Messenger(new IncomingHandler());
            }
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.rileystrickland.pokesposed", "com.rileystrickland.pokesposed.service.worker");
            intent.setComponent(cn);
            if (!context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE))
            {
                Toast.makeText(AndroidAppHelper.currentApplication(), "Unable to start service!", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }



    private void hookLocation() {
        findAndHookMethod("com.nianticlabs.nia.location.NianticLocationManager", lpparam.classLoader, "locationUpdate", Location.class, int[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!connected)
                {
                    connect();
                }
                if (param.args[0] != null && hookSettings.hookEnabled)
                {
                    Location cloc = (Location) param.args[0];
                    if (nloc == null)
                    {
                        nloc = cloc;
                    }
                    cloc.setLatitude(nloc.getLatitude());
                    cloc.setLongitude(nloc.getLongitude());
                    cloc.setBearing(nloc.getBearing());
                    param.args[0] = cloc;
                }
                if (param.args[1] != null)
                {
                    lastStatus = (int[]) param.args[1];
                }
            }
        });
    }

    private void callLocation()
    {
        if (nloc != null)
        {
            callMethod("com.nianticlabs.nia.location.NianticLocationManager", "locationUpdate", nloc, lastStatus);
        }
    }

    private void hookNetwork() {
        /*findAndHookMethod("com.nianticlabs.nia.network.NiaNet", lpparam.classLoader, "doSyncRequest",long.class, int.class, String.class, int.class, String.class, ByteBuffer.class, int.class, int.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return null;
            }
        });*/
    }
}