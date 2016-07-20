package com.rileystrickland.pokesposed;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;

public class FlatSettings {

    public static LatLng savedLatLng = new LatLng(0,0);
    public static boolean walkToPoint = false;
    public static boolean hookEnabled = true;


    public static void Load(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("config", context.MODE_PRIVATE);
        savedLatLng = new LatLng(pref.getFloat("x",0),pref.getFloat("y",0));
        walkToPoint = pref.getBoolean("wtp",false);
        hookEnabled = pref.getBoolean("he",true);
    }

    public static void Save(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("config", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat("x", (float) savedLatLng.latitude);
        editor.putFloat("y", (float) savedLatLng.longitude);
        editor.putBoolean("wtp",walkToPoint);
        editor.putBoolean("he",hookEnabled);

    }
}
