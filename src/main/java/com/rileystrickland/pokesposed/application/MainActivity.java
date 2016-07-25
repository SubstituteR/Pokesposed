package com.rileystrickland.pokesposed.application;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
    private Messenger Messenger;
    private Messenger Service;
    private Marker positionMarker;
    private ArrayList<Marker> CoordMarkers = new ArrayList<>();
    private DialogInterface.OnClickListener dong = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    };


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
            Service = null;
        }
    };

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
    protected void onStop()
    {
        super.onStop();
        if (applicationSettings.inSettings = false) {
            disconnect();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        connect();
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng pos) {
                networkCommands.sendNewLatLng(Service, pos);
            }
        });
    }

    public void onButtonClick(View view)
    {
        networkCommands.sendClearLatLng(Service);
    }

    public void launchSettings(View view)
    {
        Intent myIntent = new Intent(MainActivity.this, Settings.class);
        myIntent.putExtra("messenger", Service);
        applicationSettings.inSettings = true;
        MainActivity.this.startActivity(myIntent);
    }


    /* network code */

    private void connect()
    {
            if (Messenger == null)
            {
                Messenger = new Messenger(new IncomingHandler());
            }
            if (Service == null)
            {
                Intent intent = new Intent(this, com.rileystrickland.pokesposed.service.worker.class);
                this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
    }

    private void disconnect()
    {
        if (Service != null)
        {
            networkCommands.sendDisconnect(Service);
            this.unbindService(serviceConnection);
            Service = null;
        }
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

                    case messageCode.MSG_ADD_LL:
                        bundle = message.getData();
                        addMarker(new LatLng(bundle.getDouble("x"), bundle.getDouble("y")));
                        break;

                    case messageCode.MSG_DEL_LL:
                        if (!CoordMarkers.isEmpty())
                        {
                            CoordMarkers.get(0).remove();
                            removeMarker(0);
                        }
                        break;
                    default:
                        super.handleMessage(message);
                }
            }catch(Throwable e){}
        }
    }

    public void addMarker(LatLng latlng)
    {
        Marker marker = mMap.addMarker(new MarkerOptions().position(latlng));
        CoordMarkers.add(marker);
    }

    private void removeMarker(int index)
    {
        if (CoordMarkers.size() - 1 >= index)
        {
            CoordMarkers.remove(index);
        }
    }
}
