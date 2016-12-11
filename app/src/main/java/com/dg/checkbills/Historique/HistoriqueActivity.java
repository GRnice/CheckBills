package com.dg.checkbills.Historique;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Home;
import com.dg.checkbills.R;

public class HistoriqueActivity extends AppCompatActivity
{
    private FragmentHistorique fragmentCourant = null;
    private Toolbar barreDuTitre = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);
        barreDuTitre = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(barreDuTitre);

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

    @Override
    public void onBackPressed()
    {
        if (this.fragmentCourant instanceof HistoriqueDetailFragment)
        {
            setFragment(new HistoriqueListingFragment());
        }
        else
        {
            Intent intent = new Intent(this,Home.class);
            startActivity(intent);
        }
    }
    public void ticketSelected(Bill billselected)
    {
        HistoriqueDetailFragment fh = new HistoriqueDetailFragment();
        fh.setCheckedBill(billselected);
        setFragment(fh);
    }

}
