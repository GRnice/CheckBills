package com.dg.checkbills.Data;

import java.io.Serializable;

/**
 * Created by Remy on 31/10/2016.
 */
public enum TYPE_CONTENT_BILL implements Serializable
{
    LOISIR(0),
    PROFESSIONNEL(1),
    VACANCES(2),
    ALIMENTAIRE(3);

    private  static  final  long serialVersionUID =  1350715921376206032L;

    private int id;

        //Constructeur
    TYPE_CONTENT_BILL(int name){
        this.id = name;
    }

    public String toString()
    {
        return String.valueOf(this.id);
    }
}
