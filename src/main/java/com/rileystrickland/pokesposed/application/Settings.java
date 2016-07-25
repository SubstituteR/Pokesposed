package com.rileystrickland.pokesposed.application;

import android.content.Intent;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import com.rileystrickland.pokesposed.R;
import com.rileystrickland.pokesposed.networkCommands;
import com.rileystrickland.pokesposed.service.movementModes;

public class Settings extends AppCompatActivity {

    private Messenger messenger = null;
    private Switch heSwitch = null;
    private Switch mmSwitch = null;
    private RelativeLayout walkingLayout = null;
    private EditText walkingSpeed = null;
    private EditText walkingVariance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();

        heSwitch = (Switch) findViewById(R.id.switch1);

        mmSwitch = (Switch) findViewById(R.id.switch2);

        walkingLayout = (RelativeLayout) findViewById(R.id.walkingLayout);

        walkingSpeed = (EditText) findViewById(R.id.editText);

        walkingVariance = (EditText) findViewById(R.id.editText2);

        if (intent != null)
        {
            messenger = intent.getParcelableExtra("messenger");
        }

        if (heSwitch != null && mmSwitch != null && walkingSpeed != null && walkingVariance != null)
        {
            heSwitch.setChecked(applicationSettings.hookEnabled);
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
            bundle.putInt("ms", Integer.parseInt(walkingSpeed.getText().toString()));
            bundle.putDouble("mv", Double.parseDouble(walkingVariance.getText().toString()));
            networkCommands.sendPrefernces(messenger, bundle);
        }
        applicationSettings.inSettings = false;
        finish();
    }

    public void onCancelClick(View view)
    {
        applicationSettings.inSettings = false;
        finish();
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
}
