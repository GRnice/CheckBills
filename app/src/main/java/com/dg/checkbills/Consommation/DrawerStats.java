package com.dg.checkbills.Consommation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by Remy on 16/01/2017.
 */

public class DrawerStats extends View
{
    private boolean enableDraw = false;
    private int consoLoisir,consoPro,consoAlim,consoVoyage;
    private int ticketTotal;

    public DrawerStats(Context context)
    {
        super(context);
        setWillNotDraw(false);
    }

    public DrawerStats(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public DrawerStats(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
    }

    public void setData(int nbTicketConsoLoisir,int nbTicketConsoPro, int nbConsoAlim, int nbConsoVoyage)
    {
        consoAlim = nbConsoAlim;
        consoLoisir = nbTicketConsoLoisir;
        consoPro = nbTicketConsoPro;
        consoVoyage = nbConsoVoyage;
        ticketTotal = consoAlim+consoLoisir+consoPro+consoVoyage;
        enableDraw = true;
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (!enableDraw) return;
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int deltaEspace = (int)(0.12 * width);
        int deltaBarre = (int)(0.16 * width);

        int hauteurBarreAlim = (int) ( ( ((float)consoAlim) / ticketTotal) * height);
        int hauteurBarrePro = (int) ( ( ((float)consoPro) / ticketTotal) * height);
        int hauteurBarreLoisir = (int) ( ( ((float)consoLoisir) / ticketTotal) * height);
        int hauteurBarreVoyage = (int) ( ( ((float)consoVoyage) / ticketTotal) * height);

        Rect rectangleAlim = new Rect(0,0,deltaBarre,hauteurBarreAlim);
        Rect rectanglePro = new Rect(deltaBarre+deltaEspace,0,deltaBarre+deltaEspace+deltaBarre,hauteurBarrePro);
        Rect rectangleLoisir = new Rect(2*(deltaBarre+deltaEspace),0,deltaBarre+2*(deltaEspace+deltaBarre),hauteurBarreLoisir);
        Rect rectangleVoyage = new Rect(3*(deltaBarre+deltaEspace),0,deltaBarre+3*(deltaEspace+deltaBarre),hauteurBarreVoyage);

        Paint paintColor = new Paint();

        paintColor.setColor(Color.GREEN);
        canvas.drawRect(rectangleAlim,paintColor);
        paintColor.setColor(Color.BLUE);
        canvas.drawRect(rectanglePro,paintColor);
        paintColor.setColor(Color.YELLOW);
        canvas.drawRect(rectangleLoisir,paintColor);
        paintColor.setColor(Color.MAGENTA);
        canvas.drawRect(rectangleVoyage,paintColor);
    }
}
