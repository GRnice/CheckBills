package com.dg.checkbills.Consommation;

import android.util.Log;

import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.TYPE_CONTENT_BILL;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Remy on 16/01/2017.
 */

public class Statistiques implements Comparable<String>,Serializable
{
    private String date;
    private int nbTickets;
    private double depenseTotal;
    private int consoPro;
    private int consoVoyage;
    private int consoAlimentaire;
    private int consoLoisir;

    public Statistiques(String date,int nbTickets,double depenseTotal,int nbTicketLoisir,int nbTicketPro,int nbTicketAlimentaire,int nbTicketVoyage)
    {
        this.date = date;
        this.nbTickets = nbTickets;
        this.depenseTotal = depenseTotal;
        this.consoPro = nbTicketPro;
        this.consoVoyage = nbTicketVoyage;
        this.consoAlimentaire = nbTicketAlimentaire;
        this.consoLoisir = nbTicketLoisir;
    }

    public String getDate()
    {
        return date;
    }

    public int getConsoPro()
    {
        return consoPro;
    }

    public int getConsoVoyage()
    {
        return consoVoyage;
    }

    public int getConsoAlimentaire()
    {
        return consoAlimentaire;
    }

    public int getConsoLoisir()
    {
        return consoLoisir;
    }

    public double getDepenseTotal()
    {
        return depenseTotal;
    }

    public int getNbTickets()
    {
        return nbTickets;
    }

    public void addBill(Bill bill)
    {
        nbTickets++;
        depenseTotal += bill.getMontant();
        switch(bill.getType())
        {
            case "0": // LOISIR
            {
                consoLoisir++;
                break;
            }
            case "1": // PROFESSIONNEL
            {
                consoPro++;
                break;
            }
            case "2": // VACANCES
            {
                consoVoyage++;
                break;
            }
            case "3": // ALIMENTAIRE
            {
                consoAlimentaire++;
                break;
            }
        }
    }

    @Override
    public int compareTo(String anotherDate)
    {
        // s1.compareTo(s2)
        // -1 si s1 < s2

        // > 0 si s1 > s2

        String[] dateAnoterSplit = anotherDate.split("\\-");
        String dateAnother = ""+dateAnoterSplit[1]+dateAnoterSplit[0];

        String[] dateMeSplit = date.split("\\-");
        String dateMe = ""+dateMeSplit[1]+dateMeSplit[0];
        Log.e("dateMe",dateMe);
        Log.e("dateAnother",dateAnother);
        Log.e("res",""+(Integer.valueOf(dateMe) - Integer.valueOf(dateAnother)));
        return Integer.valueOf(dateMe) - Integer.valueOf(dateAnother);
    }
}
