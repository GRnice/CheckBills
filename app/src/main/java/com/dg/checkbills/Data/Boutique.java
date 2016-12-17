package com.dg.checkbills.Data;

import java.io.Serializable;

/**
 * Created by Remy on 31/10/2016.
 */
public class Boutique implements Serializable
{
    /**
     * Boutique a un nom -> son ID
     * sa latitude et sa longitude
     */

    private double latitude, longitude;
    private String boutiqueName;

    private  static  final  long serialVersionUID =  1332792844476720732L;

    public Boutique(String name, double longi, double lat)
    {
        this.latitude = lat;
        this.longitude = longi;
        this.boutiqueName = name;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

}
