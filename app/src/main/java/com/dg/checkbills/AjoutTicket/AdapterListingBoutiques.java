package com.dg.checkbills.AjoutTicket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.R;

import java.util.ArrayList;

/**
 * Created by Remy on 23/12/2016.
 */

public class AdapterListingBoutiques extends ArrayAdapter<Boutique>
{

    public AdapterListingBoutiques(Context context, int resource, ArrayList<Boutique> objects)
    {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_listing_boutique, parent, false);

        TextView nomBoutique = (TextView) rowView.findViewById(R.id.nomBoutique);

        Boutique uneBoutique = this.getItem(position);

        nomBoutique.setText(uneBoutique.getNom());

        return rowView;
    }
}
