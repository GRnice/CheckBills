package com.dg.checkbills.Data;

import android.media.Image;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Remy on 31/10/2016.
 */
public class Bill extends Data implements Cloneable
{
    /**
     * Un ticket est défini par son type, son montant, l'heure, la date de sa prise, sa photo ainsi que la boutique associée
     */
    private  static  final  long serialVersionUID =  1350792821376720032L;

    private TYPE_CONTENT_BILL typeDeTicket;
    private byte[] imageDuTicket;
    private double montant;
    private Boutique boutique;
    private String date;
    private String nomImageTicket;
    private boolean isOnCloud;

    public Bill()
    {

    }

    public Bill(TYPE_CONTENT_BILL type,String nom, double montant, Boutique boutique, String date,byte[] img)
    {
        this.nom = nom;
        this.typeDeTicket = type;
        this.montant = montant;
        this.boutique = boutique;
        this.imageDuTicket = img;
        this.date = date;
        this.isOnCloud = false;
        this.id = String.valueOf(this.boutique.hashCode() + this.montant + this.date.hashCode());
        this.nomImageTicket = "IMG-"+this.nom+this.id;
    }

    public Bill(String type,String nom, double montant, Boutique boutique, String date,byte[] img)
    {
        this.nom = nom;

        this.montant = montant;
        this.boutique = boutique;
        this.imageDuTicket = img;
        this.date = date;
        this.isOnCloud = false;
        this.id = String.valueOf(this.boutique.hashCode() + this.montant + this.date.hashCode());
        this.nomImageTicket = "IMG-"+this.nom+this.id;
        switch (type.toUpperCase())
        {
            case "LOISIR":
                this.typeDeTicket = TYPE_CONTENT_BILL.LOISIR;
                break;
            case "PROFESSIONNEL":
                this.typeDeTicket = TYPE_CONTENT_BILL.PROFESSIONNEL;
                break;
            case "VACANCES":
                this.typeDeTicket = TYPE_CONTENT_BILL.VACANCES;
                break;
            default:
                this.typeDeTicket = TYPE_CONTENT_BILL.ALIMENTAIRE;
        }
    }

    public Bill clone()
    {
        Bill b = null;
        try {
            b = (Bill) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return b;
    }


    public String getDate()
    {
        return this.date;
    }

    public byte[] getImage()
    {
        return this.imageDuTicket;
    }

    public double getMontant()
    {
        return this.montant;
    }

    public Boutique getBoutique()
    {
        return boutique;
    }

    public String getType() {
        return typeDeTicket.toString();
    }

    public String getNomImage()
    {
        return nomImageTicket;
    }

    public boolean isOnCloud()
    {
        return isOnCloud;
    }

    public void setIsOnCloud(boolean isOnCloud)
    {
        this.isOnCloud = isOnCloud;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public void setImage(byte[] byteArray)
    {
        this.imageDuTicket = byteArray;
    }

    public void setMontant(Double nwMontant)
    {
        montant = nwMontant;
    }

    public void setTitre(String titre)
    {
        this.nom = titre;
    }
}
