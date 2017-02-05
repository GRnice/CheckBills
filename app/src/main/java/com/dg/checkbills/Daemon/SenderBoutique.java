package com.dg.checkbills.Daemon;

import android.location.Location;
import android.os.AsyncTask;

import com.dg.checkbills.Communication.CommunicationServer;
import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.Bill;

import java.io.IOException;

/**
 * Created by Remy on 12/01/2017.
 */

public class SenderBoutique extends Sender implements CommListener
{
    private ServiceSocket service;
    private String nomBoutique;
    private Location loc;
    private CommunicationServer comm;
    private AsyncTask task;

    public SenderBoutique(ServiceSocket service, String nomBoutique, Location position)
    {
        this.service = service;
        this.nomBoutique = nomBoutique;
        this.loc = position;
    }

    public void process()
    {
        task = new TaskSendBoutique();
        task.execute();
    }

    public class TaskSendBoutique extends AsyncTask<Object,Void,Void>
    {
        @Override
        protected Void doInBackground(Object... params)
        {
            comm = new CommunicationServer(service, "SENDNEWBOUTIQUE", SenderBoutique.this);

            comm.start();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                comm.sendMessage("NEWBOUTIQUE*"+nomBoutique+"*LONG*"
                        +loc.getLongitude()+"*LAT*"+loc.getLatitude());
            } catch (IOException e) {
                comm.interrupt();
                service.endTask(SenderBoutique.this,false);
                e.printStackTrace();
            }
            return null;
        }
    }


    @Override
    public void onReceive(String key, String message)
    {
        switch(key)
        {
            case "FAILSOCKET":
            {
                task.cancel(true);
                service.endTask(this,false);
                comm.interrupt();
                break;
            }
            case "MESSAGE":
            {
                if (message.equals("NEWBOUTIQUECHECK"))
                {
                    service.endTask(this,true);
                    task.cancel(true);
                    comm.interrupt();
                }
                break;
            }
        }
    }

    @Override
    public void onReceive(String key, byte[] message,int nbBytes) {

    }
}
