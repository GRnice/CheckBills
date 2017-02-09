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
import com.dg.checkbills.Storage.ImageManager;

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
            finish();
        }
    }
    public void ticketSelected(Bill billselected)
    {
        Intent demandeImage = new Intent();
        demandeImage.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
        demandeImage.putExtra("REQUEST-IMG",billselected.getNomImage());
        sendBroadcast(demandeImage);

        HistoriqueDetailFragment fh = new HistoriqueDetailFragment();
        fh.setCheckedBill(billselected);
        setFragment(fh);

    }

    public void modificationBill(Bill old,Bill nwBill)
    {
        arrayBill.remove(old);
        arrayBill.add(nwBill);
        nwBill.setImage(null);

        Intent modifTicket = new Intent();
        modifTicket.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
        modifTicket.putExtra("REQUEST-MODIF",nwBill);
        sendBroadcast(modifTicket);

        ticketSelected(nwBill);
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
                HistoriqueListingFragment fragListing = new HistoriqueListingFragment();
                fragListing.setArrayBills(arrayBill);
                setFragment(fragListing);
            }

            if (arg1.hasExtra("IMG"))
            {
                Log.e("Episode 4","requestImage");
                if (fragmentCourant instanceof HistoriqueDetailFragment)
                {
                    HistoriqueDetailFragment frag = (HistoriqueDetailFragment) fragmentCourant;
                    String nomImage = arg1.getStringExtra("IMG");
                    Log.e("Historique Activity",nomImage);
                    ArrayList<Byte> image = ImageManager.loadImage(getBaseContext(),nomImage);
                    frag.setImage(image);
                }
            }
        }
    }

}
