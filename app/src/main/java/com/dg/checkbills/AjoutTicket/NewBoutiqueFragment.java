package com.dg.checkbills.AjoutTicket;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.R;


public class NewBoutiqueFragment extends Fragment {

    private EditText nomBoutique;

    public NewBoutiqueFragment() {
        // Required empty public constructor
    }


    public static NewBoutiqueFragment newInstance(String param1, String param2) {
        NewBoutiqueFragment fragment = new NewBoutiqueFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_new_boutique, container, false);

        nomBoutique = (EditText) v.findViewById(R.id.nomNouvelleBoutique);

        Button boutonAddBoutique = (Button) v.findViewById(R.id.buttonaddboutique);
        boutonAddBoutique.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentNwBoutique = new Intent();
                intentNwBoutique.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
                intentNwBoutique.putExtra("NEWBOUTIQUE",nomBoutique.getText().toString());
                getActivity().sendBroadcast(intentNwBoutique);
                ((ProcedureTicket) getActivity()).showTicketInfo(nomBoutique.getText().toString());
            }
        });
        return v;
    }

}
