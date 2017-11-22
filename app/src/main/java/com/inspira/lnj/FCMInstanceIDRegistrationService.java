package com.inspira.lnj;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by antonnw on 23/06/2016.
 */
public class FCMInstanceIDRegistrationService extends IntentService {

    private String token;

    public FCMInstanceIDRegistrationService(){
        super("FCMInstanceIDRegistrationService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        String recent_token = FirebaseInstanceId.getInstance().getToken();

        Log.d("REG_TOKEN3", recent_token);
        GlobalVar global= new GlobalVar(this);
        LibInspira.setShared(global.userpreferences, global.user.token,recent_token);
    }
}