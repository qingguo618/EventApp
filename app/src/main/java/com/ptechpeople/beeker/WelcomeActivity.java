package com.ptechpeople.beeker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.ptechpeople.beeker.notification.EventNotificationReceiver;
import com.ptechpeople.beeker.notification.EventNotificationService;
import com.ptechpeople.beeker.until.GPSTracker;

public class WelcomeActivity extends Activity {
    private static final String TAG = "WelcomeActivity";
//    private ProgressDialog progressDialog;

    private int mInterval = 1000; // 1 seconds by default, can be changed later
//    private Handler mHandler;
    Button btnSeek, btnInfo;
    AlertDialog alertDialog;
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ImageLoaderConfiguration defaultConfiguration
                = new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(defaultConfiguration);

        gps = new GPSTracker(this);

        btnSeek = (Button)findViewById(R.id.btnSeek);
        btnInfo = (Button)findViewById(R.id.btnInfo);

        btnSeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyBluetooth() && verifyLocation()) {
                    if (EventApp.beaconsString == null || EventApp.beaconsString.equals("null")) {
                        EventApp.beaconsString = "";
                    }

                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                }
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog = new AlertDialog.Builder(WelcomeActivity.this).
                        setMessage("Use BEEKER to get exclusive offers, deals, promotions and informati" +
                                "on from your favorite stores. You can ONLY receive messages or \"BEEKS\" from locations with BEEKER Beacon installed and when you are within 100 meters of that location.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();

                alertDialog.show();

//                Runnable hideDialog = new Runnable() {
//                    @Override
//                    public void run() {
//                        alertDialog.dismiss();
//                    }
//                };


            }
        });


        schedulePeriodicFetch();
//        mHandler = new Handler();
    }

//    Runnable mStatusChecker = new Runnable() {
//        @Override
//        public void run() {
//            try {
//                if(EventApp.beaconsString != null && !EventApp.beaconsString.equals("null")){
//                    stopRepeatingTask();
//                    progressDialog.dismiss();
//                    btnSeek.setEnabled(true);
//                }
//            } finally {
//                // 100% guarantee that this always happens, even if
//                // your update method throws an exception
//                mHandler.postDelayed(mStatusChecker, mInterval);
//            }
//        }
//    };

//    void startRepeatingTask() {
//        mStatusChecker.run();
//    }

//    void stopRepeatingTask() {
//        mHandler.removeCallbacks(mStatusChecker);
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(EventApp.beaconsString != null && !EventApp.beaconsString.equals("null")){
//            btnSeek.setEnabled(true);
//        }else{
//            progressDialog = ProgressDialog.show(this, "", "Finding Beacons...", true);
//            btnSeek.setEnabled(false);
//            startRepeatingTask();
//        }
//    }

    private boolean verifyLocation() {

        if (gps.canGetLocation()) {

            EventApp.lat = gps.getLatitude();
            EventApp.lng = gps.getLongitude();

            Log.e("debug", "latitude:" + EventApp.lat + ", longitude:" + EventApp.lng );
            return true;
        }

        gps.showSettingsAlert();

        return false;
    }

    private boolean verifyBluetooth() {
        Log.e(TAG, "@@@@@@@@@I am in Bijesh I am in verifyBluetooth");
        try {
            if (!EventApp.beaconManager.isBluetoothEnabled()) {
                Log.e(TAG,"@@@@@@@@@I am in Bijesh I am in verifyBluetooth in if");
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please turn ON bluetooth to get events.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.e(TAG, "@@@@@@@@@I am in Bijesh I am in verifyBluetooth setOnDismissListener");
                        /*finish();
                        System.exit(0);*/
                    }
                });
                builder.show();
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (RuntimeException e) {
            Log.e(TAG,"@@@@@@@@@I am in Bijesh I am in verifyBluetooth RuntimeException");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }
            });
            builder.show();
            return false;
        }
    }

    // Setup a recurring alarm every half hour
    public void schedulePeriodicFetch() {
        Log.i(TAG, "schedulePeriodicFetch");
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), EventNotificationReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, EventNotificationService.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000,
                30 * 1000, pIntent);

//        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
//        intent.putExtra(ONE_TIME, Boolean.FALSE);
//        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
//        //After after 5 seconds
//        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5 , pi);
    }
}
