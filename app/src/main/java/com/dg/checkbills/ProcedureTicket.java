package com.dg.checkbills;


import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

public class ProcedureTicket extends FragmentActivity
{
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private PhotoFragment photoFragment = null;
    private TicketInformation ticketInfoFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedure_ticket);
        dispatchTakePictureIntent();
    }

<<<<<<< HEAD

    private void dispatchTakePictureIntent()
    {
=======
    private void dispatchTakePictureIntent() {
>>>>>>> b79c4cd50151fadedabfc3bd507a4d3b08b420ad
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void getNext() {
        photoFragment.getView().setVisibility(View.INVISIBLE);
        ticketInfoFragment.getView().setVisibility(View.VISIBLE);
    }

    @Override
<<<<<<< HEAD
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            myImage.setImageBitmap(imageBitmap);
        }
=======
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                photoFragment = (PhotoFragment) getSupportFragmentManager().findFragmentById(R.id.scanFragment);
                photoFragment.putPhoto(imageBitmap);

                ticketInfoFragment = (TicketInformation) getSupportFragmentManager().findFragmentById(R.id.ticketInfoFragment);
                ticketInfoFragment.getView().setVisibility(View.INVISIBLE);
            }

              /*  if(photoFragment.getIsValid()) {  // CA VIENT DU MANIFEST ..
                    photoFragment.getView().setVisibility(View.INVISIBLE);
                    ticketInfoFragment.getView().setVisibility(View.VISIBLE);
                    // Begin the transaction
                   FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.ticketInfoFragment, new TicketInformation());

                    // or ft.add(R.id.your_placeholder, new FooFragment());
                    // Complete the changes added above
                    ft.commit();*/
                    //  photoFragment.onClick(this);  // A revoir

                }


>>>>>>> b79c4cd50151fadedabfc3bd507a4d3b08b420ad
    }





