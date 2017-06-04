package com.rileystrickland.pokesposed.application;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rileystrickland.pokesposed.R;
import com.rileystrickland.pokesposed.networkCommands;
import com.rileystrickland.pokesposed.service.messageCode;
import java.util.ArrayList;

public class MainActivity extends Activity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private Marker positionMarker;
    private ArrayList<Marker> CoordMarkers = new ArrayList<>();
    private DialogInterface.OnClickListener dong = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    };
    private networkManager networking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Global.loaded) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setTitle("Module not loaded");
            dlgAlert.setMessage("Open Xposed and enable the Module before trying to use the companion app!");
            dlgAlert.setPositiveButton("Ok", dong);
            dlgAlert.create().show();
            return;
        }
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        networking = new networkManager(this, new IncomingHandler());
        networking.connect();
        Log.i("pinfo", "MA START");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        networking.disconnect();
        Log.i("pinfo", "MA STOP");
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng pos) {
                networkCommands.sendNewLatLng(networking.Service, pos);
            }
        });
    }

    public void clearCoords(View view)
    {
        networkCommands.sendClearLatLng(networking.Service);
    }

    public void undoCoord(View view)
    {
        networkCommands.sendUndoLatLng(networking.Service);
    }

    public void launchSettings(View view)
    {
        Intent myIntent = new Intent(MainActivity.this, Settings.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void pop(View view)
    {
        networkCommands.sendPlayPause(networking.Service);
    }
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            try
            {
                Bundle bundle;
                switch (message.what) {

                    case messageCode.MSG_POS:
                        bundle = message.getData();
                        LatLng loc = new LatLng(bundle.getDouble("x", 0), bundle.getDouble("y", 0));
                        if (positionMarker == null)
                        {
                            positionMarker = mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromResource(R.drawable.pokeball)));
                        }else{
                            positionMarker.setPosition(loc);
                        }
                        break;

                    case messageCode.MSG_PREF:
                        bundle = message.getData();
                        applicationSettings.savedLatLng = new LatLng(bundle.getDouble("x", applicationSettings.savedLatLng.latitude),bundle.getDouble("y", applicationSettings.savedLatLng.longitude));
                        applicationSettings.hookEnabled = bundle.getBoolean("he", applicationSettings.hookEnabled);
                        applicationSettings.movementMode = bundle.getInt("mm", applicationSettings.movementMode);
                        applicationSettings.movementSpeed = bundle.getInt("ms", applicationSettings.movementSpeed);
                        applicationSettings.movementVariance = bundle.getDouble("mv", applicationSettings.movementVariance);
                        applicationSettings.walkloop = bundle.getBoolean("wl", applicationSettings.walkloop);
                        applicationSettings.hookRunning = bundle.getBoolean("hr", applicationSettings.hookRunning);
                        ((ToggleButton) findViewById(R.id.toggleButton1)).setChecked(applicationSettings.hookRunning);
                        if (positionMarker == null)
                        {
                            positionMarker = mMap.addMarker(new MarkerOptions().position(applicationSettings.savedLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.pokeball)));
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(applicationSettings.savedLatLng).zoom(17.5f).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }else{
                            positionMarker.setPosition(applicationSettings.savedLatLng);
                        }
                        break;

                    case messageCode.MSG_CLEAR_LL:
                        //clear
                        if (CoordMarkers.size() > 0)
                        {
                            for (int i=0; i<CoordMarkers.size();i++)
                            {
                                CoordMarkers.get(i).remove();
                            }
                            CoordMarkers.clear();
                        }

                        break;

                    case messageCode.MSG_UNDO_LL:
                        if (CoordMarkers.size() > 0)
                        {
                            removeMarker(CoordMarkers.size() - 1);
                        }
                        break;

                    case messageCode.MSG_ADD_LL:
                        bundle = message.getData();
                        addMarker(new LatLng(bundle.getDouble("x"), bundle.getDouble("y")));
                        break;

                    case messageCode.MSG_CYC_LL:
                        if (!CoordMarkers.isEmpty())
                        {
                            addMarker(CoordMarkers.get(0).getPosition());
                            removeMarker(0);
                        }
                        break;

                    case messageCode.MSG_DEL_LL:
                        if (!CoordMarkers.isEmpty())
                        {
                            removeMarker(0);
                        }
                        break;

                    case messageCode.MSG_PP:
                        applicationSettings.hookRunning = !applicationSettings.hookRunning;
                        ((ToggleButton) findViewById(R.id.toggleButton1)).setChecked(applicationSettings.hookRunning);
                        break;
                    default:
                        super.handleMessage(message);
                }
            }catch(Throwable e){}
        }
    }




    public void addMarker(LatLng latlng)
    {
        Marker marker = mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
        CoordMarkers.add(marker);
    }

    private void removeMarker(int index)
    {
        if (CoordMarkers.size() - 1 >= index)
        {
            CoordMarkers.get(index).remove();
            CoordMarkers.remove(index);
        }
    }
}
