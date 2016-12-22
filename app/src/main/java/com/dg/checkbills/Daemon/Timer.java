package com.dg.checkbills.Daemon;

import android.os.AsyncTask;

/**
 * Created by Remy on 22/12/2016.
 */

/**
 * Cette classe est un timer appelant un objet implementant l'interface TimerListener
 * Quand le temps s'est écoulé cet asynctask appelle la methode timeout pour indiquer que le temps s'est ecoulé.
 */
public class Timer extends AsyncTask
{
    int secondes;
    TimerListener timerListener;
    String tag;

    public void setTimer(int secondes)
    {
        this.secondes = secondes;
    }
    public void setTimerListener(TimerListener timerListener)
    {
        this.timerListener = timerListener;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    @Override
    protected Object doInBackground(Object[] params)
    {
        try {
            Thread.sleep(secondes * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timerListener.timeout(tag);
        return null;
    }
}
