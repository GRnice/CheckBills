package com.dg.checkbills;

import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.Data.TYPE_CONTENT_BILL;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by Remy on 01/11/2016.
 */
public class BillTest
{
    @Test
    public void testID()
    {
        Bill bill;
        TYPE_CONTENT_BILL type = TYPE_CONTENT_BILL.ALIMENTAIRE;
        int montant = 45;
        String nom = "Unticket";
        Boutique boutique = new Boutique();
        Date heure = new Date(System.currentTimeMillis());
        bill = new Bill(type,nom,montant,boutique,heure);
        assertTrue(bill.getId() == montant + boutique.hashCode() + heure.hashCode());
    }

}
