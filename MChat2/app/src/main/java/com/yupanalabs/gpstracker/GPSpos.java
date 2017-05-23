package com.yupanalabs.gpstracker;

/**
 * Created by muniz on 7/9/16.
 */
public class GPSpos {
    public String mJidNick;
    public double mLon;
    public double mLat;
    public long mTime;

    public GPSpos(String jidNick, double lat, double lon, long time) {
        mJidNick = jidNick;
        mLon = lon;
        mLat = lat;
        mTime = time;
    }
}
