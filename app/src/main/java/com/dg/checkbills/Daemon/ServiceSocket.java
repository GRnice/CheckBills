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
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
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

public class ServiceSocket extends Service implements TimerListener, LocationListener
{

    private CommunicationServer comm; // permet de dialoguer avec le serveur
    private ActivityReceiver activityReceiver; // ecoute les messages émis par les differentes activity
    private ServerReceiver serverReceiver; // ecoute les messages émis par le serveur
    private NetworkChangeReceiver networkChangeReceiver;

    private ArrayList<Bill> billsArray; // tableau de tickets
    private ArrayList<Boutique> boutiqueArray; // tableau de boutiques

    private boolean isConnected;

    private StringBuilder boutiqueStringReceived; // contient toutes les boutiques transmisent sous forme de string
    private Bill billSending;
    private Timer aTimer;
    private String idTel;

    private Location lastPosition;

    public ServiceSocket() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        activityReceiver = new ActivityReceiver();
        serverReceiver = new ServerReceiver();
        networkChangeReceiver = new NetworkChangeReceiver();

        idTel =  Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        billsArray = BillsManager.load(getBaseContext());
        boutiqueArray = BoutiqueManager.load(getBaseContext());

        LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
            }
        }


        // ECOUTE DES MESSAGES PROVENANTS DU SERVEUR
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_SERVER.getAddr());
        registerReceiver(serverReceiver, intentFilter);

        // ECOUTE DES MESSAGES PROVENANTS DE L'ACTIVITE
        intentFilter = new IntentFilter();
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
        comm.interrupt();
        LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lm.removeUpdates(this);
        }

        unregisterReceiver(activityReceiver);
        unregisterReceiver(serverReceiver);
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
    private void requestBoutique() {
        boutiqueStringReceived = new StringBuilder();

        comm = new CommunicationServer(this, "REQUESTBOUTIQUE", BroadcastAddr.ACTION_TO_SERVICE_FROM_SERVER.getAddr());
        comm.start();
        try
        {
            Thread.sleep(3000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        startTimer("REQUEST_BOUTIQUE_TIMER", 10); // si au bout de 10 secondes tout n'a pas été transféré on
        // arrete la communication
        try
        {
            comm.sendMessage("REQUEST_ALL_BOUTIQUES");
        } catch (IOException e)
        {
            stopTimer();
            comm = null;
        }
    }

    /**
     * Traite les boutiques transmisent par le serveur
     */
    private void treatRequestBoutique() {
        stopTimer();
        comm.interrupt(); // arret du socket dédié au REQUEST_ALL_BOUTIQUES
        comm = null;
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
     * Traite les erreurs de connexion
     */
    public void treatFailSocket() {
        if (aTimer != null) {
            stopTimer();
        }
        if (comm.getTag().equals("REQUESTBOUTIQUE")) {
            boutiqueStringReceived = new StringBuilder();
            comm.interrupt();
            comm = null;
        } else if (comm.getTag().equals("SENDBILL")) {
            comm.interrupt();
            billSending.setIsOnCloud(false); // pas émis
            billsArray.add(billSending); // mais quand meme ajouté
            billSending = null;
            comm = null;
        }
    }

    private void startTimer(String tag, int secondes) {
        aTimer = new Timer();
        aTimer.setTag(tag);
        aTimer.setTimer(secondes);
        aTimer.setTimerListener(this);
        aTimer.execute();
    }

    private void stopTimer() {
        aTimer.cancel(true);
        aTimer = null;
    }


    /**
     * Envoie un ticket crée par l'utilisateur au serveur
     * @param idTel
     * @param myBill
     * @return
     */
    private boolean sendBill(String idTel, Bill myBill)
    {
        BillsManager.store(getBaseContext(), myBill);
        if (!isConnected)
        {
            billSending.setIsOnCloud(false); // il n'a pas été transmis
            billsArray.add(billSending);

            SharedPreferences sharedPreferences = getSharedPreferences("users",Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            if (! sharedPreferences.contains("ticketNotOnCloud"))
            {
                edit.putStringSet("ticketNotOnCloud",new HashSet<String>());
                edit.commit();
            }

            Set<String> ticketsNotOnCloud = sharedPreferences.getStringSet("ticketNotOnCloud",null);
            ticketsNotOnCloud.add(myBill.getId());
            edit.putStringSet("ticketNotOnCloud",ticketsNotOnCloud);
            edit.commit();

            return false;
        }
        else
        {
            CommunicationServer comm = new CommunicationServer(this, "SENDBILL", BroadcastAddr.ACTION_TO_SERVICE_FROM_SERVER.getAddr());
            comm.start();

            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                billSending.setIsOnCloud(false); // il a bien été émis
                billsArray.add(billSending);
                comm.interrupt();
                comm = null;
                return false;
            }

            boolean res = false;
            try {
                res = comm.sendMessage("ID*" + idTel + "*DATE*" + myBill.getDate() + "*MONTANT*" + String.valueOf(myBill.getMontant())
                        + "*IDBOUTIQUE*" + myBill.getBoutique().getId() + "*TITRE*" + myBill.getNom() +
                        "*TYPEBILL*" + myBill.getType() + "*SIZEIMAGE*" + myBill.getImage().length + "*IMAGENAME*SzPdslWmd");
            } catch (IOException e) {
                billSending.setIsOnCloud(false); // il a bien été émis
                billsArray.add(billSending);
                comm.interrupt();
                comm = null;
            }

            if (!res)
            {
                billSending.setIsOnCloud(false); // il a bien été émis
                billsArray.add(billSending);
                comm.interrupt();
                comm = null;
                return false;
            }

            try
            {
                comm.sendMessage(myBill.getImage());
            }
            catch (IOException e) {
                e.printStackTrace();
                billSending.setIsOnCloud(false); // il a bien été émis
                billsArray.add(billSending);
                comm.interrupt();
                comm = null;
                return false;
            }

            billSending.setIsOnCloud(true); // il a bien été émis
            billsArray.add(billSending);
            return true;
        }



    }

    /**
     * Methode appellée par un asynctask qui a compté n secondes
     * @param tag
     */
    @Override
    public void timeout(String tag) {
        if (comm != null) {
            Log.e("TIMEOUT!!", "STOPSOCKET");
            treatFailSocket();
        }
    }

    private void checkPositionAndSendNewBoutiqueToServer(String nomBoutique)
    {
        /**
         * ICI RECUPERER POSITION GPS ET TRANSMETTRE AU SERVEUR
         */

        this.comm = new CommunicationServer(this, "SENDNEWBOUTIQUE", BroadcastAddr.ACTION_TO_SERVICE_FROM_SERVER.getAddr());

        comm.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            this.comm.sendMessage("NEWBOUTIQUE*"+nomBoutique+"*LONG*"
                    +lastPosition.getLongitude()+"*LAT*"+lastPosition.getLatitude());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendBillsNotOnCloud()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("users",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        if (! sharedPreferences.contains("ticketNotOnCloud"))
        {
            edit.putStringSet("ticketNotOnCloud",new HashSet<String>());
            edit.commit();
        }

        Set<String> ticketsNotOnCloud = sharedPreferences.getStringSet("ticketNotOnCloud",null);
        Iterator<String> tickStringIterator = ticketsNotOnCloud.iterator();
        while(tickStringIterator.hasNext())
        {
            String idBillOffline = tickStringIterator.next();
            Bill billOffline = null;

            for (int j = 0 ; j < billsArray.size() ; j++)
            {
                if (billsArray.get(j).getId().equals(idBillOffline))
                {
                    billOffline = billsArray.get(j);

                }
            }
            if (isConnected)
            {
                if (sendBill(idTel,billOffline))
                {
                    tickStringIterator.remove();
                    billOffline.setIsOnCloud(true);
                    BillsManager.flush(this,billOffline);
                    BillsManager.store(this,billOffline);
                    Log.e("newsend",idTel);
                }
            }
            else
            {
                edit.putStringSet("ticketNotOnCloud",ticketsNotOnCloud);
                edit.commit();
                break;
            }
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
                billSending = nwBill;
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
     * ServerReceiver , recoit les messages venants du serveur
     *
     * ACTION_RECEIVE_FROM_SERVER
     */
    private class ServerReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            if (comm != null && arg1.hasExtra("FAILSOCKET"))
            {
                treatFailSocket();
            }

            if (comm != null && comm.getTag().equals("SENDBILL"))
            {
                String message = arg1.getStringExtra("MESSAGE");
                if (message.equals("IMAGECHECK"))
                {
                    comm.interrupt();
                    comm = null;
                }
            }

            if (comm != null && comm.getTag().equals("SENDNEWBOUTIQUE"))
            {
                String message = arg1.getStringExtra("MESSAGE");
                if (message.equals("NEWBOUTIQUECHECK"))
                {
                    comm.interrupt();
                    comm = null;
                }
            }

            if (comm != null && comm.getTag().equals("REQUESTBOUTIQUE"))
            {
                String message = arg1.getStringExtra("MESSAGE");
                Log.e("MESSAGEREQUEST",message);

                if (message.equals("BOUTIQUECHECK"))
                {
                    treatRequestBoutique();

                }
                else
                {
                    boutiqueStringReceived.append(message);
                }
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

