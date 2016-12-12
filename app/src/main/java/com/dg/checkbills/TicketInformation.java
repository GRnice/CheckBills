package com.dg.checkbills;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.dg.checkbills.Daemon.ServiceSocket;
import com.dg.checkbills.Data.Boutique;

import java.util.Date;


public class TicketInformation extends Fragment
{
    private Button buttonValidation;
    private Spinner marketPlaceSpinner;
    private Spinner typeAchatSpinner;
    private TextView date;

    private String ticketDate;
    private Location longLat;




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
        marketPlaceSpinner = (Spinner) v.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.market_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        marketPlaceSpinner.setAdapter(adapter);

        typeAchatSpinner = (Spinner) v.findViewById(R.id.spinner1);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterAchat = ArrayAdapter.createFromResource(getActivity(), R.array.type_achat, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterAchat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        typeAchatSpinner.setAdapter(adapterAchat);


        buttonValidation = (Button) v.findViewById(R.id.buttonValiderTicket);
        buttonValidation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(ServiceSocket.ACTION_TO_SERVICE_FROM_ACTIVITY);
                intent.putExtra("NEWBILL",true);
                intent.putExtra("MONTANT",50);
                intent.putExtra("NOM","Un Ticket");
                intent.putExtra("BOUTIQUE",new Boutique());
                intent.putExtra("DATE",new Date());
                getActivity().sendBroadcast(intent);
            }
        });

        // Inflate the layout for this fragment
        return v;
    }

    public void saveTicket()
    {
        // ICI on sauve le ticket
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

    public void setLocation(Location loc) {
        longLat = loc;
    }

    public void setTicketDate(String date) {
        ticketDate = date;
        this.date.setText(ticketDate);
    }




}
