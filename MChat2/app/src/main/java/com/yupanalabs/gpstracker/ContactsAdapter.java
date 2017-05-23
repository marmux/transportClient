package com.yupanalabs.gpstracker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by muniz on 7/3/16.
 */
public class ContactsAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    ArrayList<String> contactsList;
    public static final String MyPREFERENCES = "user_prefs" ;
    public static final String pref_str_user2 = "str_user2";
    private Activity mActivity;
    private ViewGroup mVGparent;



    public ContactsAdapter(Activity activity, ArrayList<String> list) {
        contactsList = list;
        mActivity = activity;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return contactsList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        String contact = (String) contactsList.get(position);

        mVGparent = parent;
        if (convertView == null)
            convertView = inflater.inflate(R.layout.single_contact_layout, null);

        /*LinearLayout layout = (LinearLayout) convertView
                .findViewById(R.id.single_contact_layout);
        layout.setBackgroundResource(R.drawable.bubble1);*/


        TextView msg = (TextView) convertView.findViewById(R.id.contactTextView);
        msg.setText(contact);
        convertView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("ContactAdapter", "touch " + Integer.toString(position));
                        ViewGroup theParent = (ViewGroup) view.getParent();
                            for (int j = 0; j <  theParent.getChildCount(); j++) {
                                theParent.getChildAt(j).setSelected(false);
                            }
                        view.setSelected(true);
                        TextView from = (TextView) view.findViewById(R.id.contactTextView);
                        String sFrom = from.getText().toString();
                        String[] separated = sFrom.split("@");
                        sFrom = separated[0];

                        //maybe remove this
                        Context ctx = theParent.getContext();
                        SharedPreferences sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        Log.d("ContactAdapter", "setting receiver " + sFrom);
                        editor.putString(pref_str_user2, sFrom);
                        editor.commit();

                        //directly editing the user
                        EditText msgUser2 = (EditText) mActivity.findViewById(R.id.messageEditText);
                        msgUser2.setText("["+sFrom+"]:");
                        Selection.setSelection(msgUser2.getText(), msgUser2.getText().length());

                    }
                }
        );
        convertView.setFocusable(true);

//        LinearLayout layout = (LinearLayout) vi
//                .findViewById(R.id.bubble_layout);
//        LinearLayout parent_layout = (LinearLayout) vi
//                .findViewById(R.id.bubble_layout_parent);
//
//        // if message is mine then align to right
//        if (message.isMine) {
//            layout.setBackgroundResource(R.drawable.bubble2);
//            parent_layout.setGravity(Gravity.RIGHT);
//        }
//        // If not mine then align to left
//        else {
//            layout.setBackgroundResource(R.drawable.bubble1);
//            parent_layout.setGravity(Gravity.LEFT);
//        }
        msg.setTextColor(Color.BLACK);
        return convertView;
    }



}
