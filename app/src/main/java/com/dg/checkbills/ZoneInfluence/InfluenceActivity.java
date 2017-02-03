package com.dg.checkbills.ZoneInfluence;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.ZoneInfluence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import com.dg.checkbills.R;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class InfluenceActivity extends AppCompatActivity
{

    MapView mMapView;
    private GoogleMap googleMap;
    private ArrayList<ZoneInfluence> zonesInfluentes;
    private ServiceReceiver serviceReceiver;


    public void drawZonesInfluentes()
    {
        for (ZoneInfluence zone : zonesInfluentes)
        {
            Log.e("draw zone influente",String.valueOf(zone.getLatLng().latitude)+","+String.valueOf(zone.getLatLng().longitude));
            googleMap.addCircle(new CircleOptions()
                    .center(zone.getLatLng())
                    .radius(Math.log(zone.getPoids())*15)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.argb(50, 250, 0, 0))
            );
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_influence);

        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastAddr.ACTION_TO_ACTIVITY_FROM_SERVICE.getAddr());
        registerReceiver(serviceReceiver,intentFilter);

        Intent nwIntent = new Intent();
        nwIntent.putExtra("GET_ZONES_INFLUENCES","");
        nwIntent.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
        sendBroadcast(nwIntent);

        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                int res = checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                // For showing a move to my location button
                googleMap.setMyLocationEnabled(false);
                LatLng sophia = new LatLng(43.6155793,7.0696861);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sophia).zoom(5).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                if (res != PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }
            }
        });
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(serviceReceiver);
        super.onDestroy();

    }


    /**
     * ServiceReceiver , recoit les messages venants du serveur
     *
     * ACTION_RECEIVE_FROM_SERVICE
     */
    private class ServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            Log.e("GGII","??");
            Log.e("EPISODE-02","zones");

            if (arg1.hasExtra("ZONES-INFLUENTES"))
            {
                zonesInfluentes = (ArrayList<ZoneInfluence>) arg1.getSerializableExtra("ZONES-INFLUENTES");

                Log.e("EPISODE-03","zones");
                Log.e("EPISODE-03",String.valueOf(zonesInfluentes.size()));
                drawZonesInfluentes();
            }
        }
    }


}
