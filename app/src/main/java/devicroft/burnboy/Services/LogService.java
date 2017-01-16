package devicroft.burnboy.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import devicroft.burnboy.Activities.MainActivity;
import devicroft.burnboy.Data.LogContentHelper;
import devicroft.burnboy.Models.MovementLog;
import devicroft.burnboy.Models.MovementMarker;
import devicroft.burnboy.R;
import devicroft.burnboy.Receivers.NotificationCancelReceiver;

/**
 * Created by m on 16-Jan-17.
 */

public class LogService extends Service {
    private static final String TAG = "SERVICE";
    private LocationManager locationManager = null;
    private NotificationManager notificationManager = null;
    private static final int LOCATION_INTERVAL = 200; //TODO change before sub
    private static final float LOCATION_DISTANCE = 10f;
    private static final int LISTENER_NETWORK_INDEX = 0;
    private static final int LISTENER_GPS_INDEX = 1;

    private MovementLog currentMovementLog;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
            Log.d(TAG, "onBind");
        return new LogBinder();
    }

    public class LogBinder extends Binder implements IInterface{
        @Override
        public IBinder asBinder() {
            Log.d("LOG_BINDER", "asBinder");
            return this;
        }
    }
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        initialiseLocationManager();    //sets location manager
        setupLocationManager();         //set up location listener and managers to request updates
        initialiseNotification();       //start notification display
        currentMovementLog = new MovementLog(Calendar.getInstance().getTimeInMillis()); //create log object with start time
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, " DESTROYED");
        if(locationManager != null){
            try {
                locationManager.removeUpdates(locationListeners[0]);
                locationManager.removeUpdates(locationListeners[1]);
            } catch(SecurityException e){
                Log.i(TAG, "Need elevated permissions for location");
            } catch(Exception e) {
                Log.i(TAG, "fail to remove location listners, ignore", e);
            }
        }


        //to save currentLog. markers aren't ordered - too annoying
        Log.d(TAG, "saving log " + currentMovementLog.getFormattedStartDate() + " to DB");
        currentMovementLog.setEndTime(new Date(Calendar.getInstance().getTimeInMillis()));
        ArrayList<MovementMarker> allMarkers = locationListeners[LISTENER_GPS_INDEX].movementMarkers;
        for (int i = 0; i < locationListeners[LISTENER_NETWORK_INDEX].movementMarkers.size(); i++) {
            allMarkers.add(locationListeners[LISTENER_NETWORK_INDEX].movementMarkers.get(i));
        }
        currentMovementLog.setMarkers(allMarkers);
        currentMovementLog.setSavedInDatabase(true);

        new LogContentHelper(this).addLog(currentMovementLog);

        //TODO remember to sort markers when querying in sql

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        //to cancel notification
        Intent i = new Intent();
        intent.setAction("devicroft.BurnBoy.CANCEL_NOTIFY");
        intent.putExtra("id", NotificationCancelReceiver.NOTIFICATION_ID);
        sendBroadcast(intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
            Log.d(TAG, "onRebind");
        super.onRebind(intent);
    }

    ///////////////////////////////////////////////////////     https://stackoverflow.com/questions/28535703/best-way-to-get-user-gps-location-in-background-in-android
    private class LocationListener implements android.location.LocationListener{
        public ArrayList<MovementMarker> movementMarkers = new ArrayList<>();       //each provider fills up their own list of markers
        public LocationListener(String provider) {
            Log.i(TAG, "Constructor LocationListener " + provider);
            Location l = new Location(provider);
            //movementMarkers.add(locationToMarker(l));
        }
        @Override
        public void onProviderDisabled(String s) {
            Log.d(TAG, "onProviderDisabled");
        }
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocChanged");
            MovementMarker m = locationToMarker(location);
            Log.d(TAG, m.getTitle() + ", " + m.getLatlng().toString() + " - " + m.getTime() + " - " + m.getSnippet());
            movementMarkers.add(m);
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "onStatusChanged");
        }
        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "onProviderEnabled");
        }
        private MovementMarker locationToMarker(Location l){
            return new MovementMarker(fetchGeoName(l),
                    String.valueOf(l.getTime()),
                    new LatLng(l.getLatitude(),l.getLongitude()),
                    "Speed: " + l.getSpeed());
        }
    }
    LocationListener[] locationListeners = new LocationListener[]{
            new LocationListener(LocationManager.NETWORK_PROVIDER),
            new LocationListener(LocationManager.GPS_PROVIDER)
    };
    ///////////////////////////////////////////////////////////////////////////////////
    private void initialiseLocationManager() {
        Log.d(TAG, "initialiseLocationManager");
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
    private void setupLocationManager(){
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist, " + ex.getMessage());
        }
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist " + ex.getMessage());
        }
    }
    /////////////////////////////////////notification////////////////////////////////////////
    private void initialiseNotification() {
        Log.d("NOTIFICATION", "initialiseNotification");
        Bitmap largeNotificationIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_nobg);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.icon_run)
                .setLargeIcon(largeNotificationIcon)
                .setContentTitle(getString(R.string.notif_logging_movement))
                .setContentText(getString(R.string.notif_logging_text))
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setAutoCancel(false)            //remains in notif bar when  pressed
                .setWhen(System.currentTimeMillis())
                .setUsesChronometer(true);  //stopwatch counts time from when starts, displays next to notif title

        //general notification press
        Intent returnToAppIntent = new Intent(getApplicationContext(), MainActivity.class);
        returnToAppIntent.setAction(Intent.ACTION_MAIN);
        returnToAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent onGeneralPressIntent= PendingIntent.getActivity(this.getApplicationContext(),
                0,
                returnToAppIntent,
                0);
        notificationBuilder.setContentIntent(onGeneralPressIntent);

        //stop button function
        Intent stopTrackingIntent = new Intent(getApplicationContext(), MainActivity.class);
        returnToAppIntent.setAction(Intent.ACTION_DELETE);
        returnToAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent onStopActionPressIntent= PendingIntent.getActivity(this.getApplicationContext(),
                0,
                stopTrackingIntent,
                0);
        Notification.Action stopTrackingAction = new Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.icon_run_cancel) , //https://developer.android.com/reference/android/graphics/drawable/Icon.html
                getString(R.string.notif_action_stop_text),
                onStopActionPressIntent)
                .build();
        notificationBuilder.addAction(stopTrackingAction);

        //on delete/cancel noti function
        Intent bcIntent = new Intent (this, LogService.class);
        bcIntent.putExtra("id", "cancel");//?
        bcIntent.setAction("devicroft.BurnBoy.CANCEL_NOTIFY");
        PendingIntent deleteIntent = PendingIntent.getActivity(this.getApplicationContext(),
                0,
                bcIntent,
                0);
        notificationBuilder.setDeleteIntent(deleteIntent);

        //TODO notificationBuilder.addAction(viewProgressTrackingAction);

        Log.d("NOTIFICATION", "" + NotificationCancelReceiver.NOTIFICATION_ID + " built");
        notificationManager.notify( NotificationCancelReceiver.NOTIFICATION_ID , notificationBuilder.build());

    }
    //////////////////////////////////////////////////////////////////////////////////////////
    private String fetchGeoName(Location l){
        try {
            String geoName = "";
            Geocoder geo = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
            if (addresses.size() > 0) {
                geoName = "Near " + addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality();
            }
            return geoName;
        }catch(IOException e){Log.e(TAG, "Couldnt get locale geo name from LatLng");}
        catch(NullPointerException e){Log.e(TAG, "Can't get context , ignore");}
        return "fail";
    }

    public static String getName(){
        return "devicroft.BurnBoy.Services.LogService";
    }

}
