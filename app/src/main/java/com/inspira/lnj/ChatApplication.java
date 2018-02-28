package com.inspira.lnj;

import android.app.Application;
import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ChatApplication extends Application {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(GlobalVar.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            Log.d("indexInternal","chat app : "+e);
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
