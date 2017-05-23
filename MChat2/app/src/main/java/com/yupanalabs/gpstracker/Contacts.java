package com.yupanalabs.gpstracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

import org.jivesoftware.smack.roster.RosterEntry;


/**
 * Created by muniz on 7/3/16.
 */
public class Contacts extends Fragment {


    public static ContactsAdapter contactsAdapter;
    ListView conListView;
    public static ArrayList<String> contacts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contacts_layout, container, false);

        conListView = (ListView) view.findViewById(R.id.contactsListView);
        contacts = new ArrayList<String>();
        MainActivity activity = ((MainActivity) getActivity());
        //this will crash if roster is still empty
        if (activity.getmService().xmpp.connected) {
            if (activity.getmService().xmpp.roster != null) {
                Set<RosterEntry> entries = activity.getmService().xmpp.roster.getEntries();
                for (RosterEntry entry : entries) {
                    Log.i("Contacts: roster", entry.getUser());
                    contacts.add(entry.getUser());

                }
            }
            String[] mucs = activity.getmService().xmpp.mucRooms;
            if (mucs != null ) {
                for (String stri : mucs) {
                    if (!contacts.contains(stri))
                        contacts.add(stri);
                }
                //now add the muc chat to rooster
                //contacts.add(activity.getmService().xmpp.mucChatName);
            }
        }

        contactsAdapter = new ContactsAdapter(getActivity(), contacts);
        conListView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        conListView.setAdapter(contactsAdapter);

        conListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                //view.setSelected(true);
                Log.i("Contacts", "OnClick in listener position"+String.valueOf(position));
            }
        });
        return view;
    }


    @Override
    public void onStart(){
        super.onStart();
        // Apply any required UI change now that the Fragment is visible.
    }

    @Override
    public void onResume(){
        super.onResume();
        // Apply any required UI change now that the Fragment is visible.
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    /*@Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contactsListView:
                Log.i("Contacts", "OnClick method");
                break;
            default:
                Log.i("Contacts", "OnClick method id:" + String.valueOf(v.getId()));
                break;
        }
    }*/

}
