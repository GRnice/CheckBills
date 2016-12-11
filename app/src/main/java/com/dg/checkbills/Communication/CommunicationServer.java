package com.dg.checkbills.Communication;


import android.app.Service;

import android.content.Intent;
import android.os.MemoryFile;
import android.util.Log;


import com.dg.checkbills.Daemon.ServiceSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * Class used to manage the socket (IO) with the server
 * Connect to SOCKET_ADDR, set receivers and emit events
 *
 * Created by Team PFE 2016-2017 Cheebane-Dib-Giangrasso-Hossam
 */
public class CommunicationServer extends Thread implements Runnable
{
    public static final String SOCKET_ADDR = "10.1.99.212";

    public static final int PORT = 3000;
    private Socket m_sock;
    private BufferedReader input;
    private PrintWriter output;
    private String actionIntent;
    private Service service;
    boolean run;

    public CommunicationServer()
    {
        super();
    }

    public synchronized void setActionIntent(String action)
    {
        actionIntent = action;
    }


    public synchronized void setService(Service ser)
    {
        this.service = ser;
    }
    @Override
    public void run()
    {
        String line;

        try
        {
            m_sock = new Socket(SOCKET_ADDR, PORT);
        }
        catch (IOException e)
        {

            e.printStackTrace();
            return;
        }

        try
        {
            input = new BufferedReader(new InputStreamReader(m_sock.getInputStream()));
            output = new PrintWriter(m_sock.getOutputStream(),true);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        this.run = true;
        Intent intent;
        while(this.run)
        {
            intent = new Intent();
            intent.setAction(ServiceSocket.ACTION_RECEIVE_FROM_SERVER);
            try
            {
                line = input.readLine();

                if (line != null)
                {
                    /*if(line.equals(STOPSUIVI)){
                        intent.putExtra(STOPSUIVI, true);
                    } else if(line.equals(OKPROMENADE)){
                        intent.putExtra(OKPROMENADE, true);
                    }*/
                    synchronized (this.service)
                    {
                        this.service.sendBroadcast(intent);
                    }
                    Log.e("RECEIVE THIS -> ",line);
                }

            } catch (IOException e)
            {
                e.printStackTrace();
                break;
            }
        }

    }

    public synchronized void sendMessage(String message)
    {
        this.output.println(message);
    }

    public synchronized void deconnect()
    {
        try {
            this.m_sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void interrupt()
    {
        // liberer la ressource
        this.run = false;
        this.deconnect();
        super.interrupt();
    }

    public synchronized Socket getSocket() {
        return m_sock;
    }
}

