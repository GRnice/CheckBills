package com.dg.checkbills.Daemon;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Storage.StatManager;

import java.util.ArrayList;

public class ServiceStorage extends Service
{
    private StatManager statManager;
    private ActivityReceiver activityReceiver;

    public ServiceStorage()
    {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        Log.e("EP-store","00");
        statManager = new StatManager(getSharedPreferences("HistoriqueConsommation",Context.MODE_PRIVATE));
        activityReceiver = new ActivityReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
        registerReceiver(activityReceiver, intentFilter);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.e("YOLO","CESTLAFIN");
        statManager.saveAll();
        unregisterReceiver(activityReceiver);
    }

    private class ActivityReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // ICI on recoit les messages provenants d'une activité
            if (arg1.hasExtra("NEWBILL"))
            {
                Bill nwBill = (Bill) arg1.getSerializableExtra("BILL");
                statManager.storeBill(nwBill);
                Log.e("EP-store","01");
            }
            else if (arg1.hasExtra("GETHISTORIQUE"))
            {
                Log.e("EP-store","02");
                Intent nwIntent = new Intent();
                nwIntent.putExtra("HistoriqueSerialize",statManager.getStats());
                nwIntent.setAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());
                sendBroadcast(nwIntent);
            }
        }
    }
}
