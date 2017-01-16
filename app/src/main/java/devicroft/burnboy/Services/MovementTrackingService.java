package devicroft.burnboy.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import devicroft.burnboy.Activities.MainActivity;
import devicroft.burnboy.Activities.MapsActivity;
import devicroft.burnboy.Data.DbHelper;
import devicroft.burnboy.Data.MovementLogProviderContract;
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

    //passes at the start of creating service, to add a log we'll update as it goes
    public static final int NEW_LOG_CREATED = 0;
    //when we have a new location, time etc, we'll add this to the log
    public static final int NEW_MARKER_CAPTURED = 1;



    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    public static final String ACTION_VIEW_PROGRESS = "ACTION_VIEW_PROGRESS";
    public static final String ACTION_RECEIVE_NEW_LOCATION = "ACTION_RECEIVE_NEW_LOCATION";

    private static LocationManager locationManager = null;
    private static final int LOCATION_CHECK_DELAY = 5000;
    private static final float LOCATION_DISTANCE = 15f;

    private static NotificationManager notificationManager = null;
    private BroadcastReceiver trackingReceiver;
    private static int services_log_id = 0;

    LocationListener[] locationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER),
            new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };

    Messenger messenger;

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

        messenger = new Messenger(new LocationLoggingHandler());


    }

    private class LocationLoggingHandler extends Handler {
        public LocationLoggingHandler() {
            super();
            Log.d(LOG_TAG, "Handler created");
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case NEW_LOG_CREATED:

                    break;
                case NEW_MARKER_CAPTURED:


                    break;

                default:
                    Log.d(LOG_TAG, "default message passed");
            }
        }
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

        final LocationLoggingHandler handler = new LocationLoggingHandler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
//                setupNewLogForDbId();
                Toast.makeText(getApplicationContext(), "servicing", Toast.LENGTH_SHORT);
            }
        };
        handler.postDelayed(r, LOCATION_CHECK_DELAY);

        initialiseNotification();

        return START_STICKY;
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

        //TODO save end time in database

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

        return messenger.getBinder();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "SERVICE onHandleIntent");
    }



    private int setupNewLogForDbId(){
        //here the
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        long currentTime = System.currentTimeMillis();
        values.put(MovementLogProviderContract.MOV_START_TIME, currentTime);
        cr.insert(MovementLogProviderContract.MOVEMENT_URI, values);

        Log.d("cr", currentTime + " log added");

        Log.d(LOG_TAG, "getIdOfLastInserted");
        Cursor c = getContentResolver().query(
                MovementLogProviderContract.MOVEMENT_URI,  //content uri of table
                new String[] {MovementLogProviderContract.MOV_ID},  //to return for each row
                null,           //selection clause
                null,           //selection args
                DbHelper.COL_ID_MOVE + " DESC limit 1");          //sort order
        //go to the entry with the count integer
        c.moveToFirst();
        if(c.getCount() > 0){
            services_log_id = c.getInt(0);
        }
        //get the integer from the ID column  NOT getColumnIndex(DbHelper.COL_ID_MOVE), that returns its position in table - meta - not value inside
        return services_log_id;

    }

    private void addPositionForLog(Location location){
        //here we take the location fetched by location listener and add it to db
        //use services_log_id to create fk in marker table
        ContentResolver cr = this.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MovementLogProviderContract.MKR_TITLE, location.getProvider());      //TODO change getProvider() to something more meaningful
        values.put(MovementLogProviderContract.MKR_LAT, location.getLatitude());
        values.put(MovementLogProviderContract.MKR_LNG, location.getLongitude());
        values.put(MovementLogProviderContract.MKR_SNIPPET, "Altitude: " + String.valueOf(location.getAltitude())); //TODO change to meaningful snippet, MAYBE. i like altitude there, though refactoring would need to be done
        values.put(MovementLogProviderContract.MKR_TIME, location.getTime());
        values.put(MovementLogProviderContract.MKR_FK_MOVEMENT_ID, services_log_id);
        cr.insert(MovementLogProviderContract.MARKER_URI, values);
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
                .setAutoCancel(true)            //cancels when pressed
                //.setAutoCancel(false)            //remains in notif bar when  pressed
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


            addPositionForLog(lastLocation);


            /*
            // likely where we we latlng
            //TODO go through this to get latlng for movement
            Intent intent = new Intent();
            intent.putExtra(ACTION_RECEIVE_NEW_LOCATION, bundle);
            sendBroadcast(intent);
            */

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
