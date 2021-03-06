package com.dg.checkbills.Communication;


import android.app.Service;

import android.content.Intent;
import android.util.Log;

import com.dg.checkbills.Daemon.CommListener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * Class used to manage the socket (IO) with the server
 * Connect to SOCKET_ADDR, set receivers and emit events
 *
 */

public class CommunicationServer extends Thread implements Runnable
{
    //public static final String SOCKET_ADDR = "13.93.93.125"; // SERVEUR MICROSOFT AZURE
    public static final String SOCKET_ADDR = "192.168.1.70";

    public static final int PORT = 3200;
    private Socket m_sock;
    private BufferedReader input;
    private DataInputStream in;
    private PrintWriter outputString;
    private OutputStream outputByte;
    private String actionIntent;
    private String tag;
    private CommListener task;
    private boolean modeLecture = false; // true -> byte , false -> string
    boolean run;
    boolean isDeconnect; // si à false alors ne pas emettre un FAILSOCKET,
                        // car le socket a été interrompu intentionnellement
    AtomicBoolean atom_ic_write = new AtomicBoolean(false);


    public CommunicationServer()
    {
        super();
    }

    public CommunicationServer(Service service,String tag,CommListener task)
    {
        super();
        this.task = task;
        this.tag = tag;
    }

    public String getTag()
    {
        return tag;
    }

    private void sendMessageToListener(String key,String message)
    {
        Intent intent = new Intent();
        intent.setAction(actionIntent);
        intent.putExtra(key, message);
        synchronized (this.task)
        {
            this.task.onReceive(key,message);
        }
    }

    private void sendMessageToListener(String key,byte[] message,int nbBytes)
    {
        Intent intent = new Intent();
        intent.setAction(actionIntent);
        intent.putExtra(key, message);
        synchronized (this.task)
        {
            this.task.onReceive(key,message,nbBytes);
        }
    }

    public void readByteMode(boolean etat)
    {
        modeLecture = etat;
    }


    @Override
    public void run()
    {
        String line = null;
        isDeconnect = false;
        try
        {
            m_sock = new Socket(SOCKET_ADDR, PORT);
        }
        catch (IOException e)
        {
            if (!isDeconnect)
            {
                sendMessageToListener("FAILSOCKET","");
            }
            e.printStackTrace();
            return;
        }

        try
        {
            input = new BufferedReader(new InputStreamReader(m_sock.getInputStream()));
            in = new DataInputStream(new BufferedInputStream(m_sock.getInputStream()));

            outputString = new PrintWriter(m_sock.getOutputStream(),true);
            outputByte = m_sock.getOutputStream();
        } catch (IOException e)
        {
            if (!isDeconnect)
            {
                sendMessageToListener("FAILSOCKET","");
            }
            e.printStackTrace();
        }
        this.run = true;
        if (modeLecture == true) // lecture de bytes
        {
            byte[] buffer = new byte[4096];
            while(this.run)
            {

                try
                {
                    int nbBytes = in.read(buffer);
                    Log.e("nbBytes",""+nbBytes);
                    sendMessageToListener("MESSAGE",buffer,nbBytes);



                }
                catch (IOException e)
                {
                    if (!isDeconnect)
                    {
                        sendMessageToListener("FAILSOCKET","");
                    }
                    e.printStackTrace();
                    break;
                }
            }
        }


        while(this.run)// lecture de strings
        {

            try
            {
                line = input.readLine();

                if (line != null)
                {
                    sendMessageToListener("MESSAGE",line);
                    Log.e("RECEIVE THIS -> ",line);
                }

            }
            catch (IOException e)
            {
                if (!isDeconnect)
                {
                    sendMessageToListener("FAILSOCKET","");
                }
                e.printStackTrace();
                break;
            }
        }

    }


    public synchronized void sendMessage(byte[] bytearray) throws IOException
    {
        if (atom_ic_write.compareAndSet(false,true))
        {
            outputByte.write(bytearray);
            atom_ic_write.set(false);
        }

    }


    public synchronized boolean sendMessage(String message) throws IOException
    {
        if (atom_ic_write.compareAndSet(false,true) && this.run)
        {
            this.outputString.println(message);
            atom_ic_write.set(false);
            return true;
        }

        return false;
    }

    private synchronized void deconnect()
    {
        try {
            this.isDeconnect = true;
            if (this.m_sock != null)
            {
                this.m_sock.close();
            }

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

