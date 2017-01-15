package com.ptechpeople.beeker;

import android.app.Application;
import android.os.RemoteException;
import android.util.Log;

import com.aprilbrother.aprilbrothersdk.Beacon;
import com.aprilbrother.aprilbrothersdk.BeaconManager;
import com.aprilbrother.aprilbrothersdk.Region;

import java.util.List;

/*
 * Created by Birjesh 9/1/2016
 * This class is used to get beacon detail
 */
public class EventApp extends Application {
    public  static final String BEACON_STRING = "beaconString";
    public  static final String HEADER_URL_PROD = "http://ptechpeople.wpengine.com";
    public  static final String HEADER_URL_DEV = "http://ptechpeople.staging.wpengine.com";
    public static BeaconManager beaconManager;

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final String TAG = "WI";
    private static final Region ALL_BEACONS_REGION = new Region("com.appcoda.testregion", "FDA50693-A4E2-4FB1-AFCF-C6EB07647825", null, null);

    public static boolean isApiCalling = false;
    public static String beaconsString = "null";
    public static boolean isEnteredRegion = false;
    public static boolean isFirstNotify = true;
    public static double lat = 0;
    public static double lng = 0;
    private String comma = ",";

    public void onCreate() {
        super.onCreate();

        init_beacon();
        connectToService();
    }

    private void init_beacon(){
        beaconManager = new BeaconManager(this);
        beaconManager.setForegroundScanPeriod(3000, 0);
        beaconManager.setRangingListener(beaconRangingListener);
        beaconManager.setMonitoringListener(beaconMonitoringListener);
    }

    private void connectToService() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_BEACONS_REGION);
                } catch (RemoteException e) {

                }
            }
        });
    }

    private BeaconManager.MonitoringListener beaconMonitoringListener = new BeaconManager.MonitoringListener() {
        @Override
        public void onEnteredRegion(Region region, List<Beacon> list) {
            Log.e("beeker", "Entered Region!");
        }

        @Override
        public void onExitedRegion(Region region) {
            Log.e("beeker", "Exited Region!");
        }
    };

    private BeaconManager.RangingListener beaconRangingListener = new BeaconManager.RangingListener() {
        @Override
        public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {

            beaconsString = "";

            for(Beacon beacon:beacons)
            {

                System.out.println("@@@@@Beacon String in loop");
                if(beaconsString.isEmpty()) {
                    beaconsString =""+ beacon.getMajor()+beacon.getMinor();
                }
                else
                {
                    beaconsString =beaconsString+comma+ beacon.getMajor()+beacon.getMinor();
                }
            }
//            Toast.makeText(getBaseContext(), "beacon string is as following: " + beaconsString, Toast.LENGTH_SHORT).show();
        }
    };
}