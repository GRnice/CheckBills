package com.dg.checkbills.Historique;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.R;


import java.util.ArrayList;

/**
 * Created by Remy on 11/12/2016.
 */

public class AdapterListing extends ArrayAdapter<Bill>
{

    public AdapterListing(Context context,int layoutid, ArrayList<Bill> resource)
    {
        super(context,layoutid,resource);
    }

    @Override
    public View getView(int position, View convertView,ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_listing_historique, parent, false);

        TextView nomTicket = (TextView) rowView.findViewById(R.id.nomTicket);
        TextView dateTicket = (TextView) rowView.findViewById(R.id.dateTicket);
        TextView prixTicket = (TextView) rowView.findViewById(R.id.item_price);

        Bill unTicket = this.getItem(position);

        nomTicket.setText(unTicket.getNom());
        dateTicket.setText(unTicket.getDate());
        prixTicket.setText(String.valueOf(unTicket.getMontant()) + "€");

        return rowView;
    }
}
