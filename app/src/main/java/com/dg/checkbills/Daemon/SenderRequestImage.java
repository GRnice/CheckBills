package com.dg.checkbills.Daemon;

import android.os.AsyncTask;
import android.util.Log;

import com.dg.checkbills.Communication.CommunicationServer;
import com.dg.checkbills.Data.ArrayUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by Remy on 02/02/2017.
 */

public class SenderRequestImage extends Sender implements CommListener
{
    private ServiceSocket service;
    private String request;
    private CommunicationServer comm;
    private AsyncTask task;
    private String nomImage;
    private ArrayList<Byte> arrayImage;
    private int sizeImage;
    private String tag;

    public SenderRequestImage(ServiceSocket serv,String request,String tag,String nomImage)
    {
        this.tag = tag;
        this.request = request;
        service = serv;
        this.nomImage = nomImage;
        arrayImage = new ArrayList<>();
    }

    @Override
    public void onReceive(String key, String message)
    {


    }

    @Override
    public void onReceive(String key, byte[] message,int nbBytes)
    {
        if (key.equals("FAILSOCKET"))
        {
            service.endTask(this,false,null,null);
        }
        else if (key.equals("MESSAGE"))
        {
            Log.e("SIZE-buff",""+message.length);
            if (nbBytes == 1)
            {
                task.cancel(true);
                comm.interrupt();
                service.endTask(this,true,nomImage,arrayImage);
            }
            else
            {
                for (int i = 0 ; i < nbBytes ; i++)
                {
                    arrayImage.add(message[i]);
                }

            }
        }
    }


    public String getTag()
    {
        return tag;
    }


    @Override
    public void process()
    {
        task = new TaskRequestImage(nomImage);
        task.execute();
    }


    public class TaskRequestImage extends AsyncTask<Object,Void,Void>
    {
        private String nomImage;
        public TaskRequestImage(String nomImage)
        {
            this.nomImage = nomImage;
        }

        @Override
        protected Void doInBackground(Object... params)
        {
            comm = new CommunicationServer(service, request, SenderRequestImage.this);
            comm.readByteMode(true);
            comm.start();
            try
            {
                Thread.sleep(3000);
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
                service.endTask(SenderRequestImage.this,false,null,null);
            }

            return null;
        }


    }
}
