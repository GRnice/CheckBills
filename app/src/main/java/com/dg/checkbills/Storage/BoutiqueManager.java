package com.dg.checkbills.Storage;

/**
 * Created by Remy on 21/12/2016.
 */

import android.content.Context;
import android.util.Log;


import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.Data.Boutique;

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
public class BoutiqueManager
{
    public BoutiqueManager()
    {

    }

    /**
     * Supprime le ticket, pratique pour les tests...
     * @param context
     * @param boutique
     * @return
     */
    public static boolean flush(Context context,Boutique boutique)
    {
        return context.deleteFile("BOUTIQUE-"+String.valueOf(boutique.getId()));
    }

    /**
     * Sauve un boutique dans un fichier pour le rendre persistant, il sera transféré plus tard sur le cloud
     * @param context
     * @param boutique
     */
    public static void store(Context context, Boutique boutique)
    {
        FileOutputStream fos = null;
        ObjectOutputStream os;
        try
        {
            fos = context.openFileOutput("BOUTIQUE-"+String.valueOf(boutique.getId()), Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);

            os.writeObject(boutique);
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
    public static ArrayList<Boutique> load(Context context)
    {

        ArrayList<Boutique> listOfBoutique = new ArrayList<>();
        FileInputStream fis = null;
        ObjectInputStream is= null;

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
                String nameFile = path[path.length - 1];
                Log.e("namefile",nameFile);
                if ((nameFile.length() > 8) && (nameFile.substring(0,8).equals("BOUTIQUE")))
                {
                    fis = context.openFileInput(nameFile);
                    is = new ObjectInputStream(fis);
                    while (is.available() == 0) // j'itere dessus jusqu'à ce que une exception EOF est levée, le available est shité
                    {
                        try
                        {
                            obj = is.readObject();

                            listOfBoutique.add((Boutique) obj);
                        }
                        catch(ClassNotFoundException c)
                        {
                            c.printStackTrace();
                        }
                    }
                    is.close();
                }


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


        return listOfBoutique;
    }
}
