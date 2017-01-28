package com.dg.checkbills.Consommation;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dg.checkbills.R;


public class StatFragment extends Fragment implements View.OnTouchListener
{
    private float downX,downY,upX,upY;
    private int MIN_DISTANCE = 100;
    private ConsoActivity act;
    private Statistiques stat;

    public StatFragment() {
        // Required empty public constructor
    }


    public static StatFragment newInstance(ConsoActivity activity,Statistiques stats)
    {
        StatFragment fragment = new StatFragment();
        fragment.act = activity;
        fragment.stat = stats;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_stat, container, false);
        TextView depenseTotal = (TextView) v.findViewById(R.id.tetxViewDepenseTotal);
        depenseTotal.setText(String.valueOf(stat.getDepenseTotal())+" €");

        TextView nombredeTickets = (TextView) v.findViewById(R.id.textViewNombreTickets);
        nombredeTickets.setText(String.valueOf(stat.getNbTickets()));
        stat.getDepenseTotal();
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
