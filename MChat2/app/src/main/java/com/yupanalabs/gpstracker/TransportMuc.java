package com.yupanalabs.gpstracker;

import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Created by muniz on 8/5/16.
 */
public class TransportMuc {
    public String mMucJid;
    public boolean mJoined;
    public MultiUserChat mMucChat;

    public TransportMuc(String mucJid, boolean joined, MultiUserChat mucChat) {
        mMucJid = mucJid;
        mJoined = joined;
        mMucChat = mucChat;
    }
}
