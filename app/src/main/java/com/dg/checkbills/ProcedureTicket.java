package com.dg.checkbills;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ProcedureTicket extends FragmentActivity implements LocationListener
{
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private PhotoFragment photoFragment = null;
    private TicketInformation ticketInfoFragment = null;
    private LocationManager lm;
    private Calendar cal;
    private String strDate;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedure_ticket);
        dispatchTakePictureIntent();

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,1,0);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,500,0, this);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            strDate = sdf.format(cal.getTime());
            Log.d("DATE", strDate);

        }
    }

    public void getNext() {
        photoFragment.getView().setVisibility(View.INVISIBLE);
        ticketInfoFragment.getView().setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            photoFragment = (PhotoFragment) getSupportFragmentManager().findFragmentById(R.id.scanFragment);
            photoFragment.putPhoto(imageBitmap);

            ticketInfoFragment = (TicketInformation) getSupportFragmentManager().findFragmentById(R.id.ticketInfoFragment);
            ticketInfoFragment.getView().setVisibility(View.INVISIBLE);
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("POSLOGLAT", (String.valueOf(location.getLongitude())) + " " +
                String.valueOf(location.getLatitude()));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    @Override
    protected void onDestroy() {
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,1,0);
        lm.removeUpdates(this);
        super.onDestroy();
    }

}





