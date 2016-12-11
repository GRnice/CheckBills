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

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Remy on 03/11/2016.
 */
public class HistoriqueListingFragment extends FragmentHistorique
{
    ArrayList<String> arrayticket;
    ArrayList<Bill> arrayObjectBill;
    BillsManager managerData;
    ListView listingView;

    public HistoriqueListingFragment()
    {
        super();
    }

    public void updateListing(ArrayList<Bill> array)
    {
        this.arrayObjectBill = array;
        final AdapterListing listingAdapter = new AdapterListing(getActivity(),R.layout.item_listing_historique,this.arrayObjectBill );
        listingView.setAdapter(listingAdapter);
    }

    @Override
    public void onCreate(Bundle saved)
    {
        super.onCreate(saved);
        this.title = "Historique";
        this.arrayticket = new ArrayList<>();
        this.managerData = new BillsManager();
        AsynckTaskLoadBills asynckTaskLoadBills = new AsynckTaskLoadBills();
        asynckTaskLoadBills.execute(this.managerData);
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
        return v;
    }

    public class AsynckTaskLoadBills extends AsyncTask<BillsManager,Void,Void>
    {
        ArrayList<Bill> listing;

        @Override
        protected Void doInBackground(BillsManager... params)
        {
            BillsManager billsManager = params[0];
            listing = billsManager.load(getActivity());
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            updateListing(listing);
        }
    }

}
