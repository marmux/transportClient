package com.yupanalabs.gpstracker;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by muniz on 6/18/16.
 */


public class MyXMPP {

    public static boolean connected = false;
    public boolean loggedin = false;
    public boolean mInMucChat = false;
    public boolean mUserSet = false;
    public static boolean isconnecting = false;
    public static boolean isToasted = true;
    private boolean chat_created = false;
    private boolean chat_created_bot = false;
    private String serverAddress;
    private String hostAddress;
    public static XMPPTCPConnection connection;
    public static String loginUser;
    public static String passwordUser;
    Gson gson;
    MyService context;
    public static MyXMPP instance = null;
    public static boolean instanceCreated = false;
    public Roster roster;
    public Collection<RosterEntry> entries;
    Presence presence;
    MultiUserChatManager mucManager;
    //    MultiUserChat mucChat;
    //this name should be assigned automatically to the user after joining in
    public String mucChatNick;
    public String mucChatName;
    public String mucService;

    private int sFromLen = 0;
    private String mCaller;
    public static boolean mGotMucs = false;
    TransportCommands transportCommands;
    public ArrayList<GPSpos> mGPSposArray; //it will contain current gpspos gotten from server.
    public String[] mucRooms;
    ArrayList<TransportMuc> trMucs;
    public org.jivesoftware.smack.chat.Chat Mychat;
    public org.jivesoftware.smack.chat.Chat MychatBot;
    ChatManagerListenerImpl mChatManagerListener;
    MMessageListener mMessageListener;
    String text = "";
    String mMessage = "", mReceiver = "";


    public MyXMPP(MyService context, String serverAdress, String logiUser,
                  String passwordser) {
        this.serverAddress = serverAdress;
        this.hostAddress = context.getResources().getString(R.string.host_server);

        this.loginUser = logiUser;
        this.passwordUser = passwordser;
        this.context = context;
        mucService = context.getResources().getString(R.string.muc_chat_service);
        init();
    }

    public static MyXMPP getInstance(MyService context, String server,
                                     String user, String pass) {
        if (instance == null) {
            instance = new MyXMPP(context, server, user, pass);
            instanceCreated = true;
        }
        else {
            loginUser = user;
            passwordUser = pass;
            mGotMucs = false;
        }
        return instance;
    }


    static {
        try {
            Class.forName("org.jivesoftware.smack.ReconnectionManager");
        } catch (ClassNotFoundException ex) {
            // problem loading reconnection manager
        }
    }

    public void init() {
        gson = new Gson();
        mMessageListener = new MMessageListener(context);
        mChatManagerListener = new ChatManagerListenerImpl();
        initialiseConnection();
        transportCommands = new TransportCommands();
        trMucs = new ArrayList<TransportMuc>();
    }

    private void initialiseConnection() {

        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration
                .builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setServiceName(serverAddress);
        //set this for working locally
        config.setHost(hostAddress);
        //config.setHost(serverAddress);
        config.setPort(5222);
        config.setDebuggerEnabled(true);
        config.setResource("yupanagps");
        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        connection = new XMPPTCPConnection(config.build());
        XMPPConnectionListener connectionListener = new XMPPConnectionListener();
        connection.addConnectionListener(connectionListener);
    }

    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.disconnect();
            }
        }).start();
    }

    public void connect(final String caller) {
        mCaller = caller;
        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected synchronized Boolean doInBackground(Void... arg0) {
                if (connection.isConnected())
                    return false;
                isconnecting = true;
                if (isToasted)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(context,caller + "=>connecting....",Toast.LENGTH_LONG).show();
                        }
                    });
                Log.d("Connect() Function", caller + "=>connecting....");

                try {
                    connection.connect();
                    DeliveryReceiptManager dm = DeliveryReceiptManager
                            .getInstanceFor(connection);
                    dm.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
                    dm.addReceiptReceivedListener(new ReceiptReceivedListener() {

                        @Override
                        public void onReceiptReceived(final String fromid,
                                                      final String toid, final String msgid,
                                                      final Stanza packet) {

                        }
                    });
                    connected = true;

                    roster = Roster.getInstanceFor(connection);
                    entries = roster.getEntries();
                    for (RosterEntry entry : entries) {
                        Log.i("MyXMPP roster", entry.getUser());
                    }
                    roster.addRosterListener(new myRosterListener());
                    getMucRooms();


                } catch (IOException e) {
                    if (isToasted)
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {

                                        Toast.makeText(
                                                context,
                                                "(" + caller + ")"
                                                        + "IOException: ",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                    Log.e("(" + caller + ")", "IOException: " + e.getMessage());
                } catch (SmackException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(context,
                                    "(" + caller + ")" + "SMACKException: ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("(" + caller + ")",
                            "SMACKException: " + e.getMessage());
                } catch (XMPPException e) {
                    if (isToasted)

                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {

                                        Toast.makeText(
                                                context,
                                                "(" + caller + ")"
                                                        + "XMPPException: ",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    Log.e("connect(" + caller + ")",
                            "XMPPException: " + e.getMessage());

                }
                return isconnecting = false;
            }
        };
        connectionThread.execute();
    }

    public void getMucRooms() {
            if (connected && !mGotMucs) {
                String str = "[" + loginUser + "]:<getmucs></getmucs>";
                sendBotMessage(str);
            }
    }

    public void mucLogin() {
        mucManager = MultiUserChatManager.getInstanceFor(connection);
        for (String muci : mucRooms) {
            addTrMuc(muci);
        }
    }

    public void addTrMuc(String muc) {
        boolean found = false;
        TransportMuc trMuc;
        for (TransportMuc trmuci : trMucs) {
            if (trmuci.mMucJid.equals(muc))  {
                found = true;
                int index = trMucs.indexOf(trmuci);
                trMuc = mucRejoin(muc);
                trMucs.set(index,trMuc);
                break;
            }
        }
        if (!found) {
            trMuc = mucJoin(muc);
            trMucs.add(trMuc);
        }
    }

    public TransportMuc mucJoin(String muc) {
        MultiUserChat mucChat;
        mucChat = mucManager.getMultiUserChat(muc);
        boolean joined = false;
        if (isUserSet()) {
                try {
                    mucChat.join(loginUser, passwordUser);
                    Log.i("MyXMPP muc", "joined muc");
                    mucChat.addMessageListener(new MucMessageListener());
                    joined = true;
                } catch (XMPPException.XMPPErrorException e) {
                    joined = false;
                    e.printStackTrace();
                } catch (SmackException e) {
                    joined = false;
                    e.printStackTrace();
                }
        }
        TransportMuc trMuc = new TransportMuc(muc,joined,mucChat);
        return trMuc;
    }

    public TransportMuc mucRejoin(String muc) {
        MultiUserChat mucChat;
        mucChat = mucManager.getMultiUserChat(muc);
        boolean joined = false;
        if (isUserSet()) {
            try {
                mucChat.join(loginUser, passwordUser);
                Log.i("MyXMPP muc", "joined muc");
                joined = true;
            } catch (XMPPException.XMPPErrorException e) {
                joined = false;
                e.printStackTrace();
            } catch (SmackException e) {
                joined = false;
                e.printStackTrace();
            }
        }
        TransportMuc trMuc = new TransportMuc(muc,joined,mucChat);
        return trMuc;
    }

    public boolean isUserSet() {
        if (loginUser.equals(""))
            return false;
        else
            return true;
    }

    public void login() {
        try {
            connection.login(loginUser, passwordUser);
            Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");
        } catch (XMPPException | SmackException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

    }

    private class ChatManagerListenerImpl implements ChatManagerListener {
        @Override
        public void chatCreated(final org.jivesoftware.smack.chat.Chat chat,
                                final boolean createdLocally) {
            if (!createdLocally)
                chat.addMessageListener(mMessageListener);
        }
    }

    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection connection) {
            Log.d("xmpp", "Connected!");
            connected = true;
            if (!connection.isAuthenticated()) {
                login();
            }
        }

        @Override
        public void connectionClosed() {
            if (isToasted)
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(context, "ConnectionCLosed!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ConnectionCLosed!");
            connected = false;
            chat_created = false;
            loggedin = false;
            mInMucChat = false;
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            if (isToasted)
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "ConnectionClosedOn Error!!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ConnectionClosedOn Error!");
            connected = false;
            chat_created = false;
            loggedin = false;
            mInMucChat = false;
        }

        @Override
        public void reconnectingIn(int arg0) {
            Log.d("xmpp", "Reconnectingin " + arg0);
            loggedin = false;
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            if (isToasted)
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, "ReconnectionFailed!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            Log.d("xmpp", "ReconnectionFailed!");
            connected = false;
            chat_created = false;
            loggedin = false;
            mInMucChat = false;
        }

        @Override
        public void reconnectionSuccessful() {
            if (isToasted)
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, "REConnected!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            Log.d("xmpp", "ReconnectionSuccessful");
            connected = true;
            chat_created = false;
            loggedin = false;
            mInMucChat = false;
        }

        @Override
        public void authenticated(XMPPConnection arg0, boolean arg1) {
            Log.d("xmpp", "Authenticated!");
            loggedin = true;
            ChatManager.getInstanceFor(connection).addChatListener(
                    mChatManagerListener);
            chat_created = false;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }).start();
            if (isToasted)
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(context, "Connected!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            //muc reconnection
            mucLogin();
        }
    }


    ///////////////////////////
    //sending messages
    public void sendCurrentGPSPOS(GPSpos gpspos) {
        if (gpspos == null)
            Toast.makeText(context, "Sending GPSPOS, gps is null!", Toast.LENGTH_SHORT).show();
        else
        {
            //considere removing this
            getMucRooms();
            double lat, lon;
            lat = gpspos.mLat;
            lon = gpspos.mLon;
            Long tsLong = System.currentTimeMillis();
            String ts = tsLong.toString();
            String body = "[" + loginUser + "]:" +
                    "]<gpspos>lat=" + String.valueOf(lat) + ",lon=" + String.valueOf(lon) +
                    ",time=" + ts + "</gpspos>";
            sendBotMessage(body);
        }
        return;
    }

    public void sendBotMessage(String body) {
        if (connected) {
            if (!chat_created_bot) {
                MychatBot = ChatManager.getInstanceFor(connection).createChat(
                        loginUser + "@" + serverAddress, mMessageListener);
                chat_created_bot = true;
            }
            sFromLen = loginUser.length()+3;
            Message message = new Message();
            message.setTo(loginUser);
            message.setFrom(loginUser);
            message.setBody(body.substring(sFromLen));
            message.setType(Message.Type.chat);

            try {
                if (connection.isAuthenticated()) {
                    message.setType(Message.Type.chat);
                    MychatBot.sendMessage(message);
                } else {
                    login();
                }
            } catch (SmackException.NotConnectedException e) {
                Log.e("xmpp.SendMessage()", "msg Not sent!-Not Connected!");
            } catch (Exception e) {
                Log.e("xmpp.SendMessage()-E",
                        "msg Not sent!" + e.getMessage());

            }
        }
        else
        {
            connect(mCaller);
        }
    }

    public void sendMessage(ChatMessage chatMessage) {
        boolean isGroupChat;
        String body = chatMessage.body;
        sFromLen = chatMessage.receiver.length() + 3;
        isGroupChat = isGroupChat(chatMessage.receiver);
        if (!chat_created) {
            Mychat = ChatManager.getInstanceFor(connection).createChat(
                    chatMessage.receiver + "@"
                            + serverAddress,
                    mMessageListener);
            chat_created = true;
        }
        Message message = new Message();
        message.setTo(chatMessage.receiver);
        message.setFrom(chatMessage.sender);
        message.setBody(body.substring(sFromLen));
        message.setStanzaId(chatMessage.msgid);
        try {
            if (connection.isAuthenticated()) {
                if (!isGroupChat) {
                    message.setType(Message.Type.chat);
                    Mychat.sendMessage(message);
                }
                else  {
                    message.setType(Message.Type.groupchat);
                    MultiUserChat mucChat = getGroupChat(chatMessage.receiver);
                    mucChat.sendMessage(message);
                }
            } else {
                login();
            }
        } catch (SmackException.NotConnectedException e) {
            Log.e("xmpp.SendMessage()", "msg Not sent!-Not Connected!");
        } catch (Exception e) {
            Log.e("xmpp.SendMessage()-E",
                    "msg Not sent!" + e.getMessage());
        }
    }

    ///////////////////////////
    //getting messages

    private class MucMessageListener implements MessageListener {
        @Override
        public void processMessage(Message message) {
            Log.i("MyXMPP muc", "got muc message" + message.toString());
            boolean isCommand = transportCommands.isTransportCommand(message.getBody());

            if (message.getType() == Message.Type.groupchat
                    && message.getBody() != null && isCommand) {
                if (transportCommands.isGpsCommand(message.getBody())) {
                    //we got a command the only command now are a set of gps pos of the form
                    //&lt;transportCommand&gt;bot1@yupanalabs.com/Smack,57.0489078,9.9274613,1469633649692&amp
                    Log.i("MyXMPP muc", "got possitions" + message.toString());
                    ArrayList<GPSpos> recent = transportCommands.getGPSposFromMessage(message.getBody().toString());
                    mergeGPSpos(recent);
                }
            }

            if (message.getType() == Message.Type.groupchat
                    && message.getBody() != null && !isCommand) {
                String sFrom = message.getFrom();
                String[] separated = sFrom.split("@");
                String[] nickFrom = separated[1].split("/");
                sFrom = "[" + separated[0] + "." + nickFrom[1]+ "]";
                final String bodyWithSender =  sFrom + ":" + message.getBody();
                boolean isEchoed = loginUser.equals(nickFrom[1]);

                if (!isEchoed) {
                    ChatMessage chatMessage =
                            new ChatMessage(message.getFrom(), message.getTo(),
                                    bodyWithSender, message.getStanzaId(), false);
                    processMucMessage(chatMessage);
                }
            }
        }
        private void processMucMessage(final ChatMessage chatMessage) {
            chatMessage.isMine = false;

            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    Chats.chatlist.add(chatMessage);
                    Chats.chatAdapter.notifyDataSetChanged();
                }
            });
        }
    }



    private class MMessageListener implements ChatMessageListener {
        public MMessageListener(Context contxt) {
        }
        @Override
        public void processMessage(final org.jivesoftware.smack.chat.Chat chat,
                                   final Message message) {
            Log.i("MyXMPP_MESSAGE_LISTENER", "Xmpp message received: '"
                    + message);
            boolean isCommand = transportCommands.isTransportCommand(message.getBody());
            if (message.getType() == Message.Type.chat
                    && message.getBody() != null && !isCommand) {
                String sFrom = message.getFrom();
                String[] separated = sFrom.split("@");
                sFrom = "[" + separated[0] + "]";
                final String bodyWithSender =  sFrom + ":" + message.getBody();
                ChatMessage chatMessage =
                        new ChatMessage(message.getFrom(),message.getTo(),
                                bodyWithSender,message.getStanzaId(),false);
                processMessage(chatMessage);
            }
            if (message.getType() == Message.Type.chat
                    && message.getBody() != null && isCommand
                    && transportCommands.isMucsCommand(message.getBody()) ) {
                mucRooms = transportCommands.getMucsFromMessage(message.getBody());
                mGotMucs = true;
                for (String stri : mucRooms ) {
                    Log.i("MyXMPP_MESSAGE_LISTENER", "got mucs: "+stri);
                }
                //TODO: join mucs
                mucLogin();
            }
        }
        private void processMessage(final ChatMessage chatMessage) {
            chatMessage.isMine = false;
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    Chats.chatlist.add(chatMessage);
                    Chats.chatAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private class myRosterListener implements RosterListener {
        public void entriesAdded(Collection<String> addresses) {
        }
        public void entriesDeleted(Collection<String> addresses) {
        }
        public void entriesUpdated(Collection<String> addresses) {
        }
        public void presenceChanged(Presence presence) {
            //System.out.println("Presence changed: " + presence.getFrom() + " " + presence);
            String msg =  "Xmpp presenceChanged: '" + presence.getFrom();
            Log.i("MyXMPP_MESSAGE_LISTENER", msg);
        }
    }

    //auxiliary functions
    private void mergeGPSpos(ArrayList<GPSpos> recent) {
        if (mGPSposArray == null) {
            mGPSposArray = recent;
            return;
        }
        if (recent.isEmpty())
            return;
        else {
            for (GPSpos gpsi : recent) {
                updateGPSpos(gpsi);
            }
        }
    }

    private void updateGPSpos(GPSpos gpsj) {
        String jid = gpsj.mJidNick;
        boolean found = false;
        for (GPSpos gpsi: mGPSposArray)
        {
            if (gpsi.mJidNick.equals(jid)) {
                found = true;
                int index = mGPSposArray.indexOf(gpsi);
                mGPSposArray.set(index,gpsj);
                break;
            }
        }
        if (!found) {
            mGPSposArray.add(gpsj);
        }
    }

    private boolean isGroupChat(String muc) {
        boolean found = false;
        muc = muc + "@" + mucService;
        for (TransportMuc trmuci : trMucs) {
            if (trmuci.mMucJid.equals(muc))  {
                found = true;
            }
        }
        return found;
    }

    private MultiUserChat getGroupChat(String muc) {
        MultiUserChat mucChat;
        muc = muc + "@" + mucService;
        for (TransportMuc trmuci : trMucs) {
            if (trmuci.mMucJid.equals(muc))  {
                mucChat = trmuci.mMucChat;
                return  mucChat;
            }
        }
        return null;
    }

}