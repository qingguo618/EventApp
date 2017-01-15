package com.ptechpeople.beeker.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by pchinta on 13/12/15.
 */
public class EventNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "EventNotifReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");

//        Toast.makeText(context, "Service", Toast.LENGTH_SHORT).show();

        Intent i = new Intent(context, EventNotificationService.class);

        context.startService(i);
    }
}
