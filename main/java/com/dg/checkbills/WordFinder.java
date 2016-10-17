package com.dg.checkbills;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by Remy on 17/10/2016.
 */
public class WordFinder extends AsyncTask<Bitmap,Integer,Void>
{
    public String ttc = null;
    public Bitmap src = null;
    public String positionGPS =null;
    public String nameOfShop = null;
    public String[] texts;
    TessBaseAPI tess;

    final int nbFrames = 9;

    public WordFinder(TessBaseAPI tesseract)
    {
        super();
        this.texts = new String[nbFrames];
        this.tess = tesseract;
    }

    public boolean analyseText(int iter)
    {
        for (int i = 0 ; i < iter ; i++)
        {
            this.texts[i] = this.texts[i].toLowerCase();
            Log.e("MESSAGE",String.valueOf(i));
            Log.e("Vla",this.texts[i]);
            if (this.ttc == null && this.texts[i].contains("eur"))
            {
                String[] tab = this.texts[i].split("eur");
                String msg = tab[0].replaceAll("[^0-9.,]+","");
                this.ttc = msg;

            }
            else if (this.ttc == null && this.texts[i].contains("ttc"))
            {
                String[] stab = this.texts[i].split("ttc");
                String msg = stab[0].replaceAll("/[^0-9.,]+/","");
                this.ttc = stab[0];
            }
            else if(this.texts[i].contains("total"))
            {
                String subtext = this.texts[i].replaceAll("[^0-9.,]+","");
                Log.e("WARNING->",subtext);
                String[] stab = this.texts[i].split("total");
                this.ttc = subtext;
            }
        }
        return true;
    }

    @Override
    protected Void doInBackground(Bitmap... params)
    {
        src = params[0];

        for (int iter = 4 ; iter <= nbFrames ; iter++)
        {
            int delta = src.getHeight() / iter;
            Bitmap crop;
            for (int i = 0 ; i < iter ; i++)
            {
                crop = Bitmap.createBitmap(src,0,i*delta,src.getWidth(),delta);
                this.tess.setImage(crop);
                this.texts[i] = this.tess.getUTF8Text();
                Log.e("MESSAGE",String.valueOf(i));
                Log.e("====",this.texts[i]);
            }
            this.analyseText(iter);
        }
        if (this.ttc != null)
        {
            Log.e("COUCOU",this.ttc);
        }
        else
        {
            Log.e("FAIL","FAIL");
        }

        return null;
    }
}
