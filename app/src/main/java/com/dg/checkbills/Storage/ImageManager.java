package com.dg.checkbills.Storage;

import android.content.Context;

import com.dg.checkbills.Data.Boutique;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Remy on 02/02/2017.
 */

public class ImageManager
{

    /**
     * Retourne une file de string, où chaque string est le nom d'une image sauvegardée en local.
     * @param ctx
     * @return
     */
    public static ArrayDeque<String> loadHistorique(Context ctx)
    {
        ArrayDeque<String> fileNomFichier = new ArrayDeque<>();
        FileInputStream fis = null;
        ObjectInputStream is= null;

        try {
            fis = ctx.openFileInput("DEQUEUE-HISTORIQUE");
            is = new ObjectInputStream(fis);
            fileNomFichier = (ArrayDeque<String>) is.readObject();
            is.close();
            return fileNomFichier;

        } catch (FileNotFoundException e) {
            return fileNomFichier;
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return fileNomFichier;

    }

    public static void saveHistorique(Context ctx,ArrayDeque<String> fileHistorique)
    {
        FileOutputStream fos = null;
        ObjectOutputStream os;
        try
        {
            fos = ctx.openFileOutput("DEQUEUE-HISTORIQUE", Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(fileHistorique);
            os.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void deleteImage(Context ctx,String nomFichier)
    {
        ctx.deleteFile(nomFichier);
    }

    public static void storeImage(Context ctx,String nomFichier,ArrayList<Byte> image)
    {
        FileOutputStream fos = null;
        ObjectOutputStream os;

        try
        {
            fos = ctx.openFileOutput(nomFichier, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(image);
            os.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static ArrayList<Byte> loadImage(Context ctx,String nomFichierImage)
    {
        ArrayList<Byte> image = new ArrayList<>();
        FileInputStream fis = null;
        ObjectInputStream is = null;

        try {
            fis = ctx.openFileInput(nomFichierImage);
            is = new ObjectInputStream(fis);
            image = (ArrayList<Byte>) is.readObject();
            is.close();
            return image;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static boolean flush(Context context)
    {
        return context.deleteFile("MAPIMAGE");
    }

    /*
    public static void store(Context context, HashMap<String,ArrayList<Byte>> mapImage)
    {
        FileOutputStream fos = null;
        ObjectOutputStream os;
        try
        {
            fos = context.openFileOutput("MAPIMAGE", Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);

            os.writeObject(mapImage);
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
    */
}
