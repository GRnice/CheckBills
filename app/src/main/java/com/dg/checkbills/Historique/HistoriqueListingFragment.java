package com.dg.checkbills.Historique;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.R;
import com.dg.checkbills.Storage.BillsManager;

import java.io.IOError;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Remy on 03/11/2016.
 */
public class HistoriqueListingFragment extends FragmentHistorique
{
    ArrayList<Bill> arrayObjectBill;
    ListView listingView = null;

    public HistoriqueListingFragment()
    {
        super();
    }

    public void setArrayBills(ArrayList<Bill> arrayBill)
    {
        this.arrayObjectBill = arrayBill;
        if (listingView != null)
        {
            final AdapterListing listingAdapter = new AdapterListing(getActivity(),R.layout.item_listing_historique,this.arrayObjectBill );
            listingView.setAdapter(listingAdapter);
        }

    }

    @Override
    public void onCreate(Bundle saved)
    {
        super.onCreate(saved);
        this.title = "Historique";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.frag_historique_listing, container, false);
        listingView = (ListView) v.findViewById(R.id.list_historique);
        listingView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                HistoriqueActivity historiqueAct = (HistoriqueActivity) getActivity();
                historiqueAct.ticketSelected(arrayObjectBill.get(position));
            }
        });

        if (this.arrayObjectBill != null)
        {
            final AdapterListing listingAdapter = new AdapterListing(getActivity(),R.layout.item_listing_historique,this.arrayObjectBill );
            listingView.setAdapter(listingAdapter);
        }
        return v;
    }

}
