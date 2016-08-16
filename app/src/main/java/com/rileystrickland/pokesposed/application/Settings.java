package com.rileystrickland.pokesposed.application;

import android.app.AlertDialog;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import com.rileystrickland.pokesposed.R;
import com.rileystrickland.pokesposed.networkCommands;
import com.rileystrickland.pokesposed.service.movementModes;

public class Settings extends AppCompatActivity {

    private Switch heSwitch = null;
    private Switch mmSwitch = null;
    private RelativeLayout walkingLayout = null;
    private EditText walkingSpeed = null;
    private EditText walkingVariance = null;
    private Switch lwSwitch = null;
    private networkManager networking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        heSwitch = (Switch) findViewById(R.id.switch1);

        mmSwitch = (Switch) findViewById(R.id.switch2);

        lwSwitch = (Switch) findViewById(R.id.switch3);

        walkingLayout = (RelativeLayout) findViewById(R.id.walkingLayout);

        walkingSpeed = (EditText) findViewById(R.id.editText);

        walkingVariance = (EditText) findViewById(R.id.editText2);

        if (heSwitch != null && mmSwitch != null && walkingSpeed != null && walkingVariance != null && lwSwitch != null)
        {
            heSwitch.setChecked(applicationSettings.hookEnabled);
            lwSwitch.setChecked(applicationSettings.walkloop);
            if (applicationSettings.movementMode == movementModes.Walk)
            {
                mmSwitch.setChecked(true);
                walkingLayout.setVisibility(View.VISIBLE);
            }else
            {
                mmSwitch.setChecked(false);
                walkingLayout.setVisibility(View.INVISIBLE);
            }

            walkingSpeed.setText(Integer.toString(applicationSettings.movementSpeed));
            walkingVariance.setText(Double.toString(applicationSettings.movementVariance));

        }
    }

    public void onSaveClick(View view)
    {
            Bundle bundle = new Bundle();
            bundle.putBoolean("he", heSwitch.isChecked());
            if (mmSwitch.isChecked())
            {
                bundle.putInt("mm", movementModes.Walk);
            }else{
                bundle.putInt("mm", movementModes.Teleport);
            }
            bundle.putInt("ms", Integer.parseInt(walkingSpeed.getText().toString()));
            bundle.putDouble("mv", Double.parseDouble(walkingVariance.getText().toString()));
            bundle.putBoolean("wl", lwSwitch.isChecked());
            networkCommands.sendPrefernces(networking.Service, bundle);
        finish();
    }

    public void onCancelClick(View view)
    {
        finish();
    }

    public void onCreditsClick(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Programmed by SubstituteCS\n\n\nGreets To\n\n/r/pokemongospoofing\n/u/LuminescentReps\ncfharp@Github\nBaIanced@Github\n\n\n+ All others who I may have missed.");
        builder.setPositiveButton("Close", null);
        builder.setTitle("Credits");
        builder.create().show();
    }

    public void onWalkToggle(View view)
    {
        mmSwitch = (Switch) view;
        if (mmSwitch.isChecked())
        {
            walkingLayout.setVisibility(View.VISIBLE);
        }else{
            walkingLayout.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        networking = new networkManager(this, new Handler()); //Settings doesn't handle any incoming information.
        networking.connect();
        Log.i("pinfo", "SA START");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        networking.disconnect();
        Log.i("pinfo", "SA STOP");
    }
}
