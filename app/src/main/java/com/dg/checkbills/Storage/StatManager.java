package com.dg.checkbills.Storage;

import android.content.SharedPreferences;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dg.checkbills.Consommation.Statistiques;
import com.dg.checkbills.Data.Bill;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Remy on 16/01/2017.
 */

public class StatManager
{
    private ArrayList<Statistiques> listOfStats;
    private ArrayList<String> allDates;
    private SharedPreferences shared;
    /**
     *
     * @param shared
     */
    public StatManager(SharedPreferences shared)
    {
        this.shared = shared;
        Set<String> allData = shared.getStringSet("allData",new HashSet<String>());
        // syntaxe MM-yyyy,nbtickets,depenseTotal,nbTicketConsoLoisir,nbTicketConsoProfessionnelle,nbticketConsoAlimentaire,nbTicketConsoVoyage
        Iterator<String> iterator = allData.iterator();
        listOfStats = new ArrayList<>();
        allDates = new ArrayList<>();

        while (iterator.hasNext())
        {
            String next = iterator.next();
            String[] data = next.split("\\,");
            Log.e("dateStatMan",""+data[0]);
            Statistiques stat;
            String date;


            date = data[0];
            stat = new Statistiques(date,Integer.decode(data[1]),
                    Double.parseDouble(data[2]),Integer.decode(data[3]),
                    Integer.decode(data[4]),Integer.decode(data[5]),Integer.decode(data[6]));
            listOfStats.add(stat);
            allDates.add(date);

        }
        Collections.sort(listOfStats,new ComparatorStat());
    }

    @Nullable
    public Statistiques getLastStat()
    {
        if (listOfStats.size() > 0)
        {
            return listOfStats.get(listOfStats.size()-1);
        }
        return null;
    }

    public ArrayList<String> getAllDates()
    {
        return allDates;
    }

    public ArrayList<Statistiques> getStats()
    {
        return listOfStats;
    }

    public void saveAll()
    {
        SharedPreferences.Editor editor = shared.edit();
        Set<String> stored = new HashSet<>();

        for (Statistiques stat : listOfStats)
        {
            StringBuilder sb = new StringBuilder();
            Log.e("statdate",stat.getDate());
            sb.append(stat.getDate()+",");
            sb.append(stat.getNbTickets()+",");
            sb.append(String.valueOf(stat.getDepenseTotal())+",");
            sb.append(String.valueOf(stat.getConsoLoisir())+",");
            sb.append(String.valueOf(stat.getConsoPro())+",");
            sb.append(String.valueOf(stat.getConsoAlimentaire())+",");
            sb.append(String.valueOf(stat.getConsoVoyage()));
            Log.e("storedState",sb.toString());
            stored.add(sb.toString());
        }

        editor.putStringSet("allData",stored);
        editor.commit();
    }

    public void storeBill(Bill bill)
    {
        //dd-MM-yyyy hh:mm a

        String[] date = bill.getDate().split("\\s+");
        date = date[0].split("\\-");
        Log.e("storeBill-annee",""+date[0]);
        Log.e("storeBill-mois",""+date[1]);
        Log.e("storeBill-jour",""+date[2]);

        String dateformated = date[1]+"-"+date[0];



        if (listOfStats.size() == 0)
        {
            Statistiques nwstat = new Statistiques(dateformated,0,0,0,0,0,0);
            nwstat.addBill(bill);
            listOfStats.add(nwstat);
            allDates.add(dateformated);
        }
        else
        {
            Statistiques lastStat = listOfStats.get(listOfStats.size()-1);
            if (lastStat.getDate().compareTo(dateformated) != 0)
            {
                // si la date est differente
                Statistiques nwstat = new Statistiques(dateformated,0,0,0,0,0,0);
                nwstat.addBill(bill);
                listOfStats.add(nwstat);
                allDates.add(dateformated);
            }
            else
            {
                lastStat.addBill(bill);
            }
        }
    }

    private class ComparatorStat implements Comparator<Statistiques>
    {
        @Override
        public int compare(Statistiques lhs, Statistiques rhs)
        {
            return lhs.compareTo(rhs.getDate());
        }
    }
}