package com.dg.checkbills.Daemon;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.dg.checkbills.Communication.CommunicationServer;
import com.dg.checkbills.Communication.NetworkUtil;
import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.Storage.BillsManager;
import com.dg.checkbills.Storage.BoutiqueManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Remy on 11/12/2016.
 */

public class ServiceSocket extends Service implements LocationListener
{

    private ActivityReceiver activityReceiver; // ecoute les messages émis par les differentes activity
    private NetworkChangeReceiver networkChangeReceiver;

    private ArrayList<Bill> billsArray; // tableau de tickets
    private ArrayList<Boutique> boutiqueArray; // tableau de boutiques
    private ArrayList<Sender> arrayOfSender; // tableau de sender en cours d'execution
    private ArrayList<Bill> billsOffline; // tableau de bills offline

    private boolean isConnected;

    private String idTel;

    private Location lastPosition;

    public ServiceSocket() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        activityReceiver = new ActivityReceiver();
        networkChangeReceiver = new NetworkChangeReceiver();

        idTel =  Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        billsArray = BillsManager.load(getBaseContext());
        boutiqueArray = BoutiqueManager.load(getBaseContext());
        billsOffline = new ArrayList<>();
        arrayOfSender = new ArrayList<>();

        for (Bill bill : billsArray)
        {
            if (!bill.isOnCloud())
            {
                billsOffline.add(bill);
            }
        }


        LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
            }
        }


        // ECOUTE DES MESSAGES PROVENANTS DE L'ACTIVITE
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
        registerReceiver(activityReceiver, intentFilter);

        // ECOUTE DU CHANGEMENT DE L'ETAT DU RESEAU
        intentFilter = new IntentFilter();
        intentFilter.addAction(NetworkChangeReceiver.CONNECTIVITY_CHANGED);
        registerReceiver(networkChangeReceiver, intentFilter);

        requestBoutique(); // demande de charger les dernieres boutiques
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.e("DEAD", "DEAD");

        LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lm.removeUpdates(this);
        }

        unregisterReceiver(activityReceiver);
        unregisterReceiver(networkChangeReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Cette methode demande au serveur de transmettre les boutiques à l'application
     */
    private void requestBoutique()
    {
        SenderRequest senderRequest = new SenderRequest(this,"REQUEST_ALL_BOUTIQUES");
        senderRequest.process();
        arrayOfSender.add(senderRequest);
    }

    /**
     * Traite les boutiques transmisent par le serveur
     */
    private void treatRequestBoutique(String boutiqueStringReceived)
    {
        Log.e("ALL_BOUTIQUE_RECEIVED", boutiqueStringReceived.toString());
        String[] allBoutiques = boutiqueStringReceived.toString().split("\\_");
        boutiqueArray = new ArrayList<>();
        for (String aBoutique : allBoutiques) {

            String[] aBoutiqueSplit = aBoutique.split("\\*"); // IDBOUTIQUE*id*NOM*nom
            Boutique nwBoutique = new Boutique(aBoutiqueSplit[1], aBoutiqueSplit[3]);
            BoutiqueManager.store(getBaseContext(), nwBoutique);
            this.boutiqueArray.add(nwBoutique);
        }

        Log.e("IZELOS", String.valueOf(boutiqueArray.size()));
    }

    /**
     * Envoie un ticket crée par l'utilisateur au serveur
     * @param idTel
     * @param myBill
     * @return
     */
    private void sendBill(String idTel, Bill myBill)
    {
        SenderBill senderBill = new SenderBill(this,myBill,idTel);
        this.idTel = idTel;
        senderBill.process();
        arrayOfSender.add(senderBill);
    }


    private void checkPositionAndSendNewBoutiqueToServer(String nomBoutique)
    {
        SenderBoutique senderBoutique = new SenderBoutique(this,nomBoutique,lastPosition);
        senderBoutique.process();
    }

    private void sendBillsNotOnCloud()
    {
        if (billsOffline.size() > 0)
        {
            Log.e("SENDBILLNOTONCLOUD","DANSLEIF");
            sendBill(idTel,billsOffline.get(0));
        }
    }

    @Override
    public void onLocationChanged(Location locationp)
    {
        Log.e("Nouvelle position","!!!");
        this.lastPosition = locationp;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void endTask(SenderBoutique senderBoutique,boolean success)
    {
        arrayOfSender.remove(senderBoutique);
    }

    public void endTask(SenderRequest senderRequest,boolean success,@Nullable String response)
    {
        arrayOfSender.remove(senderRequest);
        treatRequestBoutique(response);
    }

    public void endTask(SenderBill senderBill,Bill billSent, boolean success)
    {
        billSent.setIsOnCloud(success); // il a bien été émis
        billsArray.add(billSent);
        if (! success)
        {
            billsOffline.add(billSent);
        }
        if (billsOffline.contains(billSent)) // si il est contenu dans la liste des tickets offline
        {
            billsOffline.remove(billSent); // on le retire de la liste
            BillsManager.flush(this, billSent);
            BillsManager.store(this, billSent); // on le store
            if (billsOffline.size() > 0 && isConnected) // si il y a d'autres tickets offline on essaye de les transmettre.
            {
                sendBill(idTel,billsOffline.get(0));
            }
        }
        else
        {
            BillsManager.store(this, billSent);
        }
    }

    public boolean networkAvailable()
    {
        return isConnected;
    }

    /**
     * ClientReceiver , envoie des messages au serveur
     * NEWBILL
     * GETBILLS
     * GETBOUTIQUES
     * NEWBOUTIQUE
     * MessageReceiver, recoit les messages venants d'une activité
     */
    private class ActivityReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            // ICI on recoit les messages provenants d'une activité
            if (arg1.hasExtra("NEWBILL"))
            {
                String idTel = arg1.getStringExtra("IDTEL");
                Bill nwBill = (Bill) arg1.getSerializableExtra("BILL");
                sendBill(idTel,nwBill);
            }

            if (arg1.hasExtra("GETBILLS"))
            {
                Intent intentBills = new Intent();
                intentBills.setAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());
                intentBills.putExtra("BILLS",billsArray);
                sendBroadcast(intentBills);
            }

            if (arg1.hasExtra("GETBOUTIQUES"))
            {
                Intent intentBoutique = new Intent();
                intentBoutique.setAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());
                intentBoutique.putExtra("BOUTIQUES",boutiqueArray);
                sendBroadcast(intentBoutique);
            }

            if (arg1.hasExtra("NEWBOUTIQUE"))
            {
                String nomwBoutique = arg1.getStringExtra("NEWBOUTIQUE");
                checkPositionAndSendNewBoutiqueToServer(nomwBoutique);
            }
        }
    }

    /**
     * NetworkChangeReceiver , écoute les changements d'états
     */
    public class NetworkChangeReceiver extends BroadcastReceiver {

        public static final String CONNECTIVITY_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";

        @Override
        public void onReceive(final Context context, final Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            if (CONNECTIVITY_CHANGED.equals(intent.getAction()))
            {
                Log.e("CHANGED","WDD");
                if (status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED)
                {
                    Log.e("DISCONNECT","");
                    isConnected = false;
                }
                else if(status==NetworkUtil.NETWORK_STATUS_MOBILE || status== NetworkUtil.NETWORK_STATUS_WIFI)
                {
                    if(!isConnected)
                    {
                        Log.e("connected !","dd");
                        isConnected = true;
                        sendBillsNotOnCloud();
                    }
                }
            }
        }
    }



}

