package com.dg.checkbills.Data;

import android.media.Image;

import java.io.Serializable;
import java.text.DateFormat;
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
    private String nom;

    public Bill(TYPE_CONTENT_BILL type,String nom, int montant, Boutique boutique, Date date)
    {
        this.nom = nom;
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

    public String getNom()
    {
        return this.nom;
    }

    public String getDate()
    {
        return DateFormat.getDateInstance().format(this.date);
    }

    public int getMontant()
    {
        return this.montant;
    }

}
