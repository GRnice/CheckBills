package com.dg.checkbills.Historique;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.Home;
import com.dg.checkbills.R;

import java.util.ArrayList;

public class HistoriqueActivity extends AppCompatActivity
{
    private FragmentHistorique fragmentCourant = null;
    private Toolbar barreDuTitre = null;
    private ServiceReceiver serviceReceiver;

    private ArrayList<Bill> arrayBill;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        barreDuTitre = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(barreDuTitre);

        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());
        registerReceiver(serviceReceiver,intentFilter);

        Log.e("TRYY","TRYY");
        Intent intent = new Intent();
        intent.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
        intent.putExtra("GETBILLS",true);

        sendBroadcast(intent);


    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(serviceReceiver);
        super.onDestroy();
        Log.e("ondestroy","§§§§§§§");

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
            HistoriqueListingFragment fragListing = new HistoriqueListingFragment();
            fragListing.setArrayBills(this.arrayBill);
            setFragment(fragListing);
        }
        else
        {
            Intent intent = new Intent(this,Home.class);
            startActivity(intent);
            finish();
        }
    }
    public void ticketSelected(Bill billselected)
    {
        HistoriqueDetailFragment fh = new HistoriqueDetailFragment();
        fh.setCheckedBill(billselected);
        setFragment(fh);
    }

    /**
     * ServiceReceiver , recoit les messages venants du serveur
     *
     * ACTION_RECEIVE_FROM_SERVICE
     */
    private class ServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            Log.e("GGII","??");
            if (arg1.hasExtra("BILLS"))
            {
                arrayBill = (ArrayList<Bill>) arg1.getSerializableExtra("BILLS");
                Log.e("BIENRECU","??");
                HistoriqueListingFragment fragListing = new HistoriqueListingFragment();
                fragListing.setArrayBills(arrayBill);
                setFragment(fragListing);
            }
        }
    }

}
