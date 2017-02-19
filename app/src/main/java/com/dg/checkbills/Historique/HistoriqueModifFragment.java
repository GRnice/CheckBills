package com.dg.checkbills.Historique;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dg.checkbills.Communication.NetworkUtil;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.R;

/**
 * Created by Remy on 09/02/2017.
 */

public class HistoriqueModifFragment extends FragmentHistorique
{
    private Bill oldBill;

    public HistoriqueModifFragment()
    {

    }

    public static HistoriqueModifFragment newInstance(Bill bill)
    {
        HistoriqueModifFragment modifFrag = new HistoriqueModifFragment();
        modifFrag.oldBill = bill;
        return modifFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.frag_historique_modification, container, false);
        final EditText montantEdit = (EditText) v.findViewById(R.id.montantEditDetailHistorique);
        montantEdit.setText(String.valueOf(oldBill.getMontant()));

        final EditText titreEdit = (EditText) v.findViewById(R.id.titleDetailHistoriqueModif);
        titreEdit.setText(oldBill.getNom());

        Button btnModif = (Button) v.findViewById(R.id.butttonValiderModification);
        btnModif.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (NetworkUtil.getConnectivityStatus(getActivity()) == 0)
                {
                    Toast.makeText(getActivity(),"Activez internet",Toast.LENGTH_LONG).show();
                    return;
                }
                String titre = titreEdit.getText().toString();
                String montant = montantEdit.getText().toString();
                if (titre.equals(oldBill.getNom()) && montant.equals(oldBill.getMontant()))
                {
                    ((HistoriqueActivity) getActivity()).ticketSelected(oldBill);
                    return;
                }
                Bill nwBill = oldBill.clone();
                nwBill.setMontant(Double.valueOf(montant));
                nwBill.setTitre(titre);
                ((HistoriqueActivity) getActivity()).modificationBill(oldBill,nwBill);

                return;
            }
        });
        return v;
    }
}
