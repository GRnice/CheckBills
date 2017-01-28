package com.dg.checkbills.Consommation;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Historique.HistoriqueActivity;
import com.dg.checkbills.Historique.HistoriqueListingFragment;
import com.dg.checkbills.Home;
import com.dg.checkbills.R;
import com.dg.checkbills.Storage.StatManager;

import java.util.ArrayList;

public class ConsoActivity extends AppCompatActivity
{

    private Fragment fragmentCourant = null;
    private ArrayList<Statistiques> arrayStats;
    private int cursor = 0;
    private ServiceReceiver serviceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());
        registerReceiver(serviceReceiver,intentFilter);

        Intent nwIntent = new Intent();
        nwIntent.putExtra("GETHISTORIQUE","");
        nwIntent.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
        sendBroadcast(nwIntent);

        setContentView(R.layout.activity_conso);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(serviceReceiver);
    }

    @Override
    public void onBackPressed()
    {
        Intent goHome = new Intent(this,Home.class);
        startActivity(goHome);
        finish();
    }

    public void pushFragment(Fragment frag)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (fragmentCourant != null)
        {
            ft.setCustomAnimations(android.R.animator.fade_in,android.R.animator.fade_out);
            ft.setCustomAnimations(android.R.animator.fade_in,android.R.animator.fade_out);
            ft.replace(R.id.fragmentStat,frag);
            ft.commit();
        }
        else
        {
            ft.add(R.id.fragmentStat,frag);
            ft.commit();
        }
        fragmentCourant = frag;
    }

    public void swipeReceiver(int orientation)
    {
        if (orientation == 0)
        {
            if (cursor > 0)
            {
                cursor--;
                pushFragment(StatFragment.newInstance(ConsoActivity.this,arrayStats.get(cursor)));
            }
        }
        else
        {
            if (cursor < arrayStats.size()-1)
            {
                cursor++;
                pushFragment(StatFragment.newInstance(ConsoActivity.this,arrayStats.get(cursor)));
            }
            // to right ->
        }
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
            if (arg1.hasExtra("HistoriqueSerialize"))
            {
                arrayStats = (ArrayList<Statistiques>) arg1.getSerializableExtra("HistoriqueSerialize");
                Log.e("EP-store","03");
                Log.e("sizearrayDATA",String.valueOf(arrayStats.size()));
                if (arrayStats.size() > 0)
                {
                    cursor = arrayStats.size()-1;
                    pushFragment(StatFragment.newInstance(ConsoActivity.this,arrayStats.get(cursor)));
                }

                Log.e("BIENRECUSTAT","??");
            }
        }
    }
}
