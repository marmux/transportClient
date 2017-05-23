package com.yupanalabs.gpstracker;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by muniz on 7/22/16.
 */
public class LocationsMap  extends Fragment {

    private static Double latitude, longitude;
    MapView mMapView;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    if (container == null) {
        return null;
    }
    View view = inflater.inflate(R.layout.location_layout, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback()  {
            @Override
            public void onMapReady(GoogleMap theGoogleMap) {
                googleMap = theGoogleMap;
            }
        });
        startUpdatingGPSdaemon();
        return view;
    }

    private void updatePositions() {
        MainActivity activity = ((MainActivity) getActivity());
        ArrayList<GPSpos> gpss;
        if (activity != null)
            gpss = activity.getmService().xmpp.mGPSposArray;
        else
            gpss = null;
        if (googleMap != null && gpss != null) {
            googleMap.clear();
        for (GPSpos gpsi : gpss) {
                googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(gpsi.mLat, gpsi.mLon))
                .title(gpsi.mJidNick));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void startUpdatingGPSdaemon() {
        new CountDownTimer(900000000, 10000) {
            public void onTick(long millisUntilFinished) {
                updatePositions();
            }
            public void onFinish() {
                this.start();
            }
        }.start();
    }

}
