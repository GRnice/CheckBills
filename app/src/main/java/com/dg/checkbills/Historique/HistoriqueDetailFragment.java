package com.dg.checkbills.Historique;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.R;

/**
 * Created by Remy on 03/11/2016.
 */
public class HistoriqueDetailFragment extends FragmentHistorique
{

    private Bill bill;

    public HistoriqueDetailFragment()
    {
        super();
        this.title = "Detail";
    }

    public void setCheckedBill(Bill bill)
    {
        this.bill = bill;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.frag_historique_detail, container, false);

        TextView title = (TextView) view.findViewById(R.id.titleDetailHistorique);
        TextView date = (TextView) view.findViewById(R.id.dateDetailHistorique);
        TextView montant = (TextView) view.findViewById(R.id.montantDetailHistorique);
        title.setText(this.bill.getNom());
        date.setText(this.bill.getDate());
        montant.setText("Montant : "+String.valueOf(this.bill.getMontant())+"€");
        return view;
    }
}
