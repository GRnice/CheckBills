package com.dg.checkbills.Data;

import java.util.ArrayList;

/**
 * Created by Remy on 12/01/2017.
 */

public class ArrayUtils
{
    public static byte[] toPrimitive(ArrayList<Byte> array)
    {
        byte[] arrayByte = new byte[array.size()];
        for (int i = 0 ; i < array.size() ; i++)
        {
            arrayByte[i] = (byte) array.get(i);
        }
        return arrayByte;
    }

    public static ArrayList<Byte> toArray(byte[] array)
    {
        ArrayList<Byte> arrayByte = new ArrayList<>();
        for (int i = 0 ; i < array.length ; i++)
        {
            arrayByte.add(array[i]);
        }
        return arrayByte;
    }
}
