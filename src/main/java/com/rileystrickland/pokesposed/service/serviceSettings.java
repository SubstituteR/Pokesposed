package com.rileystrickland.pokesposed.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;

public class serviceSettings {

    public static LatLng savedLatLng = new LatLng(0,0);
    public static int movementMode = 0;
    public static boolean hookEnabled = true;

    public static void Load(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("config", context.MODE_PRIVATE);
        savedLatLng = new LatLng(pref.getFloat("x",0),pref.getFloat("y",0));
        movementMode = pref.getInt("mm", 0);
        hookEnabled = pref.getBoolean("he",true);
    }

    public static void Save(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("config", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat("x", (float) savedLatLng.latitude);
        editor.putFloat("y", (float) savedLatLng.longitude);
        editor.putInt("mm",movementMode);
        editor.putBoolean("he",hookEnabled);
        editor.commit();
    }
}
