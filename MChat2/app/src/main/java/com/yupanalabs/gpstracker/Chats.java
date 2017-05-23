package com.yupanalabs.gpstracker;


import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;



public class Chats extends Fragment implements OnClickListener {

    public static final String MyPREFERENCES = "user_prefs" ;
    public static final String pref_str_user1 = "str_user1";
    public static final String pref_str_user2 = "str_user2";
    private EditText msg_edittext;
    public  String user1,user2;
    private Random random;
    public static ArrayList<ChatMessage> chatlist;
    public static ChatAdapter chatAdapter;
    ListView msgListView;
    SharedPreferences sharedpreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_layout, container, false);
        random = new Random();
        msg_edittext = (EditText) view.findViewById(R.id.messageEditText);
        setReceiver();
        msgListView = (ListView) view.findViewById(R.id.msgListView);

        ImageButton sendButton = (ImageButton) view
                .findViewById(R.id.sendMessageButton);
        sendButton.setOnClickListener(this);

        // ----Set autoscroll of listview when a new message arrives----//
        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);

        chatlist = new ArrayList<ChatMessage>();
        chatAdapter = new ChatAdapter(getActivity(), chatlist);
        msgListView.setAdapter(chatAdapter);

        msgListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                //view.setSelected(true);
                Log.i("Contacts", "OnClick in listener position"+String.valueOf(position));
            }
        });
        Context ctx = super.getContext();
        sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        user1  = sharedpreferences.getString(pref_str_user1,"");
        user2  = sharedpreferences.getString(pref_str_user2,"");
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        // Apply any required UI change now that the Fragment is visible.
        // maybe remove this
        setReceiver();
    }

    @Override
    public void onResume(){
        //super.onStart();
        super.onResume();
        // Apply any required UI change now that the Fragment is visible.
        setReceiver();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    public final void setReceiver() {
        Context ctx = super.getContext();
        SharedPreferences sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        user2  = sharedpreferences.getString(pref_str_user2,"");
        Log.d("Chats", "setReciever, user2 " + user2);
        msg_edittext.setText("["+user2+":]");
        Selection.setSelection(msg_edittext.getText(), msg_edittext.getText().length());

    }


    public void sendTextMessage(View v) {
        String message = msg_edittext.getEditableText().toString();


        setReceiver();
        if (!message.equalsIgnoreCase("")) {
            ChatMessage chatMessage = new ChatMessage(user1, user2,
                    message, "" + random.nextInt(1000), true);
            chatMessage.setMsgID();

            Log.d("Chats", "SendTextMessage, user2 " + user2 + "reciever:" + chatMessage.receiver);

            chatMessage.body = message;
            chatMessage.Date = CommonMethods.getCurrentDate();
            chatMessage.Time = CommonMethods.getCurrentTime();

            //msg_edittext.setText("["+user2+":]");
            chatAdapter.add(chatMessage);
            chatAdapter.notifyDataSetChanged();
            MainActivity activity = ((MainActivity) getActivity());
            activity.getmService().xmpp.sendMessage(chatMessage);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendMessageButton:
                sendTextMessage(v);

        }
    }

}
/**
 * Created by muniz on 6/8/16.
 */
