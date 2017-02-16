package com.dg.checkbills.AjoutTicket;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.R;

import java.util.ArrayList;

/**
 * Use the {@link SelectionBoutiqueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectionBoutiqueFragment extends Fragment
{
    private ListView aListViewBoutiques;
    private EditText inputBoutique;
    private ArrayList<Boutique> listofBoutiques = new ArrayList<>();
    private workerListingBoutique worker;

    public SelectionBoutiqueFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SelectionBoutiqueFragment.
     */
    public static SelectionBoutiqueFragment newInstance(ArrayList<Boutique> boutiqueArrayList)
    {
        SelectionBoutiqueFragment fragment = new SelectionBoutiqueFragment();
        fragment.listofBoutiques = boutiqueArrayList;
        return fragment;
    }

    public void setListofBoutiques(ArrayList<Boutique> listeboutiquesP)
    {
        Log.e("sizelistboutique",String.valueOf(listeboutiquesP.size()));
        listofBoutiques = listeboutiquesP;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_selection_boutique_fab, container, false);

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fabnwboutique);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProcedureTicket) getActivity()).showNewBoutique();
            }
        });

        aListViewBoutiques = (ListView) v.findViewById(R.id.listViewBoutiques);

        aListViewBoutiques.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String nomBoutique = ( (TextView) view.findViewById(R.id.nomBoutique)).getText().toString();
                for (Boutique boutique : listofBoutiques)
                {
                    if (boutique.getNom().equals(nomBoutique))
                    {
                        ((ProcedureTicket) getActivity()).showTicketInfo(nomBoutique,Integer.valueOf(boutique.getId()));
                    }
                }
            }
        });

        inputBoutique = (EditText) v.findViewById(R.id.editTextBoutiques);
        inputBoutique.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (worker != null)
                {
                    worker.cancel(true);
                }
                worker = new workerListingBoutique();
                worker.setText(s.toString());
                worker.execute();
            }
        });
        return v;
    }

    public void updateListing(AdapterListingBoutiques adapterListingBoutiques)
    {
        if (aListViewBoutiques !=  null)
        {
            aListViewBoutiques.setAdapter(adapterListingBoutiques);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private class workerListingBoutique extends AsyncTask
    {
        ArrayList<Boutique> listOfBoutiqueSelected;
        String text;

        public void setText(String text)
        {
            this.text = text;
        }

        @Override
        protected Object doInBackground(Object[] words)
        {
            listOfBoutiqueSelected = new ArrayList<>();

            for(Boutique b : listofBoutiques)
            {
                if (b.getNom().contains(this.text))
                {
                    listOfBoutiqueSelected.add(b);
                }
            }
            return null;

        }

        protected void onPostExecute(Object result)
        {
            final AdapterListingBoutiques listingBoutiques = new AdapterListingBoutiques(
                    getActivity(),R.layout.item_listing_historique,listOfBoutiqueSelected );
            updateListing(listingBoutiques);
        }
    }
}
