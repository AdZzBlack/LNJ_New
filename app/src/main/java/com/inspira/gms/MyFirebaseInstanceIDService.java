package com.inspira.gms;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by shoma on 7/26/17.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String recent_token = FirebaseInstanceId.getInstance().getToken();

        Log.d("REG_TOKEN3", recent_token);
        GlobalVar global= new GlobalVar(this);
        LibInspira.setShared(global.userpreferences, global.user.token,recent_token);

//        SharedPreferences sharedPreferences = getApplicationContext().
//                getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
//        SharedPreferences.Editor  editor = sharedPreferences.edit();
//        editor.putString(getString(R.string.FCM_TOKEN), recent_token);
//        editor.commit();
    }

}
