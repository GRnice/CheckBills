package com.dg.checkbills.Communication;


import android.app.Service;

import android.content.Intent;
import android.util.Log;

import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Daemon.ServiceSocket;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * Class used to manage the socket (IO) with the server
 * Connect to SOCKET_ADDR, set receivers and emit events
 *
 */
public class CommunicationServer extends Thread implements Runnable
{
    //public static final String SOCKET_ADDR = "13.93.93.125"; // SERVEUR MICROSOFT AZURE
    public static final String SOCKET_ADDR = "192.168.1.13";

    public static final int PORT = 3200;
    private Socket m_sock;
    private BufferedReader input;
    private PrintWriter outputString;
    private OutputStream outputByte;
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
            outputString = new PrintWriter(m_sock.getOutputStream(),true);
            outputByte = m_sock.getOutputStream();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        this.run = true;
        Intent intent;
        while(this.run)
        {
            intent = new Intent();
            intent.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_SERVER.getAddr());
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

    public synchronized void sendMessage(byte[] bytearray)
    {
        try
        {
            outputByte.write(bytearray);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public synchronized void sendMessage(String message)
    {
        this.outputString.println(message);
    }

    private synchronized void deconnect()
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

