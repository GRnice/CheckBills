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
import com.dg.checkbills.Data.ArrayUtils;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.Data.ZoneInfluence;
import com.dg.checkbills.Storage.BillsManager;
import com.dg.checkbills.Storage.BoutiqueManager;
import com.dg.checkbills.Storage.ImageManager;
import com.dg.checkbills.Storage.StatManager;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Queue;


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

    private ArrayDeque<String> queueNomImage;

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
        queueNomImage = ImageManager.loadHistorique(getBaseContext());

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
        Log.e("WOOLLL", "SOPPPPPP");

        try
        {
            LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                lm.removeUpdates(this);
            }
            unregisterReceiver(activityReceiver);
            unregisterReceiver(networkChangeReceiver);
        }
        catch(Exception e)
        {

        }

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

        if(hour >= 8 && hour < 12){
            heureDepart = dateJour + " " + "08:00:00";
            heureFin = dateJour + " " + "11:59:59";
        }
        else if(hour >= 12 && hour < 18) {
            heureDepart = dateJour + " " + "12:00:00";
            heureFin = dateJour + " " + "17:59:59";

        }
        else if(hour >= 18 && hour < 24) {
            heureDepart = dateJour + " " + "18:00:00";
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

    private void sendUpdateBill(Bill nwBill)
    {
        for (int i  = 0 ; i < billsArray.size() ; i++)
        {
            Bill aBill = billsArray.get(i);
            if (aBill.getId().equals(nwBill))
            {
                billsArray.set(i,nwBill);
            }
        }

        String requete = "MODIF*ID*" + idTel + "*DATE*" + nwBill.getDate() + "*MONTANT*" + String.valueOf(nwBill.getMontant())
                + "*IDBOUTIQUE*" + nwBill.getBoutique().getId() + "*TITRE*" + nwBill.getNom() +
                "*TYPEBILL*" + nwBill.getType();
        SenderRequest senderRequest = new SenderRequest(this,requete,"SENDMODIFBILL");
        arrayOfSender.add(senderRequest);
    }


    private void checkPositionAndSendNewBoutiqueToServer(String nomBoutique)
    {
        SenderBoutique senderBoutique = new SenderBoutique(this,nomBoutique,lastPosition);
        senderBoutique.process();
        arrayOfSender.add(senderBoutique);
    }

    private void sendRequestImage(String nomImage)
    {
        SenderRequestImage senderRequestImage = new SenderRequestImage(this,"REQUEST-IMG*"+nomImage,"RQST-IMAGE",nomImage);
        senderRequestImage.process();
        arrayOfSender.add(senderRequestImage);
    }

    private void sendBillsNotOnCloud()
    {
        if (billsOffline.size() > 0)
        {
            Log.e("SENDBILLNOTONCLOUD","DANSLEIF");
            Bill billToSend = billsOffline.get(0);
            ArrayList<Byte> image = ImageManager.loadImage(getBaseContext(),billToSend.getNomImage());
            billToSend.setImage(ArrayUtils.toPrimitive(image));
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

    /**
     * Methode appellée quand une tache dédiée à recuperer une image se termine
     * @param sender
     * @param success
     * @param nomImage
     * @param image
     */
    public void endTask(SenderRequestImage sender,boolean success,String nomImage,ArrayList<Byte> image)
    {
        Log.e("Episode 2","requestImage");
        arrayOfSender.remove(sender);

        if (success)
        {
            ImageManager.storeImage(getBaseContext(),nomImage,image);
            if (queueNomImage.size() == 5)
            {
                String last = queueNomImage.removeLast();
                ImageManager.deleteImage(getBaseContext(),last);
            }
            queueNomImage.addFirst(nomImage);
            ImageManager.saveHistorique(getBaseContext(),queueNomImage);

            Intent intentForDetailHistoriqueFrag = new Intent();
            intentForDetailHistoriqueFrag.setAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());

            Intent intentImage = new Intent();
            intentImage.setAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());
            intentImage.putExtra("IMG",nomImage);
            Log.e("Episode 3","requestImage");
            sendBroadcast(intentImage);
        }
    }

    public void endTask(SenderBoutique senderBoutique,boolean success)
    {
        arrayOfSender.remove(senderBoutique);
    }

    /**
     * Requete
     * @param senderRequest
     * @param success
     * @param response
     */
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

    /**
     * Methode appellée quand une tache dédiée à emettre un billet se termine
     * @param senderBill
     * @param billSent
     * @param success
     */
    public void endTask(SenderBill senderBill,Bill billSent, boolean success)
    {
        arrayOfSender.remove(senderBill);
        billSent.setIsOnCloud(success); // il a bien été émis ou non
        Log.e("ENDTASK SEND BILL","");
        if (! queueNomImage.contains(billSent.getNomImage()))
        {
            Log.e("ENDTASK SEND BILL","SIZE QUEUE"+queueNomImage.size());
            if (queueNomImage.size() == 5)
            {
                Log.e("MAXSIZEQUEUE","");
                String stringpop = queueNomImage.removeLast();
                Log.e("REMOVEDIS",stringpop);
                ImageManager.deleteImage(getBaseContext(),stringpop);
            }
            queueNomImage.addFirst(billSent.getNomImage());
        }

        Log.e("Billsent-servicesocket",billSent.getNomImage());
        ImageManager.storeImage(getBaseContext(),billSent.getNomImage(),ArrayUtils.toArray(billSent.getImage()));
        ImageManager.saveHistorique(getBaseContext(),queueNomImage);

        billSent.setImage(null);

        if (success == false && ! billsOffline.contains(billSent)) // si echec et le ticket n'est pas deja dans la liste des tickets offlines
        {
            billsOffline.add(billSent);
            billsArray.add(billSent);
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
            billsArray.add(billSent);
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

            if (arg1.hasExtra("REQUEST-IMG"))
            {
                String nomImage = arg1.getStringExtra("REQUEST-IMG");
                Log.e("Episode 1","requestImage");
                if (queueNomImage.contains(nomImage))
                {
                    Log.e("Episode 1.2","requestImage");
                    Intent intentImage = new Intent();
                    intentImage.setAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());

                    intentImage.putExtra("IMG",nomImage);
                    sendBroadcast(intentImage);
                }
                else
                {
                    Log.e("Episode 1.2","requestImagServe");
                    sendRequestImage(nomImage);
                }
            }

            if (arg1.hasExtra("REQUEST-MODIF"))
            {
                Bill nwBill = (Bill) arg1.getSerializableExtra("REQUEST-MODIF");
                sendUpdateBill(nwBill);

            }

            if (arg1.hasExtra("GET_ZONES_INFLUENCES"))
            {
                Log.e("EPISODE-01","zones");
                Date d = Calendar.getInstance().getTime();
                requestZonesInfluences(d);
         /*       Intent intentBoutique = new Intent();
                intentBoutique.setAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());
                intentBoutique.putExtra("ZONES-INFLUENTES",arrayOfCentres);
                sendBroadcast(intentBoutique); */
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