package com.dg.checkbills.Data;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Remy on 28/01/2017.
 */

public class ZoneInfluence implements Serializable
{
    private double longitude,latitude;
    private int poids;

    public ZoneInfluence(double longitude,double latitude,int poids)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.poids = poids;
    }

    public LatLng getLatLng()
    {
        LatLng position = new LatLng(latitude,longitude);
        return position;
    }

    public int getPoids()
    {
        return poids;
    }
}
