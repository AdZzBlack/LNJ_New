package com.inspira.lnj;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import layout.ChatData;
import layout.ChatMsgContainer;

/**
 * Created by shoma on 02/08/17.
 */

public class BackgroundTask extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private LocationManager locationManager;
    private String GPSprovider;
    private String Networkprovider;
    private Location oldLocation;
    private static GlobalVar global;
    private static Double oldLatitude;
    private static Double oldLongitude;
    private static int startState;
    private static int endState;
    private static double trackingRadius;
    private static long trackingInterval;
    private static String TAG;
    private static boolean GpsStopped;
    private static String trackingType;

    //chat
    public static Socket mSocket;
    public static List<ChatData> listChatData = new ArrayList<>();

    @Override
    public void onCreate() {

        global = new GlobalVar(this);
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        TAG = BackgroundTask.class.getSimpleName();
        Log.i("BackgroundTask", "starting background service");
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        //CHAT
        foregroundNotif("BG service", "Chat ON");

        //load data dari sharedpref dlu
        loadOldDataChat();

        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.connect();

        LibInspira.setShared(global.chatPreferences, global.chat.chat_menu_position, "indexInternal");

        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("appStart", onAppStart);
        mSocket.on("loadAllRoom", loadAllRoom);
        mSocket.on("loadData",loadData);
        mSocket.on("new message", onNewMessage);

        mSocket.emit("appStart",LibInspira.getShared(global.userpreferences, global.user.nama, ""));
        // get room pakai id_user
        mSocket.emit("loadAllRoom",LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
        //mSocket.emit("loadData",LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
    }


    // ## CHAT ##
    private Emitter.Listener onAppStart = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            try {
                //Log.d(TAG,data.getString("log")+"");
                data.getString("log");
            } catch (JSONException e) {
                return;
            }
        }
    };

    private Emitter.Listener loadAllRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            try {
                //Log.d(TAG,"load all room "+data.getString("log"));
                data.getString("log");
            } catch (JSONException e) {
                return;
            }
        }
    };

    private Emitter.Listener loadData = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            List<ChatData.roomInfo> listDataRoom = new ArrayList<>();
            List<ChatMsgContainer> listDataPendingChat = new ArrayList<>();
            listChatData = new ArrayList<>();

            try {
                String result = data.getString("dataRoomInfo");
                result = result.replace("\\","");
                Log.d(TAG,result);
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0) {
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        String[] dataStr = new String[obj.length()];

                        dataStr[0] = obj.getString("id_room_info");
                        dataStr[1] = obj.getString("userid");
                        dataStr[2] = obj.getString("roomName");
                        dataStr[3] = obj.getString("type");
                        dataStr[4] = obj.getInt("creator")+"";
                        dataStr[5] = obj.getString("created_date");
                        dataStr[6] = obj.getString("memberInThatRoom");

                        listDataRoom.add(new ChatData.roomInfo(dataStr[0],dataStr[1],dataStr[2],dataStr[3],dataStr[4],dataStr[5],dataStr[6]));
                        // lalu set di class
                    }
                }
            } catch (JSONException e) {
                Log.d(TAG,"err load room");
            }

            try {
                String result = data.getString("dataPendingMsg");
                result = result.replace("\\","");
                Log.d(TAG,result);
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0) {
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        Log.d(TAG,obj.toString());
                        String[] dataStr = new String[obj.length()];

                        dataStr[0] = obj.getInt("id")+"";
                        dataStr[1] = obj.getInt("message_type")+"";
                        dataStr[2] = obj.getString("message_data");
                        dataStr[3] = obj.getInt("message_data_type")+"";
                        dataStr[4] = obj.getInt("id_room_info")+"";
                        dataStr[5] = obj.getInt("status")+"";
                        dataStr[6] = obj.getInt("from_id")+"";
                        dataStr[7] = obj.getString("from_nama");
                        dataStr[8] = obj.getString("sendTime");

                        // lalu set di class
                        listDataPendingChat.add(new ChatMsgContainer(dataStr[0],dataStr[1],dataStr[2],dataStr[3],
                                dataStr[4],dataStr[5],dataStr[6],dataStr[7],dataStr[8]));
                    }
                }
                else
                {
                    Log.d(TAG,"no pending msg");
                }
            } catch (JSONException e) {
                Log.d(TAG,"err load pending msg");
            }

            //create dan update room tanpa message
            if(listDataRoom.size() > 0)
            {
//                Log.d("runnn","listDataRoom.size() "+listChatData.);
                if(listChatData.size() > 0)
                {
                    //replace data lama
                    for(int i=0;i<listDataRoom.size();i++) {
                        boolean flag = false;
                        for(ChatData.roomInfo temp : listDataRoom) {

                            if (listChatData.get(i).getMroomInfo().getIdRoom().equals(temp.getIdRoom())  && listChatData.get(i).getMroomInfo().getType().equals(temp.getType())) {
                                flag = true;
                                listChatData.get(i).replaceRoomInfo(temp);
                                break; // untuk percepat looping aja
                            }
                        }
                        if(!flag)
                        {
                            // karena tidak ketemu id yang sama, berarti data baru
                            listChatData.add(new ChatData(listDataRoom.get(i)));
                        }
                    }
                }
                else
                {
                    for(ChatData.roomInfo temp : listDataRoom) {
                        listChatData.add(new ChatData(temp));
                    }
                }
            }



            // ## old code sebelum save chat
            // kepake buat update data
            //Log.d(qwe,"pending chat.size() "+listDataPendingChat.size());
            if(listDataPendingChat.size() > 0) {
                if (listChatData.size() > 0) {
                    // replace - karena sdh ada data
                    //Log.d(qwe,"replace msg");
                    for (int i = 0; i < listChatData.size(); i++) {
                        for (ChatData.roomInfo temp : listDataRoom) {
                            if (listChatData.get(i).getMroomInfo().getIdRoom().equals(temp.getIdRoom())) {
                                listChatData.get(i).replaceAllData(temp, listDataPendingChat);
                                break; // percepat looping aja
                            }
                            //klo room id dr
                        }
                    }
                } else {
                    //Log.d(qwe,"add msg karna kosong");
                    // langusng add - karena data msh kosong
                    for (ChatData.roomInfo temp : listDataRoom) {
                        listChatData.add(new ChatData(temp, listDataPendingChat));
                    }
                    // Log.d("msglala", listChatData.size() + "");
                }
            }

            //Log.d(qwe,"save di loadall");
            saveChatData(listChatData);

            if(listDataPendingChat.size() > 0)
            {
                for(ChatMsgContainer newMsg : listDataPendingChat)
                {
                    if(!newMsg.getFrom_id().equals(LibInspira.getShared(global.userpreferences, global.user.nomor, "")))
                    {
                        String status = "";
                        if (newMsg.getStatus().equals(ChatMsgContainer.statusSend)) {
                            status = ChatMsgContainer.statusDelivered;
                        } else {
                            status = newMsg.getStatus();
                        }
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("id", newMsg.getId());
                            jsonObject.put("id_room_info", newMsg.getIdRoom());
                            jsonObject.put("from_id", newMsg.getFrom_id());
                            jsonObject.put("from_nama", newMsg.getFrom_nama());
                            jsonObject.put("message_type", newMsg.getType());
                            jsonObject.put("message_data_type", newMsg.getMsgType());
                            jsonObject.put("status", status);
                            jsonObject.put("message_data", newMsg.getMessage());
                            jsonObject.put("sendTime", newMsg.getSendTime());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mSocket.emit("new message", jsonObject.toString());
                    }
                }
            }
        }
    };


    private Boolean isConnected = true;
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            new Thread (new Runnable() {
                @Override
                public void run() {
                    if(!isConnected) {
                        isConnected = true;
                        Log.d(TAG,"emit onConnect");
                        // get room pakai id_user
                        mSocket.emit("loadAllRoom",LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
                    }
                }
            }).start();
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "diconnected");
                    isConnected = false;
                    //LibInspira.ShowShortToast(con,"disconnect");
                    Log.d(TAG,"emit disconnect");
                }
            });
            thread.start();
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //Log.e(TAG, "Error connecting");
                    //LibInspira.ShowShortToast(getApplicationContext(),"ERR on connect");
                    Log.d(TAG,"emit con err "+args[0].toString());
                }
            });
            thread.start();
        }
    };


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("onnewmsg","indexmsg");
                    JSONObject obj = (JSONObject) args[0];
                    Log.d(TAG,"new msg : "+obj.toString());
                    String[] dataStr = new String[obj.length()];
//                    String username;
//                    String message;
                    try {
                        dataStr[0] = obj.getString("id");
                        dataStr[1] = obj.getInt("message_type")+"";
                        dataStr[2] = obj.getString("message_data");
                        dataStr[3] = obj.getInt("message_data_type")+"";
                        dataStr[4] = obj.getInt("id_room_info")+"";
                        dataStr[5] = obj.getInt("status")+"";
                        dataStr[6] = obj.getInt("from_id")+"";
                        dataStr[7] = obj.getString("from_nama");
                        dataStr[8] = obj.getString("sendTime");

                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    String prevId = "";
                    if(dataStr[0].contains("~")) {
                        String[] id_piece = dataStr[0].trim().split("\\~");
                        if (id_piece.length > 0) {
                            dataStr[0] = id_piece[0];
                            prevId = id_piece[1];
                        }
                    }

                    ChatMsgContainer newMsg = new ChatMsgContainer(
                            dataStr[0],dataStr[1],dataStr[2],dataStr[3],
                            dataStr[4],dataStr[5],dataStr[6],dataStr[7],
                            dataStr[8]
                    );

                    //addMessage(newMsg);
                    // cek misal from dr diri sendiri ga ush di update
                    if(newMsg.getFrom_id().equals(LibInspira.getShared(global.userpreferences, global.user.nomor, "")))
                    {
                        replaceMessage(newMsg,prevId);
//                        if(newMsg.getStatus().equals(ChatMsgContainer.statusSend)) {
//                            //status send dan from yourself
//                            // tinggal replace message
//                            replaceMessage(newMsg,prevId);
//                        }
//                        else
//                        {
//                            replaceMessage(newMsg,prevId);
//                        }
                    }
                    else if(!newMsg.getFrom_id().equals(LibInspira.getShared(global.userpreferences, global.user.nomor, "")))
                    {
                        if(newMsg.getStatus().equals(ChatMsgContainer.statusSend)) {
                            //updatenya ketika terima ack dr server
                            //newMsg.setStatus(ChatMsgContainer.statusDelivered);
                            //replaceMessage(newMsg,prevId);

                            //send dan from other
                            //update jadi deliver
                            replaceMessage(newMsg,prevId);
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("id", newMsg.getId());
                                jsonObject.put("id_room_info", newMsg.getIdRoom());
                                jsonObject.put("from_id", newMsg.getFrom_id());
                                jsonObject.put("from_nama", newMsg.getFrom_nama());
                                jsonObject.put("message_type", newMsg.getType());
                                jsonObject.put("message_data_type", newMsg.getMsgType());
                                jsonObject.put("status", ChatMsgContainer.statusDelivered);
                                jsonObject.put("message_data", newMsg.getMessage());
                                jsonObject.put("sendTime", newMsg.getSendTime());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mSocket.emit("new message", jsonObject.toString());
                        }
                        else if(newMsg.getStatus().equals(ChatMsgContainer.statusDelivered))
                        {
                            // sdh deliv tapi blm diread
                            replaceMessage(newMsg,prevId);
                            String mToUserId = LibInspira.getShared(global.chatPreferences, global.chat.chat_to_id, "");
                            if(!mToUserId.equals(newMsg.getFrom_id()))
                            {
                                showNotif(newMsg.getFrom_nama(),newMsg.getMessage());
                            }

                            // jika fragment chat di buka asumsi langusng read
                            Log.d("logasd",LibInspira.getShared(GlobalVar.chatPreferences, GlobalVar.chat.chat_menu_position, ""));
                            if(LibInspira.getShared(GlobalVar.chatPreferences, GlobalVar.chat.chat_menu_position, "").equals("chatFrag"))
                            {
                                int tempPost = 0;
                                boolean flagRoom = false;
                                for(int i=0;i<listChatData.size();i++)
                                {
                                    if(listChatData.get(i).getMroomInfo().getIdRoom().equals(newMsg.getIdRoom()))
                                    {
                                        tempPost = i;
                                        flagRoom = true;
                                        break;
                                    }
                                }

                                if(flagRoom) {
                                    updateStatusToRead(listChatData.get(tempPost));
                                }
                            }
                        }
                        else if(newMsg.getStatus().equals(ChatMsgContainer.statusRead))
                        {
                            replaceMessage(newMsg,prevId);
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("id", newMsg.getId());
                                jsonObject.put("id_room_info", newMsg.getIdRoom());
                                jsonObject.put("from_id", newMsg.getFrom_id());
                                jsonObject.put("from_nama", newMsg.getFrom_nama());
                                jsonObject.put("message_type", newMsg.getType());
                                jsonObject.put("message_data_type", newMsg.getMsgType());
                                jsonObject.put("status", ChatMsgContainer.statusReadDelivered);
                                jsonObject.put("message_data", newMsg.getMessage());
                                jsonObject.put("sendTime", newMsg.getSendTime());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mSocket.emit("new message", jsonObject.toString());
                        }
                        else
                        {
                            replaceMessage(newMsg,prevId);
                        }

                    }

                }
            });
            thread.start();
        }
    };

    private Emitter.Listener onACK = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            Log.d(TAG,"onACK");
        }
    };


    public static void replaceMessage(ChatMsgContainer newMsgData, String prevId)
    {
        // yg masuk sini sdh data bersih sdh di pisah id nya antara id db dengan id generate android
        // search di list berdasar prev id
        Log.d(TAG, "normal id "+newMsgData.getId() +" previd "+ prevId);
        int tempPost = 0;
        boolean flagRoom = false;
        for(int i=0;i<listChatData.size();i++)
        {
            if(listChatData.get(i).getMroomInfo().getIdRoom().equals(newMsgData.getIdRoom()))
            {
                tempPost = i;
                flagRoom = true;
                break;
            }
        }

        if(flagRoom) {
            int flag = 0;
            for (int i = 0; i < listChatData.get(tempPost).getChatMsgData().size(); i++) {
                if (!prevId.equals("")) {
                    if (listChatData.get(tempPost).getChatMsgData().get(i).getId().equals(prevId)) {

//                        if(listChatData.get(tempPost).getChatMsgData().get(i).getMsgType().equals(ChatMsgContainer.message_data_type_picture)
//                                && ChatMsgContainer.isYou(listChatData.get(tempPost).getChatMsgData().get(i)))
//                        {
//                            listChatData.get(tempPost).getChatMsgData().get(i).copy(newMsgData,ChatMsgContainer.message_data_type_picture);
//                        }
//                        else
//                        {
//                            listChatData.get(tempPost).getChatMsgData().get(i).copy(newMsgData);
//                        }
                        listChatData.get(tempPost).getChatMsgData().get(i).copy(newMsgData);
                        flag = 1;
                        Log.d(TAG, "by previd replace id " + newMsgData.getId());
                        break;
                    }
                } else {
                    if (listChatData.get(tempPost).getChatMsgData().get(i).getId().equals(newMsgData.getId())) {

//                        if(listChatData.get(tempPost).getChatMsgData().get(i).getMsgType().equals(ChatMsgContainer.message_data_type_picture)
//                                && ChatMsgContainer.isYou(listChatData.get(tempPost).getChatMsgData().get(i)))
//                        {
//                            listChatData.get(tempPost).getChatMsgData().get(i).copy(newMsgData,ChatMsgContainer.message_data_type_picture);
//                        }
//                        else
//                        {
//                            listChatData.get(tempPost).getChatMsgData().get(i).copy(newMsgData);
//                        }
                        listChatData.get(tempPost).getChatMsgData().get(i).copy(newMsgData);
                        flag = 1;
                        Log.d(TAG, "by id replace id " + newMsgData.getId());
                        break;
                    }
                }
            }
            if (flag == 0) {
                listChatData.get(tempPost).getChatMsgData().add(newMsgData);
                Log.d(TAG, "add id " + newMsgData.getId());
            }
        }
        //Log.d("msglala","4 size "+listChatData.get(tempPost).getChatMsgData().size()+"");
        //chatFrag.setAdapter(listChatData.get(tempPost));
        saveChatData(listChatData);
    }

    public static void updateStatusToRead(ChatData _mChatData)
    {
        // saran : mungkin nanti biar lbh efisien di search dari bawah ke atas
        // krna asumsi yang atas2 sdh di read dari pada loop lg
        if(_mChatData != null) {
            for (ChatMsgContainer newMsg : _mChatData.getChatMsgData()) {
                if (newMsg.getType().equals(ChatMsgContainer.typeMSG)
                        && newMsg.getStatus().equals(ChatMsgContainer.statusDelivered)
                        && !newMsg.getFrom_id().equals(LibInspira.getShared(global.userpreferences, global.user.nomor, ""))) {
                    // klo other, ketika chat fragment di buka, update jd read, di asumsikan user sdh baca messagenya
                    JSONObject jsonObject = new JSONObject();
                    String status = ChatMsgContainer.statusRead;
                    try {
                        jsonObject.put("id", newMsg.getId());
                        jsonObject.put("id_room_info", newMsg.getIdRoom());
                        jsonObject.put("from_id", newMsg.getFrom_id());
                        jsonObject.put("from_nama", newMsg.getFrom_nama());
                        jsonObject.put("message_type", newMsg.getType());
                        jsonObject.put("message_data_type", newMsg.getMsgType());
                        jsonObject.put("status", status);
                        jsonObject.put("message_data", newMsg.getMessage());
                        jsonObject.put("sendTime", newMsg.getSendTime());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("new message", jsonObject.toString());
                    //updateStatusOnServer(temp.getId(),ChatMsgContainer.statusRead);
                }
            }
        }
    }

    private void loadOldDataChat()
    {
        List<ChatData> oldData = new ArrayList<>();
        String tempData = LibInspira.getShared(GlobalVar.chatPreferences,GlobalVar.chat.chat_history_all,"");
        Type listType = new TypeToken<ArrayList<ChatData>>(){}.getType();
        oldData= new Gson().fromJson(tempData, listType);

        if(oldData != null && oldData.size() > 0)
        {
            //Log.d(qwe,"ada data");
            listChatData.clear();
            listChatData.addAll(oldData);
        }
        else
        {
            //Log.d(qwe,"no data");
        }

    }

    public static void saveChatData(List<ChatData> newData)
    {
        if(newData.size() > 0) {
            String data = new Gson().toJson(newData);
            LibInspira.setShared(GlobalVar.chatPreferences, GlobalVar.chat.chat_history_all, data);
            Log.d("save", "save data");
            //Log.d("picass","save data");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //onTaskRemoved(intent);

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        return super.onStartCommand(intent, flags, startId);
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Date currentDate = new Date();
            currentDate.getTime();
            String[] currentTime = currentDate.toString().substring(11, 16).split(":");
            String currentTimeValue = currentTime[0] + currentTime[1];

            if (LibInspira.getShared(global.userpreferences, global.user.nomor, "") == "")
                stopSelf(msg.arg1);
            //stopSelf(msg.arg1); <- don't use, ur gonna kill this
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("appStart", onAppStart);
        mSocket.off("loadAllRoom", loadAllRoom);
        mSocket.off("new message", onNewMessage);
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // AlarmManager for background service
        Intent service = new Intent(getApplicationContext(), trackerBroadcastReciver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, 88088, service, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 5000, pIntent);
        Log.v(TAG, "Task Removed, restarting service");
    }


    private void foregroundNotif(String title, String text) {
        int notifID = 1;
        PendingIntent notifIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(BackgroundTask.this, BackgroundTask.class), 0);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(text)
                .setTicker(" ToDoList Notification")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(notifIntent)
                .setSmallIcon(R.drawable.lnj_logo).build();
        startForeground(notifID, notification);
    }

    private void showNotif(String title, String message)
    {
        Intent intent;
        intent = new Intent(getApplicationContext() ,Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("LNJ : "+title);
        notificationBuilder.setContentText(message);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSmallIcon(R.mipmap.lnj_logo);
        notificationBuilder.setContentIntent(pendingIntent);
        //notificationBuilder.setDeleteIntent(createOnDismissedIntent(this,0)); //0 = notificationID;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
