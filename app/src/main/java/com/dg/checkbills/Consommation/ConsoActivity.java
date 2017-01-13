package com.dg.checkbills.Consommation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dg.checkbills.Home;
import com.dg.checkbills.R;

public class ConsoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conso);
    }

    @Override
    public void onBackPressed()
    {
        Intent goHome = new Intent(this,Home.class);
        startActivity(goHome);
        finish();
    }
}
