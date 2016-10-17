package com.dg.checkbills;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;


import com.googlecode.tesseract.android.TessBaseAPI;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity
{
    private TessBaseAPI ts;
    private Bitmap bitmap;
    private Socket mSocket;
    private WordFinder wordFinder;
    {

    }
    private static final int SELECT_PHOTO = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ts = new TessBaseAPI();

        //mSocket.connect();

        File externalStorageDirectory = Environment.getExternalStorageDirectory();

        File appDir = new File(externalStorageDirectory, "ocrsample");
        if (!appDir.isDirectory())
            appDir.mkdir();

        final File baseDir = new File(appDir, "tessdata");
        if (!baseDir.isDirectory())
            baseDir.mkdir();

        ts.init(appDir.getPath(), "fra");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.this.check();
                            }
                        }).show();
            }
        });
    }

    public void pickImage()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 155);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 155 && resultCode == Activity.RESULT_OK)
        {
            if (data == null) {
                //Display an error
                return;
            }

            try {
                InputStream stream = getContentResolver().openInputStream(
                        data.getData());

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inSampleSize = 2;
                options.inScreenDensity = DisplayMetrics.DENSITY_DEFAULT;


                this.bitmap = BitmapFactory.decodeStream(stream,null,options);
                this.bitmap = this.doGamma(this.bitmap,1.8,1.8,1.8);

                //this.bitmap = this.sharpen(this.bitmap,3);

                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            Matrix matrix = new Matrix();
            // resize the bit map
            //matrix.postScale(scaleWidth, scaleHeight);

            matrix.postRotate(90);

            this.bitmap = Bitmap.createBitmap(this.bitmap, 0, 0,
                    this.bitmap.getWidth(), this.bitmap.getHeight(), matrix, true);

            if (this.bitmap == null)
            {
                Log.e("III","CHIER");
                return;
            }
            final ImageView p = (ImageView) findViewById(R.id.imageView);
            p.setImageBitmap(bitmap);
            final TextView t = (TextView) findViewById(R.id.resultat);
            this.wordFinder = new WordFinder(ts);
            this.wordFinder.execute(this.bitmap);

            Log.e("UI","GOOD");
        }
    }

    public Bitmap doGamma(Bitmap src,double red,double green,double blue)
    {
        Bitmap bmout = Bitmap.createBitmap(src.getWidth(),src.getHeight(),src.getConfig());
        int width = src.getWidth();
        int height = src.getHeight();
        int A,R,G,B;
        int pixel;
        final int MAX_SIZE = 256;
        final double MAX_VALUE_DBL = 255.0;
        final int MAX_VALUE_INT = 255;
        final double REVERSE = 1.0;

        // gamma arrays
        int[] gammaR = new int[MAX_SIZE];
        int[] gammaG = new int[MAX_SIZE];
        int[] gammaB = new int[MAX_SIZE];


        for (int i = 0 ; i< MAX_SIZE ; ++i)
        {
            gammaR[i] = (int) Math.min(MAX_VALUE_INT,
                    (int) ((MAX_VALUE_DBL * Math.pow(i/MAX_VALUE_DBL,REVERSE/red)) + 0.5));
            gammaG[i] = (int) Math.min(MAX_VALUE_INT,
                    (int) ((MAX_VALUE_DBL * Math.pow(i/MAX_VALUE_DBL,REVERSE/green)) + 0.5));
            gammaB[i] = (int) Math.min(MAX_VALUE_INT,
                    (int) ((MAX_VALUE_DBL * Math.pow(i/MAX_VALUE_DBL,REVERSE/blue)) + 0.5));

        }

        for (int x = 0 ; x < width ; ++x)
        {
            for(int y = 0 ; y < height ; ++y)
            {
                pixel = src.getPixel(x,y);
                A = Color.alpha(pixel);
                R = gammaR[Color.red(pixel)];
                G = gammaG[Color.green(pixel)];
                B = gammaB[Color.blue(pixel)];
                bmout.setPixel(x,y,Color.argb(A,R,G,B));
            }
        }

        return bmout;

    }

    public void check()
    {
        Bitmap b = BitmapFactory.decodeResource(getBaseContext().getResources(),R.mipmap.fu);
        Log.e("WARNING","FALSE");
        //ts.setImage(b);
       // Log.e("MAYBE",ts.getUTF8Text());
        this.pickImage();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
