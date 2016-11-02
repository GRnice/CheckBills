package com.dg.checkbills.Data;

import android.media.Image;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Remy on 31/10/2016.
 */
public class Bill implements Serializable
{
    /**
     * Un ticket est défini par son type, son montant, l'heure, la date de sa prise, sa photo ainsi que la boutique associée
     */

    private  static  final  long serialVersionUID =  1350792821376720032L;

    private TYPE_CONTENT_BILL typeDeTicket;
    private transient Image imageDuTicket; // L'image n'est pas serializable
    private int montant;
    private Boutique boutique;
    private Date date;
    private int ID;

    public Bill(TYPE_CONTENT_BILL type, int montant, Boutique boutique, Date date)
    {
        this.typeDeTicket = type;
        this.montant = montant;
        this.boutique = boutique;
        this.date = date;
        this.ID = this.boutique.hashCode() + this.montant + this.date.hashCode();
    }

    public int getId()
    {
        return ID;
    }

}
