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
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.Data.TYPE_CONTENT_BILL;
import com.dg.checkbills.Storage.BillsManager;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Remy on 11/12/2016.
 */

public class ServiceSocket extends Service
{
    final public static String ACTION_SEND_TO_ACTIVITY = "DATA_TO_ACTIVITY";
    final public static String ACTION_TO_SERVICE_FROM_ACTIVITY = "DATA_ACTIVITY_TO_SERVICE";
    final public static String ACTION_TO_SERVICE_FROM_SERVER = "DATA_SERVER_TO_SERVICE";

    private CommunicationServer comm;
    private ActivityReceiver activityReceiver;
    private ServerReceiver serverReceiver;
    private NetworkChangeReceiver networkChangeReceiver;

    public ServiceSocket()
    {
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {
        activityReceiver = new ActivityReceiver();
        serverReceiver = new ServerReceiver();
        networkChangeReceiver = new NetworkChangeReceiver();

        Log.e("PK TANT DE HAINE","YOLO");

        // ECOUTE DES MESSAGES PROVENANTS DU SERVEUR
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TO_SERVICE_FROM_SERVER);
        registerReceiver(serverReceiver,intentFilter);

        // ECOUTE DES MESSAGES PROVENANTS DE L'ACTIVITE
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TO_SERVICE_FROM_ACTIVITY);
        registerReceiver(activityReceiver,intentFilter);

        // ECOUTE DU CHANGEMENT DE L'ETAT DU RESEAU
        intentFilter = new IntentFilter();
        intentFilter.addAction(NetworkChangeReceiver.CONNECTIVITY_CHANGED);
        registerReceiver(networkChangeReceiver, intentFilter);

        super.onStartCommand(intent,flags,startId);
        return START_STICKY;
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


    public void sendImage(Bill myBill) {
        String imgString = myBill.getImage();
        for(int i = 0; i < imgString.length(); i+=4096)
        {
            comm.sendMessage(imgString.substring(i, Math.min(i + 4096, imgString.length())));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean sendBill(Bill myBill)
    {
        comm = new CommunicationServer();
        comm.setActionIntent(ACTION_TO_SERVICE_FROM_SERVER);
        comm.setService(this);
        comm.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        comm.sendMessage("DATE*" + myBill.getDate());
        comm.sendMessage("MONTANT*" + String.valueOf(myBill.getMontant()));
        comm.sendMessage("IMAGE*" + myBill.getImage().split(",").length);
        sendImage(myBill);

        return true;
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
                int montant = arg1.getIntExtra("MONTANT",-1);
                String nom = arg1.getStringExtra("NOM");
                String typeAchat = (String) arg1.getSerializableExtra("TYPEACHAT");
                Boutique boutique = (Boutique) arg1.getSerializableExtra("BOUTIQUE");
                String date = (String) arg1.getSerializableExtra("DATE");
                byte[] image = (byte[]) arg1.getSerializableExtra("IMAGE");

                BillsManager managerData = new BillsManager();
                Bill nwBill = new Bill(TYPE_CONTENT_BILL.LOISIR,nom,montant,boutique,date,image);
                Log.e("COUCOU","TRUSTME");
                managerData.store(getBaseContext(),nwBill);
                sendBill(nwBill);
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
                if(status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED)
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

