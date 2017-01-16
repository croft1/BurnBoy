package devicroft.burnboy.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;

import devicroft.burnboy.Activities.MainActivity;
import devicroft.burnboy.Data.DbHelper;
import devicroft.burnboy.Data.MovementLogProviderContract;
import devicroft.burnboy.Models.MovementLog;
import devicroft.burnboy.Models.MovementMarker;
import devicroft.burnboy.R;
import devicroft.burnboy.Receivers.ActivityTrackingBroadcastReceiver;
import devicroft.burnboy.Receivers.NotificationCancelReceiver;

/**
 * Created by m on 13-Jan-17.
 */
    //VERSION 2 OF MovementTrackingService to start clean
public class LogService extends Service implements LogLocInterface {

    private static final String LOG_TAG = "LOG_SERVICE";
    private static LocationManager locationManager = null;

    private static final int LOCATION_CHECK_DELAY = 500;
    private static final float LOCATION_DISTANCE = 15f;

    private static MovementLog currentLog;

    RemoteCallbackList<LogBinder> remoteCallbackList = new RemoteCallbackList<LogBinder>();


    LocationListener[] locationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };


    public class Logger extends Thread implements Runnable{
        LocationListener listener;
        public Logger(LocationListener listener){
            this.listener = listener;
        }
    }

    @Override
    public void newLocationEvent(Location location) {
        Log.d(LOG_TAG, "newLocationEvent");

    }

    public LogService() {
        super();
    }


    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");

        super.onCreate();
        initialiseLocationManager();
        currentLog = new MovementLog(Calendar.getInstance().getTimeInMillis());
        initialiseNotification();

    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        //Clean up at the end of a service run
        if(!currentLog.isSavedInDatabase()){
            saveCurrent();
        }else{
            updateCurrentInDb();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

    }

    private void updateCurrentInDb() {
        //TODO same as save current, but need to fetch  and update, or add markers to db relating to current
    }

    private void saveCurrent(){
        if(currentLog.getEndTime() == null){
            currentLog.setEndTime(new Date(Calendar.getInstance().getTimeInMillis()));
        }
        if(!currentLog.hasMarker()){

            Location loc = getMostRecentLastLocation();
            MovementMarker marker = new MovementMarker(
                    "Altitude: " + loc.getAltitude(),
                    String.valueOf(loc.getTime()),
                    new LatLng(loc.getLatitude(), loc.getLongitude()),
                    "Provider: " + loc.getProvider()
            );
            currentLog.addNewMarker(marker);
        }

        addLog(currentLog);
        currentLog.set_id(getIdOfLastInserted());
    }

    private Location getMostRecentLastLocation(){
        //juvenile but eh
        Location l;
        l = new Location("NONE");
        l.setLatitude(1);
        l.setLongitude(1);
        l.setAltitude(2);
        l.setTime(0);

        if(locationListeners[0].lastLocation != null){
            l = locationListeners[0].lastLocation;
        }
        if(locationListeners[1].lastLocation != null){
            if(locationListeners[1].lastLocation.getTime() > l.getTime()){
                l = locationListeners[1].lastLocation;
            };
        }

        return l;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return new LogBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "onRebind");
        super.onRebind(intent);
    }

    public class LogBinder extends Binder implements IInterface {

        LogLocInterface callback;

        @Override
        public IBinder asBinder() {
            return this;
        }

        void logLocation(){

        }

        public void registerCallback(LogLocInterface logLoc){
            Log.d(LOG_TAG, "registerCallback");
            this.callback = logLoc;
            remoteCallbackList.register(LogBinder.this);


        }

        public void unregisterCallback(LogLocInterface logLoc){
            Log.d(LOG_TAG, "unregisterCallback");
            remoteCallbackList.unregister(LogBinder.this);
        }


    }

    private void logLocation(){

    }


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



    /////////////////////////////////
    //LocListeneer Class

    private class LocationListener implements android.location.LocationListener{
        Location lastLocation = null;

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


    //when we insert a new log, to put markers in we run this method to find out what the new foreign key is
    private int getIdOfLastInserted(){
        Log.d(LOG_TAG,"getIdOfLastInserted");
        Cursor c = getContentResolver().query(
                MovementLogProviderContract.MOVEMENT_URI,  //content uri of table
                new String[] {MovementLogProviderContract.MOV_ID},  //to return for each row
                null,           //selection clause
                null,           //selection args
                DbHelper.COL_ID_MOVE + " DESC limit 1");          //sort order
        //go to the entry with the count integer
        c.moveToFirst();
        //get the integer from the ID column  NOT getColumnIndex(DbHelper.COL_ID_MOVE), that returns its position in table - meta - not value inside

        return (c.getCount() > 0) ? c.getInt(0) : -1;    //checks if theres a row in the db
    }


    /*
    ONCE ALL COMPLETE, WHETHER DELIBERATELY OR NOT THIS IS RUN TO SAVE THE DATA WE LOGGED
     */
    private void addLog(MovementLog log){

        Log.d(LOG_TAG,"addLog" + log.getFormattedStartDate());
        //setup inserting putting in movement values
        ContentResolver cr = this.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MovementLogProviderContract.MOV_START_TIME, log.getStartTime().getTime());
        values.put(MovementLogProviderContract.MOV_END_TIME, log.getEndTime().getTime());
        Log.d("cr", log.getStartTime().getTime() + " log added");
        cr.insert(MovementLogProviderContract.MOVEMENT_URI, values);
        //setup and insert marker values - use same object but clear firrst
        values.clear();

        for (int i = 0; i < log.getMarkers().size(); i++) {
            values.put(MovementLogProviderContract.MKR_TITLE, log.getMarkers().get(i).getTitle());
            values.put(MovementLogProviderContract.MKR_LAT, log.getMarkers().get(i).getLatlng().latitude);
            values.put(MovementLogProviderContract.MKR_LNG, log.getMarkers().get(i).getLatlng().longitude);
            values.put(MovementLogProviderContract.MKR_SNIPPET, log.getMarkers().get(i).getSnippet());
            values.put(MovementLogProviderContract.MKR_TIME, log.getMarkers().get(i).getTime());
            values.put(MovementLogProviderContract.MKR_FK_MOVEMENT_ID, getIdOfLastInserted());
            cr.insert(MovementLogProviderContract.MARKER_URI, values);
        }


    }

    public static String getName(){
        return "LogService";
    }


    private static NotificationManager notificationManager = null;
    public static final int REQUEST_INTENT_RETURN_MAIN = 1000;
    public static final int REQUEST_INTENT_DELETE = 0;
    private static final int NOTIFICATION_ID = 99;


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

        PendingIntent onPressIntent= PendingIntent.getActivity(this.getApplicationContext(),
                REQUEST_INTENT_RETURN_MAIN,
                returnToAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(onPressIntent);


        Notification.Action stopTrackingAction = new Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.icon_run_cancel) , //https://developer.android.com/reference/android/graphics/drawable/Icon.html
                getString(R.string.notif_action_stop_text),
                createStopIntent())
                .build();

        Intent bcIntent = new Intent (this, NotificationCancelReceiver.class);
        bcIntent.putExtra("cancel", "cancel");
        PendingIntent deleteIntent = PendingIntent.getActivity(this.getApplicationContext(),
                REQUEST_INTENT_DELETE,
                bcIntent,
                0);

        notificationBuilder.addAction(stopTrackingAction);
        notificationBuilder.setDeleteIntent(deleteIntent);

        //TODO build support for viewProgress, extra functionality
        // notificationBuilder.addAction(viewProgressTrackingAction);

        Log.d(LOG_TAG, "Notification built with intent and pending intent");
        notificationManager.notify( NOTIFICATION_ID , notificationBuilder.build());
        return notificationBuilder;
    }

    private void stopNotification() {


    }


    private static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    private static final int REQUEST_INTENT_STOP = 1;


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


}
