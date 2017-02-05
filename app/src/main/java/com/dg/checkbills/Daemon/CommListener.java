package com.dg.checkbills.Daemon;

/**
 * Created by Remy on 12/01/2017.
 */

public interface CommListener
{
    public void onReceive(String key,String message);
    public void onReceive(String key,byte[] message,int nbBytes);
}
