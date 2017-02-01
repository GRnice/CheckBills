package com.dg.checkbills.Daemon;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.format.Time;
import android.util.Log;

import com.dg.checkbills.Communication.NetworkUtil;
import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.Data.ZoneInfluence;
import com.dg.checkbills.Storage.BillsManager;
import com.dg.checkbills.Storage.BoutiqueManager;
import com.dg.checkbills.Storage.StatManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Remy on 11/12/2016.
 */

public class ServiceSocket extends Service implements LocationListener
{

    private ActivityReceiver activityReceiver; // ecoute les messages émis par les differentes activity
    private NetworkChangeReceiver networkChangeReceiver;

    private ArrayList<Bill> billsArray; // tableau de tickets
    private ArrayList<Boutique> boutiqueArray; // tableau de boutiques
    private ArrayList<ZoneInfluence> arrayOfCentres; // tableau de centres
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
        arrayOfCentres = new ArrayList<>();
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
    public void onDestroy()
    {
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
        SenderRequest senderRequest = new SenderRequest(this,"REQUEST_ALL_BOUTIQUES","TAG_REQUEST-ALL-BOUTIQUES");
        senderRequest.process();
        arrayOfSender.add(senderRequest);
    }

    private void requestZonesInfluences(Date temps)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateJour = sdf.format(temps).split(" ")[0];
        String heure = sdf.format(temps).split(" ")[1].split("\\:")[0];
        int hour = Integer.valueOf(heure);
        String heureDepart = "";
        String heureFin = "";

        if(hour >= 8 && hour <= 12){
            heureDepart = dateJour + " " + "08:00:00";
            heureFin = dateJour + " " + "12:00:00";
        }
        else if(hour > 12 && hour <= 18) {
            heureDepart = dateJour + " " + "12:00:01";
            heureFin = dateJour + " " + "18:00:00";

        }
        else if(hour > 18 && hour < 24) {
            heureDepart = dateJour + " " + "18:00:01";
            heureFin = dateJour + " " + "23:59:59";
        }

        if(heureDepart.length() != 0 && heureFin.length() != 0) {
            String sendToServer = "GET_ZONES_INFLUENCES*" + heureDepart + "*" + heureFin;
            SenderRequest senderRequest = new SenderRequest(this, sendToServer, "TAG_REQUEST-ZONES-INFLUENCES");
            senderRequest.process();
            arrayOfSender.add(senderRequest);
        }

    }

    /**
     * Traite les centres transmis par le serveur
     */
    public void treatRequestZonesInfluences(String allCentres)
    {
        // syntaxe longitude,latitude,poids*...*longitude,latitude,poids
        allCentres = allCentres.trim();
        String[] tabCentres = allCentres.split("\\*");
        Log.e("treatRequestZones","778");
        Log.e("SIZEtabCentres",String.valueOf(tabCentres.length));
        arrayOfCentres = new ArrayList<>();
        for (String aCentre : tabCentres)
        {
            String[] args = aCentre.split("\\,");
            Log.e("treatReques len",String.valueOf(args.length));
            Log.e("POIDS ", String.valueOf(args[2]));
            ZoneInfluence zone = new ZoneInfluence(Double.valueOf(args[0]),Double.valueOf(args[1]),Integer.valueOf(args[2]));
            arrayOfCentres.add(zone);
        }
    }

    /**
     * Traite les boutiques transmisent par le serveur
     */
    private void treatRequestBoutique(String boutiqueStringReceived)
    {
        Log.e("ALL_BOUTIQUE_RECEIVED", boutiqueStringReceived.toString());
        String[] allBoutiques = boutiqueStringReceived.toString().split("\\_");
        boutiqueArray = new ArrayList<>();
        for (String aBoutique : allBoutiques)
        {
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
        if (success && senderRequest.getTag() == "TAG_REQUEST-ALL-BOUTIQUES")
        {
            treatRequestBoutique(response);
        }
        else if (success && senderRequest.getTag().equals("TAG_REQUEST-ZONES-INFLUENCES"))
        {
            treatRequestZonesInfluences(response);

            Intent intentBoutique = new Intent();
            intentBoutique.setAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());
            intentBoutique.putExtra("ZONES-INFLUENTES",arrayOfCentres);
            sendBroadcast(intentBoutique);
        }

    }

    public void endTask(SenderBill senderBill,Bill billSent, boolean success)
    {
        arrayOfSender.remove(senderBill);
        billSent.setIsOnCloud(success); // il a bien été émis ou non
        billsArray.add(billSent);
        if (success == false && ! billsOffline.contains(billSent)) // si echec et le ticket n'est pas deja dans la liste des tickets offlines
        {
            billsOffline.add(billSent);
            BillsManager.flush(this, billSent);
            BillsManager.store(this, billSent); // on le store
        }
        else if (success && billsOffline.contains(billSent)) // si success et si billsent est contenu dans la liste des tickets offline
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
            BillsManager.flush(this, billSent);
            BillsManager.store(this, billSent); // on le store
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

            if (arg1.hasExtra("GET_ZONES_INFLUENCES"))
            {
                Log.e("EPISODE-01","zones");
                Date d = Calendar.getInstance().getTime();
                requestZonesInfluences(d);

               /* Intent intentBoutique = new Intent();
                intentBoutique.setAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());
                intentBoutique.putExtra("ZONES-INFLUENTES",arrayOfCentres);
                sendBroadcast(intentBoutique);*/
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
                Log.e("statenet",""+status);
                if (status == 0)
                {
                    Log.e("DISCO","youuuuu");
                    isConnected = false;
                }
                else if(status==NetworkUtil.NETWORK_STATUS_MOBILE || status== NetworkUtil.NETWORK_STATUS_WIFI)
                {
                    Log.e("darkk","vad");
                    if(!isConnected)
                    {
                        Log.e("connected !","dd");
                        sendBillsNotOnCloud();
                    }

                    isConnected = true;
                }
            }
        }
    }
}