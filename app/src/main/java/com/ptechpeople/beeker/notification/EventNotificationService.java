package com.ptechpeople.beeker.notification;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ptechpeople.beeker.EventApp;
import com.ptechpeople.beeker.MainActivity;
import com.ptechpeople.beeker.R;
import com.ptechpeople.beeker.until.APIManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by pchinta on 13/12/15.
 */
public class EventNotificationService extends IntentService {
    public static final int REQUEST_CODE = 12345;
    private static int lastRead = -1;
    private static final String TAG = "EventNotifService";

    public EventNotificationService() {
        super("EventNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        /*
             Code written by Birjesh
             */

        if(!EventApp.isFirstNotify) return;
        if(!EventApp.isEnteredRegion) return;

        EventApp.isApiCalling = true;
        Log.i(TAG, "onHandleIntent");
        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            if (sharedPref != null && lastRead == -1 && sharedPref.contains("LastReadItemId")) {
                lastRead = sharedPref.getInt("LastReadItemId", -1);
            }

//            EventApp.beaconsString = "1001554415";

            String api_url = "http://ptechpeople.wpengine.com/wp-json/wp/v2/posts?filter[post_status]=publish&filter[posts_per_page]=10&page=1&filter[category_name]="+EventApp.beaconsString;
            String result = APIManager.getInstance().callGet(getApplicationContext(), api_url, null, true);
            EventApp.isApiCalling = false;
            int unread = 0;
            int lastReadItemId = -1;
            JSONObject lastReadItem = null;
            JSONArray arr_data = new JSONArray(result);

            for (int i = 0; i < arr_data.length(); i++) {
                JSONObject item = arr_data.getJSONObject(i);
                final int itemId = item.getInt("id");
                if (itemId == lastRead) {
                    break;
                } else {
                    if (lastReadItemId < itemId) {
                        lastReadItemId = itemId;
                        lastReadItem = item;
                    }
                    unread++;
                }
            }

            Log.i(TAG, "Found " + unread + " new events");
            if (unread > 0) {
                boolean isForeground = isAppOnForeground(this);
//                boolean isForeground = false;

                if (lastReadItemId != -1 && !isForeground) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.logo)
                                    .setContentTitle(getString(R.string.app_name))
                                    .setAutoCancel(true);
                    if (unread == 1 && lastReadItem != null) {
                        mBuilder.setContentText(lastReadItem.getJSONObject("title").getString("rendered"));
                    } else {
                        mBuilder.setContentText("You have " + unread + " unread BEEKs");
                    }
// Creates an explicit intent for an Activity in your app
                    Intent resultIntent = new Intent(this, MainActivity.class);
                    resultIntent.putExtra(EventApp.BEACON_STRING, EventApp.beaconsString);
//                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                    //Vibration
//                    mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

                    mNotificationManager.notify(1234, mBuilder.build());

                    EventApp.isFirstNotify = false;
                }
                lastRead = lastReadItemId;
                if (sharedPref != null)
                    sharedPref.edit().putInt("LastReadItemId", lastRead).commit();
            }
        } catch (Exception e1) {
            Log.e(TAG, e1.getMessage(), e1);
        }
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
