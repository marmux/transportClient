package com.yupanalabs.gpstracker;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by muniz on 7/9/16.
 */
public class MyGPSLocation implements  LocationListener {

    private LocationManager locationManager;
    MyService mContext;
    String user1;
    public static MyGPSLocation instance = null;
    public static boolean instanceCreated = false;
    public GPSpos gpspos = null;
    public static final String MyPREFERENCES = "user_prefs" ;
    public static final String pref_str_user1 = "str_user1";
    public boolean mHasPermission = true;

    public MyGPSLocation(MyService context) {
        Log.d("MyGPSLocation", "Constructor");
        this.mContext = context;
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        user1  = sharedpreferences.getString(pref_str_user1,"");

        locationManager = (LocationManager) mContext.getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);


        gpspos = new GPSpos(user1,0,0,0);
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Google", "Somethinig wrong with location settings");
            // TODO: Consider calling, this is the problem!!
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mHasPermission = false;
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 50, this);
    }

    public static MyGPSLocation getInstance(MyService context) {

        if (instance == null) {
            instance = new MyGPSLocation(context);
            instanceCreated = true;
        }
        return instance;
    }

    public GPSpos getGPSpos() {
        Log.e("MyGPSLocation", "Getting Location, permission:" + String.valueOf(mHasPermission));
        return gpspos;
    }

    private LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.e("Google", "Location Changed");
            if (location == null)
                return;
            try {
                Log.e("latitude", location.getLatitude() + "");
                Log.e("longitude", location.getLongitude() + "");
                gpspos.mLat = location.getLatitude();
                gpspos.mLon = location.getLongitude();
                gpspos.mTime = location.getTime();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

    };

    @Override
    public void onLocationChanged(Location location) {
        Log.e("Google", "Location Changed");
        if (location == null)
            return;
        try {
            Log.e("latitude", location.getLatitude() + "");
            Log.e("longitude", location.getLongitude() + "");
            gpspos.mLat = location.getLatitude();
            gpspos.mLon = location.getLongitude();
            gpspos.mTime = location.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }


}