package devicroft.burnboy.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import devicroft.burnboy.Activities.MainActivity;
import devicroft.burnboy.Activities.MapsActivity;
import devicroft.burnboy.R;
import devicroft.burnboy.Receivers.ActivityTrackingBroadcastReceiver;

/**
 * Created by m on 13-Dec-16.
 */

public class MovementTrackingService extends IntentService {

    private static final String LOG_TAG = "TESTINGGPS";

    private static final int NOTIFICATION_ID = 999;

    public static final int REQUEST_INTENT_STOP = 1;
    public static final int REQUEST_INTENT_PROGRESS = 2;
    public static final int REQUEST_INTENT_RETURNTOMAINACTIVITY = 0;

    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    public static final String ACTION_VIEW_PROGRESS = "ACTION_VIEW_PROGRESS";
    public static final String ACTION_RECEIVE_NEW_LOCATION = "ACTION_RECEIVE_NEW_LOCATION";

    private static LocationManager locationManager = null;
    private static final int LOCATION_CHECK_DELAY = 5000;
    private static final float LOCATION_DISTANCE = 15f;

    private static NotificationManager notificationManager = null;
    private BroadcastReceiver trackingReceiver;

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
        Log.d(LOG_TAG, "SERVICE onCreate");
        //method saves the instance variable of locationmanager
        //then checks if we have permissions and requests location updates, using our locationListener, through the manager
        initialiseLocationManager();
        initialiseBroadcastReceiver();

    }

    private void initialiseBroadcastReceiver() {
        IntentFilter trackingFilter = new IntentFilter();
        trackingFilter.addAction(ACTION_RECEIVE_NEW_LOCATION);
        trackingReceiver = new ActivityTrackingBroadcastReceiver();
        this.registerReceiver(trackingReceiver, trackingFilter);
    }

    @Override
    public void onStart(Intent intent, int startId) {

        Log.d(LOG_TAG, "SERVICE onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "SERVICE onDestroy");

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

        if (trackingReceiver != null) {
            unregisterReceiver(trackingReceiver);
            trackingReceiver = null;
        }
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "SERVICE onBind");

        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "SERVICE onHandleIntent");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "SERVICE onStartCommand: " + "flags: " + flags + "startId: " + startId);

        //https://stackoverflow.com/questions/4805269/programmatically-register-a-broadcast-receiver?rq=1
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            Log.d(LOG_TAG,"called to cancel service");
            notificationManager.cancel(NOTIFICATION_ID);
            stopSelf();
        }

        initialiseNotification();

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "SERVICE onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "SERVICE onRebind");
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

    private Notification.Builder initialiseNotification(){
        Bitmap largeNotificationIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_nobg);

        //TODO GET LOCATION FIGURES IN INTERVALS THEN STORE THEM
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.icon_run)
                .setLargeIcon(largeNotificationIcon)
                .setContentTitle(getString(R.string.notif_logging_movement))
                .setContentText(getString(R.string.notif_logging_text))
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                //.setAutoCancel(true)            //cancels when pressed
                .setAutoCancel(false)            //remains in notif bar when  pressed
                .setWhen(System.currentTimeMillis())
                .setUsesChronometer(true);  //stopwatch counts time from when starts, displays next to notif title

        Intent returnToAppIntent = new Intent(getApplicationContext(), MainActivity.class);
        returnToAppIntent.setAction(Intent.ACTION_MAIN);
        returnToAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent= PendingIntent.getActivity(getApplicationContext(),
                REQUEST_INTENT_RETURNTOMAINACTIVITY,
                returnToAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(pendingIntent);

        Notification.Action stopTrackingAction = new Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.icon_run_cancel) , //https://developer.android.com/reference/android/graphics/drawable/Icon.html
                getString(R.string.notif_action_stop_text),
                createStopIntent())
                .build();

        Notification.Action viewProgressTrackingAction = new Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.icon_run_cancel) ,
                getString(R.string.notif_action_view_progress),
                createViewProgressIntent())
                .build();


        notificationBuilder.addAction(stopTrackingAction);

        //TODO build support for viewProgress, extra functionality
        // notificationBuilder.addAction(viewProgressTrackingAction);



        Log.d(LOG_TAG, "Notification built with intent and pending intent");
        notificationManager.notify( NOTIFICATION_ID , notificationBuilder.build());
        return notificationBuilder;
    }

    private PendingIntent createStopIntent(){

        //stop service from notification functionality
        Intent stopIntent = new Intent(this, MainActivity.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);

        PendingIntent stopTrackingPendingIntent = PendingIntent.getActivity(
                getApplicationContext(),        //context
                REQUEST_INTENT_STOP,                              //request code
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return stopTrackingPendingIntent;
    }

    private PendingIntent createViewProgressIntent(){

        Intent progressIntent = new Intent(this, MapsActivity.class);
        progressIntent.setAction(ACTION_VIEW_PROGRESS);
        progressIntent.putExtra("source", "trackingService");

        //TODO take locations fetched (or store in db, and send id across) to populate map     progressIntent.putExtra()
        //used latest as placeholder
        //need to feed through id of the activity we are currently working on
        progressIntent.putExtra("currentActivityBeingTracked", "latest");

        PendingIntent viewProgressPendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                REQUEST_INTENT_PROGRESS,
                progressIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return viewProgressPendingIntent;
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
            Log.d(LOG_TAG, "StatusChanged: " + s + i + "LastLoc: " + lastLocation);

            // likely where we we latlng
            //TODO go through this to get latlng for movement
            Intent intent = new Intent();
            intent.putExtra(ACTION_RECEIVE_NEW_LOCATION, bundle);
            sendBroadcast(intent);


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

    public static String getName(){
        return "MovementTrackingService";
    }




}
