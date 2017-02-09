package com.dg.checkbills.Daemon;

import android.os.AsyncTask;

import com.dg.checkbills.Communication.CommunicationServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Remy on 12/01/2017.
 */

public class SenderRequest extends Sender implements CommListener
{
    private ServiceSocket service;
    private String request;
    private CommunicationServer comm;
    private AsyncTask task;
    private StringBuilder boutiqueStringReceived;
    private String tag;

    public SenderRequest(ServiceSocket serv,String request,String tag)
    {
        this.tag = tag;
        this.request = request;
        service = serv;
        boutiqueStringReceived = new StringBuilder();
    }

    @Override
    public void onReceive(String key, String message)
    {
        switch(key)
        {
            case "FAILSOCKET":
            {
                task.cancel(true);
                service.endTask(this,false,null);
                comm.interrupt();
                break;
            }
            case "MESSAGE":
            {
                if (message.equals("BOUTIQUECHECK") || message.equals("ZONES_INFLUENCES_CHECK") ||
                        message.equals("BILLUPDATECHECK") )
                {
                    task.cancel(true);
                    comm.interrupt();
                    service.endTask(this,true,boutiqueStringReceived.toString());

                }
                else
                {
                    boutiqueStringReceived.append(message);
                }
                break;
            }
        }
    }

    @Override
    public void onReceive(String key, byte[] message,int nbBytes) {

    }

    public String getTag()
    {
        return tag;
    }

    public void process()
    {
        task = new TaskSendRequest();
        task.execute();
    }

    public class TaskSendRequest extends AsyncTask<Object,Void,Void>
    {

        @Override
        protected Void doInBackground(Object... params)
        {
            comm = new CommunicationServer(service, "REQUEST", SenderRequest.this);
            comm.start();
            try
            {
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            try
            {
                comm.sendMessage(request);
            } catch (IOException e)
            {
                comm.interrupt();
                service.endTask(SenderRequest.this,false,null);
            }

            return null;
        }


    }
}
