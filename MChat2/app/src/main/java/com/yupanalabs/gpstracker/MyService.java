package com.yupanalabs.gpstracker;

import org.jivesoftware.smack.chat.Chat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.CountDownTimer;
import android.os.IBinder;

import java.util.ArrayList;


public class MyService extends Service {
    public static final String MyPREFERENCES = "user_prefs" ;
    public static final String pref_str_user = "str_user";
    public static final String pref_str_pass = "str_pass";
    private String USERNAME;
    private String PASSWORD;
    private String DOMAIN;
    public static ConnectivityManager cm;
    public static MyXMPP xmpp;
    public static MyGPSLocation myGPSLocation;
    public static boolean ServerchatCreated = false;
    String text = "";



    @Override
    public IBinder onBind(final Intent intent) {
        return new LocalBinder<MyService>(this);
    }

    public Chat chat;

    @Override
    public void onCreate() {
        super.onCreate();
        Context ctx = getApplicationContext();
        DOMAIN = ctx.getResources().getString(R.string.domain_server);
        SharedPreferences sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        USERNAME  = sharedpreferences.getString(pref_str_user,"");
        PASSWORD  = sharedpreferences.getString(pref_str_pass,"");
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        xmpp = MyXMPP.getInstance(MyService.this, DOMAIN, USERNAME, PASSWORD);
        xmpp.connect("onCreate");

        myGPSLocation = MyGPSLocation.getInstance(MyService.this);

        startSendingGPSdaemon();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        xmpp.disconnect();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        xmpp.connection.disconnect();
    }

    public static boolean isNetworkConnected() {
        return cm.getActiveNetworkInfo() != null;
    }

    public void startSendingGPSdaemon() {
        new CountDownTimer(900000000, 20000) {
            public void onTick(long millisUntilFinished) {
                xmpp.sendCurrentGPSPOS(myGPSLocation.getGPSpos());
            }
            public void onFinish() {
                this.start();
            }
        }.start();
    }
}