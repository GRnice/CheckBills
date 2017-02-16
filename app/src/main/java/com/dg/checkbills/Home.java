package com.dg.checkbills;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dg.checkbills.AjoutTicket.ProcedureTicket;
import com.dg.checkbills.Consommation.ConsoActivity;
import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Daemon.ServiceSocket;
import com.dg.checkbills.Daemon.ServiceStorage;
import com.dg.checkbills.Historique.HistoriqueActivity;
import com.dg.checkbills.ZoneInfluence.InfluenceActivity;


public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (! isMyServiceRunning(ServiceSocket.class,this))
        {
            Log.e("GEN NEW SERVICE","GEN AT ONCREATE");
            WakefulBroadcastReceiver wakeful = new WakefulReceiver();
            Intent intent = new Intent(Home.this,ServiceSocket.class);
            wakeful.startWakefulService(this,intent);
        }

        if (! isMyServiceRunning(ServiceStorage.class,this))
        {
            Log.e("GEN NEW SERVICE","GEN AT ONCREATE");
            WakefulBroadcastReceiver wakeful = new WakefulReceiver();
            Intent intent = new Intent(Home.this,ServiceStorage.class);
            wakeful.startWakefulService(this,intent);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Home.this, ProcedureTicket.class);
                startActivityForResult(myIntent,1123);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }


    @Override
    public void onDestroy()
    {
        Intent intent = new Intent(Home.this,ServiceStorage.class);
        stopService(intent);
        super.onDestroy();
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Activity act) {
        ActivityManager manager = (ActivityManager) act.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera)
        {
            Intent myIntent = new Intent(Home.this, ProcedureTicket.class);
            startActivityForResult(myIntent,1123);
        }
        else if (id == R.id.nav_historique)
        {
            Intent intentHistorique = new Intent(Home.this,HistoriqueActivity.class);
            startActivityForResult(intentHistorique,1124);
        }
        else if (id == R.id.nav_parametres)
        {
            Intent intentConso = new Intent(Home.this, ConsoActivity.class);
            startActivityForResult(intentConso,1125);
        }
        else if (id == R.id.nav_share)
        {
            Intent intentInfluence = new Intent(Home.this, InfluenceActivity.class);
            startActivityForResult(intentInfluence,1126);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // SYNCH
        Log.e("Home-SYNCHRO-boutique","!");
        Intent intentSynchBoutique = new Intent();
        intentSynchBoutique.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
        intentSynchBoutique.putExtra("SYNCHBOUTIQUE","");
        sendBroadcast(intentSynchBoutique);
    }

    public class WakefulReceiver extends WakefulBroadcastReceiver {
        public WakefulReceiver()
        {
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            startWakefulService(context,intent);
        }
    }
}
