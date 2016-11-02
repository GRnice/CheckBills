package com.dg.checkbills.Storage;

/**
 * Created by Remy on 31/10/2016.
 */

import android.content.Context;


import com.dg.checkbills.Data.Bill;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

/**
 * BillsManager gère les billets stockés en local
 */
public class BillsManager
{
    public BillsManager()
    {

    }

    /**
     * Supprime tout les tickets, pratique pour les tests...
     * @param context
     * @param ticket
     * @return
     */
    public boolean flush(Context context,Bill ticket)
    {
        return context.deleteFile(String.valueOf(ticket.getId()));
    }

    /**
     * Sauve un ticket dans un fichier pour le rendre persistant, il sera transféré plus tard sur le cloud
     * @param context
     * @param ticket
     */
    public void store(Context context, Bill ticket)
    {
        FileOutputStream fos = null;
        ObjectOutputStream os;
        try
        {
            fos = context.openFileOutput(String.valueOf(ticket.getId()), Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);

            os.writeObject(ticket);
            os.close();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }


    }

    /**
     * Charge tout les tickets puis les retourne
     * @param context
     * @return
     */
    public ArrayList<Bill> load(Context context)
    {
        FileInputStream fis = null;
        ObjectInputStream is= null;
        ArrayList<Bill> listOfBills = new ArrayList<>();
        Object obj;

        File[] listFiles = context.getFilesDir().listFiles();
        for (File file : listFiles)
        {
            if (file.isDirectory())
            {
                // si le fichier est un dossier on l'ignore
                continue;
            }
            String[] path = file.getPath().split("/");
            try
            {
                // path[path.length - 1] est le nom du fichier
                fis = context.openFileInput(path[path.length - 1]);
                is = new ObjectInputStream(fis);
                while (is.available() == 0) // j'itere dessus jusqu'à ce que une exception EOF est levée, le available est shité
                {
                    try
                    {
                        obj = is.readObject();

                        listOfBills.add((Bill) obj);
                    }
                    catch(ClassNotFoundException c)
                    {
                        c.printStackTrace();
                    }
                }

                is.close();


            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return listOfBills;
    }
}
