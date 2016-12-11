package com.dg.checkbills;

import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;
import com.dg.checkbills.Data.TYPE_CONTENT_BILL;
import com.dg.checkbills.Storage.BillsManager;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import android.test.FlakyTest;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;


/**
 * Created by Remy on 01/11/2016.
 */

public class BillsManagerTest extends ApplicationTestCase<Application>
{

    public BillsManagerTest()
    {
        super(Application.class);
    }

    public BillsManagerTest (Class<Application> applicationClass)
    {
        super(applicationClass);
    }

    @Override
    public void setUp() throws Exception
    {
        createApplication();
    }

    @FlakyTest
    public void testOneTicket()
    {

        Bill bill;

        Context context = getContext();
        BillsManager manager;
        TYPE_CONTENT_BILL type = TYPE_CONTENT_BILL.ALIMENTAIRE;
        int montant = 45;
        Boutique boutique = new Boutique();
        Date heure = new Date(System.currentTimeMillis());
        bill = new Bill(type,"un Ticket",montant,boutique,heure);
        manager = new BillsManager();

        manager.store(context,bill);
        ArrayList<Bill> list = manager.load(context);
        Bill b = list.get(0);
        assertTrue(b.getId() == bill.getId());
        assertTrue(manager.flush(context,bill));

    }


    @FlakyTest
    public void testMultipleTicket() {

        Bill bill1,bill2,bill3;

        Context context = getContext();
        BillsManager manager;
        TYPE_CONTENT_BILL type1 = TYPE_CONTENT_BILL.ALIMENTAIRE;
        TYPE_CONTENT_BILL type2 = TYPE_CONTENT_BILL.LOISIR;
        TYPE_CONTENT_BILL type3 = TYPE_CONTENT_BILL.PROFESSIONNEL;
        int montant1 = 45;
        int montant2 = 50;
        int montant3 = 1000;
        Boutique boutique1 = new Boutique();
        Date heure1 = new Date(System.currentTimeMillis());
        Date heure2 = new Date(System.currentTimeMillis());
        Date heure3 = new Date(System.currentTimeMillis());

        bill1 = new Bill(type1,"un ticket",montant1,boutique1,heure1);
        bill2 = new Bill(type2,"un ticket",montant2,boutique1,heure2);
        bill3 = new Bill(type3,"un ticket",montant3,boutique1,heure3);
        manager = new BillsManager();

        manager.store(context,bill1);
        manager.store(context,bill2);
        manager.store(context,bill3);

        ArrayList<Bill> list = manager.load(context);
        Bill b = list.get(0);
        assertTrue(b.getId() == bill1.getId());
        b = list.get(1);
        assertTrue(b.getId() == bill2.getId());
        b = list.get(2);
        assertTrue(b.getId() == bill3.getId());

        //assertTrue(manager.flush(context,bill1));
        //assertTrue(manager.flush(context,bill2));
        //assertTrue(manager.flush(context,bill3));

    }

    @FlakyTest
    public void testFlush()
    {
        Context context = getContext();
        BillsManager manager;
        TYPE_CONTENT_BILL type1 = TYPE_CONTENT_BILL.ALIMENTAIRE;
        TYPE_CONTENT_BILL type2 = TYPE_CONTENT_BILL.LOISIR;
        TYPE_CONTENT_BILL type3 = TYPE_CONTENT_BILL.PROFESSIONNEL;
        int montant1 = 45;
        int montant2 = 50;
        int montant3 = 1000;
        Boutique boutique1 = new Boutique();
        Date heure1 = new Date(System.currentTimeMillis());
        Date heure2 = new Date(System.currentTimeMillis());
        Date heure3 = new Date(System.currentTimeMillis());

        Bill bill1,bill2,bill3;

        bill1 = new Bill(type1,"un ticket",montant1,boutique1,heure1);
        bill2 = new Bill(type2,"un ticket",montant2,boutique1,heure2);
        bill3 = new Bill(type3,"un ticket",montant3,boutique1,heure3);
        manager = new BillsManager();

        manager.store(context,bill1);
        assertTrue(manager.flush(context,bill1));
        assertFalse(manager.flush(context,bill1));
    }
}

