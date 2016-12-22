package com.dg.checkbills.Constantes;

/**
 * Created by Remy on 21/12/2016.
 */

public enum BroadcastAddr
{
    ACTION_TO_ACTIVITY_FROM_SERVICE("DATA_SERVICE_TO_ACTIVITY"),
    ACTION_TO_SERVICE_FROM_ACTIVITY("DATA_ACTIVITY_TO_SERVICE"),
    ACTION_TO_SERVICE_FROM_SERVER("DATA_SERVER_TO_SERVICE");

    private String addr;

    BroadcastAddr(String addr){
        this.addr = addr;
    }

    public String getAddr()
    {
        return this.addr;
    }
}
