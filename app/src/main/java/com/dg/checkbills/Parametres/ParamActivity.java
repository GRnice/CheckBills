package com.dg.checkbills.Parametres;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Daemon.ServiceSocket;
import com.dg.checkbills.R;

public class ParamActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_param);
        setTitle("Parametres");

        final Switch switch4G = (Switch) findViewById(R.id.switch4g);
        Button btnValid = (Button) findViewById(R.id.buttonValidParam);

        btnValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentParam = new Intent();
                intentParam.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
                intentParam.putExtra("PARAMDATA",switch4G.isChecked());
                sendBroadcast(intentParam);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }
}
