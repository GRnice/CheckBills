package com.dg.checkbills.Daemon;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.dg.checkbills.Communication.CommunicationServer;
import com.dg.checkbills.Communication.NetworkUtil;
import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.Data.TYPE_CONTENT_BILL;
import com.dg.checkbills.Storage.BillsManager;
import com.dg.checkbills.Storage.BoutiqueManager;

import java.util.ArrayList;

/**
 * Created by Remy on 11/12/2016.
 */

public class ServiceSocket extends Service implements TimerListener
{

    private CommunicationServer comm; // permet de dialoguer avec le serveur
    private ActivityReceiver activityReceiver; // ecoute les messages émis par les differentes activity
    private ServerReceiver serverReceiver; // ecoute les messages émis par le serveur
    private NetworkChangeReceiver networkChangeReceiver;

    private ArrayList<Bill> billsArray; // tableau de tickets
    private ArrayList<Boutique> boutiqueArray; // tableau de boutiques

    private StringBuilder boutiqueStringReceived; // contient toutes les boutiques transmisent sous forme de string
    private Bill billSending;
    private Timer aTimer;
    // par le serveur, ce tableau sera traité quand toutes les boutiques auront été recues.

    public ServiceSocket()
    {
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {
        activityReceiver = new ActivityReceiver();
        serverReceiver = new ServerReceiver();
        networkChangeReceiver = new NetworkChangeReceiver();

        billsArray = BillsManager.load(getBaseContext());
        boutiqueArray = BoutiqueManager.load(getBaseContext());

        Log.e("PK TANT DE HAINE","YOLO");

        // ECOUTE DES MESSAGES PROVENANTS DU SERVEUR
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_SERVER.getAddr());
        registerReceiver(serverReceiver,intentFilter);

        // ECOUTE DES MESSAGES PROVENANTS DE L'ACTIVITE
        intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
        registerReceiver(activityReceiver,intentFilter);

        // ECOUTE DU CHANGEMENT DE L'ETAT DU RESEAU
        intentFilter = new IntentFilter();
        intentFilter.addAction(NetworkChangeReceiver.CONNECTIVITY_CHANGED);
        registerReceiver(networkChangeReceiver, intentFilter);

        super.onStartCommand(intent,flags,startId);

        requestBoutique(); // demande de charger les dernieres boutiques
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        Log.e("DEAD","DEAD");
        comm.interrupt();
        unregisterReceiver(activityReceiver);
        unregisterReceiver(serverReceiver);
        unregisterReceiver(networkChangeReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    /**
     * Cette methode demande au serveur de transmettre les boutiques à l'application
     */
    private void requestBoutique()
    {
        boutiqueStringReceived = new StringBuilder();
        comm = new CommunicationServer(this,"REQUESTBOUTIQUE",BroadcastAddr.ACTION_TO_SERVICE_FROM_SERVER.getAddr());
        comm.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startTimer("REQUEST_BOUTIQUE_TIMER",10); // si au bout de 10 secondes tout n'a pas été transféré on
                                                    // arrete la communication
        comm.sendMessage("REQUEST_ALL_BOUTIQUES");
    }

    /**
     * Traite les boutiques transmisent par le serveur
     */
    private void treatRequestBoutique()
    {
        comm.interrupt(); // arret du socket dédié au REQUEST_ALL_BOUTIQUES
        comm = null;
        Log.e("ALL_BOUTIQUE_RECEIVED",boutiqueStringReceived.toString());
        String[] allBoutiques = boutiqueStringReceived.toString().split("\\_");
        for (String aBoutique : allBoutiques)
        {
            String[] aBoutiqueSplit = aBoutique.split("\\*"); // IDBOUTIQUE*id*NOM*nom
            Boutique nwBoutique = new Boutique(aBoutiqueSplit[1],aBoutiqueSplit[3]);
            BoutiqueManager.store(getBaseContext(),nwBoutique);
            this.boutiqueArray.add(nwBoutique);
        }
    }

    /**
     * Traite les erreurs de connexion
     */
    public void treatFailSocket()
    {
        if (comm.getTag().equals("REQUESTBOUTIQUE"))
        {
            boutiqueStringReceived = new StringBuilder();
            comm.interrupt();
            comm = null;
        }
        else if(comm.getTag().equals("SENDBILL"))
        {
            comm.interrupt();
            billSending.setIsOnCloud(false); // pas émis
            billsArray.add(billSending); // mais quand meme ajouté
            billSending = null;
            comm = null;
        }
    }

    private void startTimer(String tag,int secondes)
    {
        aTimer = new Timer();
        aTimer.setTag(tag); aTimer.setTimer(secondes);aTimer.setTimerListener(this);
        aTimer.execute();
    }



    /**
     * Envoie un ticket crée par l'utilisateur au serveur
     * @param idTel
     * @param myBill
     * @return
     */
    private boolean sendBill(String idTel,Bill myBill)
    {
        comm = new CommunicationServer(this,"SENDBILL",BroadcastAddr.ACTION_TO_SERVICE_FROM_SERVER.getAddr());
        comm.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // ID*idxxxx*DATE*string*MONTANT*40*IDBOUTIQUE*f8e9*TITRE*xxtitrexx*TYPEBILL*x
        comm.sendMessage("ID*" + idTel+"*DATE*" + myBill.getDate()+"*MONTANT*"+String.valueOf(myBill.getMontant())
                + "*IDBOUTIQUE*" + myBill.getBoutique().getId()+"*TITRE*"+myBill.getNom()+"*TYPEBILL*"+myBill.getType());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        comm.sendMessage(myBill.getImage());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        comm.sendMessage("IMAGECHECK");

        comm.interrupt();
        billSending.setIsOnCloud(true); // il a bien été émis
        billsArray.add(billSending);
        billSending = null;
        comm = null;
        return true;
    }

    /**
     * Methode appellée par un asynctask qui a compté n secondes
     * @param tag
     */
    @Override
    public void timeout(String tag)
    {
        if (comm != null)
        {
            Log.e("TIMEOUT!!","STOPSOCKET");
            treatFailSocket();
        }
    }

    /**
     * ClientReceiver , envoie des messages au serveur
     * STARTSUIVI
     * CONTINUE
     * STOPSUIVI
     *
     * MessageReceiver, recoit les messages venants d'une activité
     */
    private class ActivityReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            // ICI on recoit les messages provenants d'une activité

            boolean newBill = arg1.getBooleanExtra("NEWBILL", false);

            if (newBill)
            {
                String idTel = arg1.getStringExtra("IDTEL");
                Bill nwBill = (Bill) arg1.getSerializableExtra("BILL");
                BillsManager.store(getBaseContext(),nwBill);
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

            if (comm != null && comm.getTag().equals("REQUESTBOUTIQUE"))
            {
                String message = arg1.getStringExtra("MESSAGE");
                Log.e("MESSAGEREQUEST",message);
                if (message.equals("BOUTIQUECHECK"))
                {
                    aTimer.cancel(true);
                    aTimer = null;
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
        private boolean connected = true;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            if (CONNECTIVITY_CHANGED.equals(intent.getAction()))
            {
                if (status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED)
                {

                }
                else if(status==NetworkUtil.NETWORK_STATUS_MOBILE || status== NetworkUtil.NETWORK_STATUS_WIFI)
                {
                    if(!connected)
                    {

                    }
                }
            }
        }
    }

}

