package com.dg.checkbills.AjoutTicket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.dg.checkbills.Consommation.ConsoActivity;
import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.R;
import com.dg.checkbills.Storage.StatManager;

import java.io.ByteArrayOutputStream;


public class TicketInformation extends Fragment
{
    private Button buttonValidation;

    private Button selectionBoutique;
    private Spinner typeAchatSpinner;
    private TextView date;
    private EditText editTextMontant;
    private EditText editTextNomBillet;
    private Bitmap imgTicket;
    private String ticketDate;




    // Required empty public constructor
    public TicketInformation() { }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_ticket_information, container, false);

        date = (TextView) v.findViewById(R.id.date);
        selectionBoutique = (Button) v.findViewById(R.id.button_select_boutique);

        selectionBoutique.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProcedureTicket) getActivity()).showListingBoutiques();
            }
        });

        typeAchatSpinner = (Spinner) v.findViewById(R.id.spinner1);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterAchat = ArrayAdapter.createFromResource(getActivity(), R.array.type_achat, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterAchat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        typeAchatSpinner.setAdapter(adapterAchat);

        editTextMontant = ((EditText) v.findViewById(R.id.editTextMontant));
        editTextNomBillet = ((EditText) v.findViewById(R.id.editTextNomTicket));

        buttonValidation = (Button) v.findViewById(R.id.buttonValiderTicket);
        buttonValidation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());

                ProcedureTicket activity = (ProcedureTicket) getActivity();
                intent.putExtra("IDTEL",activity.getAndroidId());
                intent.putExtra("NEWBILL",true);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imgTicket.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                String montant = editTextMontant.getText().toString().trim();
                String nomTicket = editTextNomBillet.getText().toString().trim();
                Log.e("M%ONTNY",montant);
                Log.e("SIZE",String.valueOf(montant.length()));
                Bill nwBill = new Bill(typeAchatSpinner.getSelectedItem().toString()
                        ,nomTicket
                        ,Double.parseDouble(montant)
                        ,new Boutique("1",selectionBoutique.getText().toString())
                        ,ticketDate
                        ,byteArray);

                intent.putExtra("BILL",nwBill);
                getActivity().sendBroadcast(intent);
                ((ProcedureTicket) getActivity()).backToHome();
            }
        });

        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            //      mListener = (FragmentActivity) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //   mListener = null;
    }


    public void setTicketDate(String date) {
        ticketDate = date;
        this.date.setText(ticketDate);
    }

    public void setBoutiqueSelected(String b)
    {
        selectionBoutique.setText(b);
    }

    public void setImageTicket(Bitmap ticketBitmap)
    {
        imgTicket = ticketBitmap;
    }


}
