package com.dg.checkbills;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Remy on 03/11/2016.
 */
public class HistoriqueListingFragment extends FragmentHistorique
{
    public HistoriqueListingFragment()
    {
        this.title = "Historique";
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.frag_historique_listing, container, false);
    }

}