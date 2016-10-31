package com.dg.checkbills;

import android.graphics.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CamActivity extends AppCompatActivity {

    int TAKE_PHOTO_CODE = 0;
    public static int count = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

    }


}
