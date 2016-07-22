package com.rileystrickland.pokesposed.application;

import android.content.Intent;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import com.rileystrickland.pokesposed.R;
import com.rileystrickland.pokesposed.networkCommands;
import com.rileystrickland.pokesposed.service.movementModes;

public class Settings extends AppCompatActivity {

    private Messenger messenger = null;
    private Switch heSwitch = null;
    private Switch mmSwitch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();

        heSwitch = (Switch) findViewById(R.id.switch1);

        mmSwitch = (Switch) findViewById(R.id.switch2);

        if (intent != null)
        {
            messenger = intent.getParcelableExtra("messenger");
        }

        if (heSwitch != null && mmSwitch != null)
        {
            heSwitch.setChecked(applicationSettings.hookEnabled);
            if (applicationSettings.movementMode == movementModes.Walk)
            {
                mmSwitch.setChecked(true);
            }else
            {
                mmSwitch.setChecked(false);
            }


        }
    }

    public void onSaveClick(View view)
    {
        if (messenger != null)
        {
            Bundle bundle = new Bundle();
            bundle.putBoolean("he", heSwitch.isChecked());
            if (mmSwitch.isChecked())
            {
                bundle.putInt("mm", movementModes.Walk);
            }else{
                bundle.putInt("mm", movementModes.Teleport);
            }

            networkCommands.sendPrefernces(messenger, bundle);
        }
        finish();
    }

    public void onCancelClick(View view)
    {

        finish();
    }
}
