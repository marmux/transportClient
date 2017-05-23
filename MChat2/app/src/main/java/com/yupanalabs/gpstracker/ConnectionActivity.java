package com.yupanalabs.gpstracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ConnectionActivity extends AppCompatActivity {

   public static final String MyPREFERENCES = "user_prefs" ;
   public static final String pref_str_user = "str_user";
   public static final String pref_str_pass = "str_pass";
   SharedPreferences sharedpreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        load_preferences();
        Intent intent = getIntent();

    }

    public void save_preferences(View view) {
        EditText ed_user,ed_pass;
        ed_user=(EditText)findViewById(R.id.editText_user);
        ed_pass=(EditText)findViewById(R.id.editText_password);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String str_user  = ed_user.getText().toString();
        String str_pass  = ed_pass.getText().toString();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(pref_str_pass, str_pass);
        editor.putString(pref_str_user, str_user);
        editor.commit();
    }

    public void load_preferences() {
        EditText ed_user,ed_pass;
        ed_user=(EditText)findViewById(R.id.editText_user);
        ed_pass=(EditText)findViewById(R.id.editText_password);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String str_user  = sharedpreferences.getString(pref_str_user,"");
        String str_pass  = sharedpreferences.getString(pref_str_pass,"");
        ed_user.setText(str_user);
        ed_pass.setText(str_pass);
    }
}
