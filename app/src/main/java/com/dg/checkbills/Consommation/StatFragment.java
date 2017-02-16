package com.dg.checkbills.Consommation;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.dg.checkbills.R;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class StatFragment extends Fragment implements View.OnTouchListener
{
    private float downX,downY,upX,upY;
    private int MIN_DISTANCE = 100;
    private ConsoActivity act;
    private Statistiques stat;
    private Spinner spinnerDate;
    private ArrayList<String> allPeriodes;
    private int selectedDefaultSpinner;

    public StatFragment() {
        // Required empty public constructor
    }


    public static StatFragment newInstance(ConsoActivity activity,Statistiques stats,ArrayList<String> allPeriodes,int selectedDefaultSpinner)
    {
        StatFragment fragment = new StatFragment();
        fragment.act = activity;
        fragment.stat = stats;
        fragment.allPeriodes = allPeriodes;
        fragment.selectedDefaultSpinner = selectedDefaultSpinner;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Statistiques de consommation");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_stat, container, false);
        TextView depenseTotal = (TextView) v.findViewById(R.id.tetxViewDepenseTotal);
        depenseTotal.setText(String.valueOf(stat.getDepenseTotal())+" €");


        ArrayAdapter<String> adp1=new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,allPeriodes);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerDate = (Spinner) v.findViewById(R.id.spinnerDate);
        spinnerDate.setAdapter(adp1);
        spinnerDate.setSelection(selectedDefaultSpinner);
        Log.e("ONCREATE","StatFragment");
        spinnerDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                act.dateSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                Log.e("noChange","");
            }
        });
/*
        spinnerDate.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                String date = ((TextView) v.findViewById(android.R.id.text1)).getText().toString();
                act.dateSelected(date);
                return false;
            }
        });
*/

        TextView nombredeTickets = (TextView) v.findViewById(R.id.textViewNombreTickets);
        nombredeTickets.setText(String.valueOf(stat.getNbTickets()));
        DrawerStats drawer = (DrawerStats) v.findViewById(R.id.drawerStat);
        drawer.setData(stat.getConsoLoisir(),stat.getConsoPro(),stat.getConsoAlimentaire(),stat.getConsoVoyage());
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return StatFragment.this.onTouch(v,event);
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public boolean onTouch(View v,MotionEvent event)
    {
        Log.e("action",String.valueOf(event.toString()));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return true; // allow other events like Click to be processed
            }
            case MotionEvent.ACTION_UP: {
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;
                Log.e("math abs deltax",String.valueOf(Math.abs(deltaX)));
                // horizontal swipe detection
                if (Math.abs(deltaX) > MIN_DISTANCE)
                {
                    // left or right
                    if (deltaX < 0)
                    {
                        // left to right
                        Log.e("TORIGHT","TORIGHT");
                        act.swipeReceiver(1);
                        return false;
                    }
                    if (deltaX > 0)
                    {
                        // right to left
                        Log.e("TOLEFT","TOLEFT");
                        act.swipeReceiver(0);
                        return false;
                    }
                }

                return false;
            }
        }
        return true;
    }

}
