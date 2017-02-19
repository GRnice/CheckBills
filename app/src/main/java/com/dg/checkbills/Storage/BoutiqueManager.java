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


    public static void store(Context context, ArrayList<Boutique> boutique)
    {
        FileOutputStream fos = null;
        ObjectOutputStream os;
        try
        {
            fos = context.openFileOutput("BOUTIQUE", Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);

            os.writeObject(boutique);
            os.close();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Charge tout les tickets puis les retourne
     * @param ctx
     * @return
     */
    public static ArrayList<Boutique> load(Context ctx)
    {
        ArrayList<Boutique> arrayBoutique = new ArrayList<>();
        FileInputStream fis = null;
        ObjectInputStream is = null;

        try {
            fis = ctx.openFileInput("BOUTIQUE");
            is = new ObjectInputStream(fis);
            arrayBoutique = (ArrayList<Boutique>) is.readObject();
            is.close();
            return arrayBoutique;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return arrayBoutique;
    }
}
