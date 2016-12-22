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

public class ServiceSocket extends Service
{

    private CommunicationServer comm;
    private ActivityReceiver activityReceiver;
    private ServerReceiver serverReceiver;
    private NetworkChangeReceiver networkChangeReceiver;

    private ArrayList<Bill> billsArray;
    private ArrayList<Boutique> boutiqueArray;

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


    private boolean sendBill(String idTel,Bill myBill)
    {
        comm = new CommunicationServer();
        comm.setActionIntent(BroadcastAddr.ACTION_TO_SERVICE_FROM_SERVER.getAddr());
        comm.setService(this);
        comm.start();

        Boutique boutique = myBill.getBoutique();
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
                String idTel = arg1.getStringExtra("IDTEL");
                Bill nwBill = (Bill) arg1.getSerializableExtra("BILL");
                BillsManager.store(getBaseContext(),nwBill);
                billsArray.add(nwBill);

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

