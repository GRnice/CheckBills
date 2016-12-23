package com.dg.checkbills.AjoutTicket;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.Home;
import com.dg.checkbills.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ProcedureTicket extends FragmentActivity
{
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private PhotoFragment photoFragment = null;
    private TicketInformation ticketInfoFragment = null;
    private SelectionBoutiqueFragment selectionBoutiqueFragment = null;
    private Calendar cal;
    private String strDate;
    private String androidId;
    private ServiceReceiver serviceReceiver;

    private ArrayList<Bill> billArrayList;
    private ArrayList<Boutique> boutiqueArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());
        registerReceiver(serviceReceiver,intentFilter);

        Intent boutiqueRequest = new Intent();
        boutiqueRequest.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
        boutiqueRequest.putExtra("GETBOUTIQUES",true);
        sendBroadcast(boutiqueRequest);

        setContentView(R.layout.activity_procedure_ticket);
        photoFragment = (PhotoFragment) getSupportFragmentManager().findFragmentById(R.id.scanFragment);
        ticketInfoFragment = (TicketInformation) getSupportFragmentManager().findFragmentById(R.id.ticketInfoFragment);
        ticketInfoFragment.getView().setVisibility(View.INVISIBLE);
        selectionBoutiqueFragment = (SelectionBoutiqueFragment) getSupportFragmentManager().findFragmentById(R.id.listingBoutique);

        selectionBoutiqueFragment.getView().setVisibility(View.INVISIBLE);
        androidId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        dispatchTakePictureIntent();

    }

    @Override
    public void onBackPressed()
    {
        backToHome();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(serviceReceiver);
    }


    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            strDate = sdf.format(cal.getTime());
            Log.d("DATE", strDate);
            ticketInfoFragment.setTicketDate(strDate);

        }
    }

    public void getNext()
    {
        photoFragment.getView().setVisibility(View.INVISIBLE);
        ticketInfoFragment.getView().setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");


            photoFragment.putPhoto(imageBitmap);
            ticketInfoFragment.setImageTicket(imageBitmap);
        }

    }

    public String getAndroidId()
    {
        return this.androidId;
    }

    public void backToHome()
    {
        Intent intentHome = new Intent(this,Home.class);
        startActivity(intentHome);
        finish();
    }

    public void showListingBoutiques()
    {
        photoFragment.getView().setVisibility(View.INVISIBLE);
        ticketInfoFragment.getView().setVisibility(View.INVISIBLE);
        selectionBoutiqueFragment.getView().setVisibility(View.VISIBLE);
    }

    public void showTicketInfo(@Nullable String b)
    {
        if (b != null)
        {
            ticketInfoFragment.setBoutiqueSelected(b);
        }
        photoFragment.getView().setVisibility(View.INVISIBLE);
        ticketInfoFragment.getView().setVisibility(View.VISIBLE);
        selectionBoutiqueFragment.getView().setVisibility(View.INVISIBLE);
    }

    /**
     * ServerReceiver , recoit les messages venants du service
     *
     * ACTION_RECEIVE_FROM_SERVICE
     */
    private class ServiceReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            if (arg1.hasExtra("BILLS"))
            {
                billArrayList = (ArrayList<Bill>) arg1.getSerializableExtra("BILLS");

            }

            if (arg1.hasExtra("BOUTIQUES"))
            {
                boutiqueArrayList = (ArrayList<Boutique>) arg1.getSerializableExtra("BOUTIQUES");
                selectionBoutiqueFragment.setListofBoutiques(boutiqueArrayList);
            }
        }
    }

}





