package com.dg.checkbills.Data;

import java.io.Serializable;

/**
 * Created by Remy on 21/12/2016.
 */

public abstract class Data implements Serializable
{
    protected String id;
    protected String nom;

    public String getId()
    {
        return id;
    }

    public String getNom()
    {
        return this.nom;
    }
}
