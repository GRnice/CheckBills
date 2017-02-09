package com.dg.checkbills.Consommation;

import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.TYPE_CONTENT_BILL;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Remy on 16/01/2017.
 */

public class Statistiques implements Comparable<Date>,Serializable
{
    private Date date;
    private int nbTickets;
    private double depenseTotal;
    private int consoPro;
    private int consoVoyage;
    private int consoAlimentaire;
    private int consoLoisir;

    public Statistiques(Date date,int nbTickets,double depenseTotal,int nbTicketLoisir,int nbTicketPro,int nbTicketAlimentaire,int nbTicketVoyage)
    {
        this.date = date;
        this.nbTickets = nbTickets;
        this.depenseTotal = depenseTotal;
        this.consoPro = nbTicketPro;
        this.consoVoyage = nbTicketVoyage;
        this.consoAlimentaire = nbTicketAlimentaire;
        this.consoLoisir = nbTicketLoisir;
    }

    public Date getDate()
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
    public int compareTo(Date anotherDate)
    {
        return date.compareTo(anotherDate);
    }
}
