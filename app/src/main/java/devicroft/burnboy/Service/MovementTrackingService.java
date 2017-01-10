package devicroft.burnboy.Service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import devicroft.burnboy.Activities.MainActivity;
import devicroft.burnboy.R;

/**
 * Created by m on 13-Dec-16.
 */

public class MovementTrackingService extends IntentService {

    private static final String LOG_TAG = "TESTINGGPS";

    private static final int INTENT_RETURN_TAG = 999;

    private static LocationManager locationManager = null;
    private static final int LOCATION_CHECK_DELAY = 5000;
    private static final float LOCATION_DISTANCE = 15f;
    private static NotificationManager notificationManager = null;

    LocationListener[] locationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER),
            new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };

    public MovementTrackingService(String name) {
        super(name);
    }
    public MovementTrackingService() {
        super("MovementTrackingService");
    }

    /*
            MOVE TRACK OVERRIDES
     */

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate service");
        initialiseLocationManager();
        try{
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_CHECK_DELAY, LOCATION_DISTANCE,
                    locationListeners[1]
            );
        }catch(SecurityException e){
            Log.i(LOG_TAG, "Fail to get network location", e);
        }catch(IllegalArgumentException e){
            Log.d(LOG_TAG, "Network provider doesn't exist" + e.getMessage());
        }catch(RuntimeException e){
            Log.d(LOG_TAG, "Runtime error" + e.getMessage(), e);
        }
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_CHECK_DELAY, LOCATION_DISTANCE,
                    locationListeners[0]);
        } catch (java.lang.SecurityException e) {
            Log.i(LOG_TAG, "Fail to get GPS", e);
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "gps provider does not exist " + e.getMessage());
        }

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        super.onDestroy();
        if (locationManager != null) {
            for (int i = 0; i < locationListeners.length; i++) {
                try {
                    locationManager.removeUpdates(locationListeners[i]);
                } catch(SecurityException e){
                    Log.e(LOG_TAG, "need permissions", e);
                } catch (Exception e) {
                    Log.i(LOG_TAG, "fail to remove location listners", e);
                }
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand: " + "flags: " + flags + "startId: " + startId);

        //TODO GET LOCATION FIGURES IN INTERVALS THEN STORE THEM
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.icon_run)
                .setContentTitle(getString(R.string.notif_logging_movement))
                .setContentText(getString(R.string.notif_logging_text))
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());

        Intent returnToAppIntent = new Intent(getApplicationContext(), MainActivity.class);
        returnToAppIntent.setAction(Intent.ACTION_MAIN);
        returnToAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent= PendingIntent.getActivity(getApplicationContext(), 0, returnToAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        Log.d(LOG_TAG, "Notification built with intent and pending intent");

        notificationManager.notify( INTENT_RETURN_TAG , notificationBuilder.build());

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
    //END MOVE TRACK OVERRIDES


    /*
        Cleaner methods
     */

    private void initialiseLocationManager() {
        Log.d(LOG_TAG, "initialiseLocationManager");
        if(locationManager == null){
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

    }


    /*/
    Loc Listener
     */
    private class LocationListener implements android.location.LocationListener{
        Location lastLocation;

        public LocationListener(String provider) {
            Log.d(LOG_TAG, "LocListen created: " + provider);
            lastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(LOG_TAG, "LocChange - Lat:" + location.getLatitude() + " Lng: " + location.getLongitude());
            lastLocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(LOG_TAG, "StatusChanged: " + s + i);

        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(LOG_TAG, "ProviderEnabled: " + s);

        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d(LOG_TAG, "ProviderEnabled: " + s);

        }
    }


}
