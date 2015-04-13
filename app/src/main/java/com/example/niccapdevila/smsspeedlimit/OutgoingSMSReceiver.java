package com.example.niccapdevila.smsspeedlimit;

/**
 * Created by niccapdevila on 4/7/15.
 */

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by niccapdevila on 4/6/15.
 */
public final class OutgoingSMSReceiver extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String CONTENT_SMS = "content://sms";
    private static final String CONTENT_SMS_OUTBOX = "content://sms/outbox";

    private SMSDatabaseHelper mSMSDatabaseHelper;
    private SMSInfo mSMSInfo;

    private LocationListener mLocationListener;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;


    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 1000; // 1 sec
    private static int FATEST_INTERVAL = 10; // 5 sec
    private static int DISPLACEMENT = 15; // 10 meters

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    @Override
    public void onCreate() {
        MyContentObserver contentObserver = new MyContentObserver();
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        contentResolver.registerContentObserver(Uri.parse(CONTENT_SMS), true, contentObserver);
        mSMSDatabaseHelper = new SMSDatabaseHelper(this);

        mLocationListener = this;
        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.v("Caller History: Service Started.", "OutgoingSMSReceiverService");
        /**
         *   Constant to return from onStartCommand(Intent, int, int): if this service's process is killed while it is started
         *   (after returning from onStartCommand(Intent, int, int)), then leave it in the started state but don't retain this delivered intent.
         *   Later the system will try to re-create the service. Because it is in the started state, it will guarantee to call
         *   onStartCommand(Intent, int, int) after creating the new service instance; if there are not any pending start commands to be
         *   delivered to the service, it will be called with a null intent object, so you must take care to check for this.
         *   This mode makes sense for things that will be explicitly started and stopped to run for arbitrary periods of time, such as a
         *   service performing background music playback.
         */

        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting())
            mGoogleApiClient.connect();

        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    //Called on location speed
    @Override
    public void onLocationChanged(Location location) {

        //Speed comes in meters/sec and needs to be converted to miles/hour
        float speed = location.getSpeed() * 2.236936292054f;

        mSMSInfo.setSpeed(String.format("%.1f", speed));
        mSMSDatabaseHelper.updateSMS(mSMSInfo);

        //Call the right broadcast based on speed
        Intent intent;
        if (speed < 15) {
            intent = new Intent(MainActivity.UPDATE_UNDER15ARRAYLIST);
        }
        else{
            intent = new Intent(MainActivity.UPDATE_OVER15ARRAYLIST);

        }

        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);

        //Once location received, we stop getting updates
        stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("SPEEDLIMITSMS", "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    //Used to listen to SMS messages
    private class MyContentObserver extends ContentObserver {

        public MyContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Uri uriSms = Uri.parse(CONTENT_SMS);


            ///Request certain fields of the SMS message
            ContentResolver cr = getContentResolver();
            String[] reqCols = new String[]{"_id", "type", "address", "date"};
            Cursor cur = cr.query(uriSms, reqCols, null, null, null);
            // this will make it point to the first record, which is the last SMS
            if (!cur.moveToNext()) {
                return; //Just error checking
            }

            int type = cur.getInt(cur.getColumnIndex("type"));
            //Check to see if it's a sent message
            if (type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT) {

                String id = cur.getString(cur.getColumnIndex("_id"));
                String address = cur.getString(cur.getColumnIndex("address"));
                String sDate = cur.getString(cur.getColumnIndex("date"));

                //Add the info the the database
                if (mSMSDatabaseHelper.getSMSInfo(id, sDate, address) == null) {
                    mSMSInfo = new SMSInfo(id, sDate, address);

                    startLocationUpdates();
                    mSMSDatabaseHelper.addSMS(mSMSInfo);

                    //Send a broadcast to main UI activity to update the array list
                    Intent intent = new Intent(MainActivity.UPDATE_UNDER15ARRAYLIST);
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);

                }

            }
        }
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) getApplicationContext(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();

            }
            return false;
        }
        return true;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {

        try {
            if (mGoogleApiClient.isConnected()) {

                Handler handler = new Handler(Looper.getMainLooper());

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
                    }
                });

            }
        } catch (Exception e) {
            Log.i("SpeedLimitSMS", e.toString());
        }

    }

    /**
     * Stopping location updates
     */

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
}

