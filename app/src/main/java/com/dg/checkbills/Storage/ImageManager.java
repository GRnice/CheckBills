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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Remy on 02/02/2017.
 */

public class ImageManager
{
    public static HashMap<String,ArrayList<Byte>> load(Context ctx)
    {
        ArrayList<Boutique> listOfBoutique = new ArrayList<>();
        FileInputStream fis = null;
        ObjectInputStream is= null;
        Object obj;
        try {
            fis = ctx.openFileInput("MAPIMAGE");
            is = new ObjectInputStream(fis);
            HashMap<String,ArrayList<Byte>> map = (HashMap<String,ArrayList<Byte>>) is.readObject();
            is.close();
            return map;

        } catch (FileNotFoundException e) {
            return new HashMap<String,ArrayList<Byte>>();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new HashMap<String,ArrayList<Byte>>();
    }

    public static boolean flush(Context context)
    {
        return context.deleteFile("MAPIMAGE");
    }

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
}
