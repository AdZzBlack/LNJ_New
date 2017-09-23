package com.inspira.gms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by shoma on 7/28/17.
 */

public class trackerBroadcastReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent background = new Intent(context, GMSbackgroundTask.class);
        background.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(background);
        Log.d(trackerBroadcastReciver.class.getSimpleName(), "SERVICE RESTARTS!");
    }
}
