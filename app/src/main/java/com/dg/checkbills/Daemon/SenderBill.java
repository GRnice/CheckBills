package com.dg.checkbills.Daemon;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.dg.checkbills.Communication.CommunicationServer;
import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Storage.BillsManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Remy on 12/01/2017.
 */
public class SenderBill extends Sender implements CommListener
{

    private ServiceSocket service;
    private Bill billaSend;
    private CommunicationServer comm;
    private String idTel;
    private AsyncTask task;

    public SenderBill(ServiceSocket serviceSocket, Bill billToSend, String idTel)
    {
        service = serviceSocket;
        billaSend = billToSend;
        this.idTel = idTel;
    }


    public void process()
    {
        task = new TaskSendBill();
        task.execute();
    }

    @Override
    public void onReceive(String key,String message)
    {
        switch(key)
        {
            case "FAILSOCKET":
            {
                comm.interrupt();
                task.cancel(true);
                service.endTask(this,billaSend,false);
                break;
            }

            case "MESSAGE":
            {
                if (message.equals("IDCHECK"))
                {
                    try
                    {
                        comm.sendMessage(billaSend.getImage());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        task.cancel(true);
                        comm.interrupt();
                        service.endTask(SenderBill.this,billaSend,false);
                    }
                }
                if (message.equals("IMAGECHECK"))
                {
                    task.cancel(true);
                    comm.interrupt();
                    service.endTask(SenderBill.this,billaSend,true);
                    break;
                }
            }
        }
    }

    @Override
    public void onReceive(String key, byte[] message, int nbBytes) {

    }


    public class TaskSendBill extends AsyncTask<Object,Void,Void>
    {

        @Override
        protected Void doInBackground(Object... params)
        {
            if (!service.networkAvailable())
            {
                service.endTask(SenderBill.this,billaSend,false);
                return null;
            }
            else
            {
                comm = new CommunicationServer(service, "SENDBILL", SenderBill.this);
                comm.start();

                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    comm.interrupt();
                    task.cancel(true);
                    service.endTask(SenderBill.this,billaSend,false);
                    return null;
                }

                try
                {
                    String requete;

                    if (billaSend.getBoutique().getId().equals("-1"))
                    {
                        requete= "ID*" + idTel + "*DATE*" + billaSend.getDate() + "*MONTANT*" + String.valueOf(billaSend.getMontant())
                                + "*IDBOUTIQUE*" + billaSend.getBoutique().getNom() + "*TITRE*" + billaSend.getNom() +
                                "*TYPEBILL*" + billaSend.getType() + "*SIZEIMAGE*" + billaSend.getImage().length + "*IMAGENAME*"+billaSend.getNomImage();

                    }
                    else
                    {
                        requete= "ID*" + idTel + "*DATE*" + billaSend.getDate() + "*MONTANT*" + String.valueOf(billaSend.getMontant())
                                + "*IDBOUTIQUE*" + billaSend.getBoutique().getId() + "*TITRE*" + billaSend.getNom() +
                                "*TYPEBILL*" + billaSend.getType() + "*SIZEIMAGE*" + billaSend.getImage().length + "*IMAGENAME*"+billaSend.getNomImage();

                    }
                    comm.sendMessage(requete);
                }
                catch (IOException e)
                {
                    comm.interrupt();
                    this.cancel(true);
                    service.endTask(SenderBill.this,billaSend,false);
                    return null;
                }

                return null;
            }

        }


    }

}
