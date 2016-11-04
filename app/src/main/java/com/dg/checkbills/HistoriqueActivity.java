package com.dg.checkbills;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class HistoriqueActivity extends AppCompatActivity
{
    private FragmentHistorique fragmentCourant = null;
    private Toolbar barreDuTitre = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);
        barreDuTitre = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(barreDuTitre);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setFragment(new HistoriqueDetailFragment());
                            }
                        }).show();
            }
        });

        this.setFragment(new HistoriqueListingFragment());
    }

    public void setFragment(FragmentHistorique fg)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (fragmentCourant != null)
        {
            ft.remove(fragmentCourant);
            ft.commit();
        }
        FragmentTransaction ft2 = getFragmentManager().beginTransaction();
        ft2.replace(R.id.fragment,fg);
        ft2.commit();
        fragmentCourant = fg;
        setTitle(fg.getTitle());
    }

}
