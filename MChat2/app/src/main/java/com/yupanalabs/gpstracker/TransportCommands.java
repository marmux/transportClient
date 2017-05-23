package com.yupanalabs.gpstracker;

import android.util.Log;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.ArrayList;

/**
 * Created by muniz on 7/13/16.
 */
public class TransportCommands {

    public static String transportCommandStr = "<transportCommand>";
    public static String gpsCommandStr = "<gpspos>";
    public static String mucsCommandStr = "<mucs>";


    public boolean isTransportCommand(String messageBody) {
        if (messageBody.indexOf(transportCommandStr) != -1)
            return true;
        else
            return false;
    }

    public boolean isGpsCommand(String str) {
        if (isTransportCommand(str) && str.indexOf(gpsCommandStr)!= -1)
            return true;
        else
            return false;
    }

    public boolean isMucsCommand(String str) {
        if (isTransportCommand(str) && str.indexOf(mucsCommandStr) != -1)
            return true;
        else
            return false;
    }

    public String[] getMucsFromMessage(String msg) {
        String str1 = removeCommand(transportCommandStr,msg);
        String str2 = removeCommand(mucsCommandStr,str1);
        String[] mucs = str2.split(",");
        return mucs;
    }

    public ArrayList<GPSpos> getGPSposFromMessage(String msg) {
        ArrayList<GPSpos> gpss;
        gpss = new ArrayList<GPSpos>();
        String str1 = removeCommand(transportCommandStr,msg);
        String str2 = removeCommand(gpsCommandStr,str1);
        String[] jidWighGPS = str2.split("&");
        for (String stri : jidWighGPS) {
            gpss.add(getGPSposFromString(stri));
        }
        return gpss;
    }

    private GPSpos getGPSposFromString(String str) {
        String jidNick;
        double lon;
        double lat;
        long time;
        String[] strs = str.split(",");
        jidNick = strs[0];
        lat = Double.parseDouble(strs[1]);
        lon = Double.parseDouble(strs[2]);
        time = Long.parseLong(strs[3]);
        GPSpos jidgps;
        jidgps = new GPSpos(jidNick,lat,lon,time);
        return jidgps;
    }


    private String removeCommand(String cmd, String str) {
        int len = cmd.length();
        String newString = str.substring(len);
        return newString;
    }
}
