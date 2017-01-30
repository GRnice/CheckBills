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

    public SenderBill(ServiceSocket serviceSocket, Bill billToSend, String idPortable)
    {
        service = serviceSocket;
        billaSend = billToSend;
        idTel = idPortable;
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
                task.cancel(true);
                service.endTask(this,billaSend,false);
                comm.interrupt();
                break;
            }

            case "MESSAGE":
            {
                if (message.equals("IMAGECHECK"))
                {
                    task.cancel(true);
                    service.endTask(SenderBill.this,billaSend,true);
                    comm.interrupt();
                    break;
                }
            }
        }
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

                    service.endTask(SenderBill.this,billaSend,false);
                    return null;
                }

                boolean res = false;
                try {
                    res = comm.sendMessage("ID*" + idTel + "*DATE*" + billaSend.getDate() + "*MONTANT*" + String.valueOf(billaSend.getMontant())
                            + "*IDBOUTIQUE*" + billaSend.getBoutique().getId() + "*TITRE*" + billaSend.getNom() +
                            "*TYPEBILL*" + billaSend.getType() + "*SIZEIMAGE*" + billaSend.getImage().length + "*IMAGENAME*SzPdslWmd");
                } catch (IOException e) {
                    comm.interrupt();;

                    service.endTask(SenderBill.this,billaSend,false);
                    return null;
                }

                if (!res) // si le message n'est pas passé
                {

                    comm.interrupt();

                    service.endTask(SenderBill.this,billaSend,false);
                    return null;
                }

                try
                {
                    comm.sendMessage(billaSend.getImage());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    comm.interrupt();

                    service.endTask(SenderBill.this,billaSend,false);
                    return null;
                }

                return null;
            }

        }


    }

}
