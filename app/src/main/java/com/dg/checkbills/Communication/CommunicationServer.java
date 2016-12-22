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
    private String tag;
    boolean run;
    boolean isDeconnect; // si à false alors ne pas emettre un FAILSOCKET,
                        // car le socket a été interrompu intentionnellement

    public CommunicationServer()
    {
        super();
    }

    public CommunicationServer(Service service,String tag,String actionIntent)
    {
        super();
        this.actionIntent = actionIntent;
        this.tag = tag;
        this.service = service;
    }

    public String getTag()
    {
        return tag;
    }

    private void sendMessageToService(String key,String message)
    {
        Intent intent = new Intent();
        intent.setAction(actionIntent);
        intent.putExtra(key, message);
        synchronized (this.service)
        {
            this.service.sendBroadcast(intent);
        }
    }


    @Override
    public void run()
    {
        String line;
        isDeconnect = false;
        try
        {
            m_sock = new Socket(SOCKET_ADDR, PORT);
        }
        catch (IOException e)
        {
            if (!isDeconnect)
            {
                sendMessageToService("FAILSOCKET","");
            }
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
            if (!isDeconnect)
            {
                sendMessageToService("FAILSOCKET","");
            }
            e.printStackTrace();
        }
        this.run = true;
        while(this.run)
        {

            try
            {
                line = input.readLine();

                if (line != null)
                {
                    sendMessageToService("MESSAGE",line);
                    Log.e("RECEIVE THIS -> ",line);
                }

            } catch (IOException e)
            {
                if (!isDeconnect)
                {
                    sendMessageToService("FAILSOCKET","");
                }
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
            this.isDeconnect = true;
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

