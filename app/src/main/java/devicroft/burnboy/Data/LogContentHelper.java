package devicroft.burnboy.Data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import devicroft.burnboy.Models.MovementLog;
import devicroft.burnboy.Models.MovementMarker;

/**
 * Created by m on 16-Jan-17.
 */

public class LogContentHelper {
    Context context = null;
    private static final String LOG_TAG = "LogContentHelper";

    public LogContentHelper(Context c) {
        super();
        setContext(c);
    }



    private Context getContext() {
        return context;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    public void addLog(MovementLog log){
        Log.d(LOG_TAG,"addLog" + log.getFormattedStartDate());
        //setup inserting putting in movement values
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MovementLogProviderContract.MOV_START_TIME, log.getStartTime().getTime());
        values.put(MovementLogProviderContract.MOV_END_TIME, log.getEndTime().getTime());
        Log.d(LOG_TAG, log.getStartTime().getTime() + " log added");
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
    private int getIdOfLastInserted(){
        Log.d(LOG_TAG,"getIdOfLastInserted");
        Cursor c = context.getContentResolver().query(
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

    public int deleteLog(int id){
        Log.d(LOG_TAG,"deleteLog");
        //delete individual movement log
        int success = context.getContentResolver().delete(
                MovementLogProviderContract.MOVEMENT_URI,   //set uri
                DbQueries.ID_EQUALS_PLACEHOLDER,   //selection clause to find the id
                new String[]{"" + String.valueOf(id)}    //selection args (after WHERE ...)
        );
        return success;
    }
    /*
            delete all logs clears all rows from the movement table
            marker table has movement id as FK, so it will cascade delete itself (many markers to a log)
            we fetch a content resolver, define the table uri, and since were deleting all, no arguments are needed
            log and show user we did something in the background via toast
     */
    public void deleteAllLogs(){
        Log.d(LOG_TAG,"deleteAllLogs");
        context.getContentResolver().delete(
                MovementLogProviderContract.ALL_URI,       //deletes all rows in all tables
                null,   //selection clause
                null    //selection args (after WHERE ...)
        );

        /*      if other tables (other than movement and marker) are added, uncomment.

        getContentResolver().delete(
                MovementLogProviderContract.MOVEMENT_URI,       //just calling delete on movement deletes all, using CASCADE
                null,   //selection clause
                null    //selection args (after WHERE ...)
        );
        //probably only need this whilst testing, cascade should always work - though in dev orphan markers may be made
        getContentResolver().delete(MovementLogProviderContract.MARKER_URI,null,null);    //uri, selection clause, selection args (after WHERE ...)
        */

        Log.d("cr", "All logs deleted from db");
    }

    //https://www.xaprb.com/blog/2006/12/07/how-to-select-the-firstleastmax-row-per-group-in-sql/
    //
    public ArrayList<MovementMarker> getFirstMarkerForEachLog() {
        Log.d(LOG_TAG, "getFirstMarkerForEachLog");
        final int INDEX_ID = 0;
        final int INDEX_LAT = 1;
        final int INDEX_LNG = 2;
        final int INDEX_SNIP = 3;
        final int INDEX_TIME = 4;
        final int INDEX_TITLE = 5;
        String[] markerData = new String[]{MovementLogProviderContract.MKR_ID_MARKER,
                MovementLogProviderContract.MKR_LAT,
                MovementLogProviderContract.MKR_LNG,
                MovementLogProviderContract.MKR_SNIPPET,
                MovementLogProviderContract.MKR_TIME,
                MovementLogProviderContract.MKR_TITLE};
        //setup inserting putting in movement values
        //TODO cant figure out how to do it this android way, will just add them all
        Cursor c = context.getContentResolver().query(
                MovementLogProviderContract.MARKER_URI,  //content uri of table
                markerData,  //to return for each row
                null,           //selection clause
                null,           //selection args
                null);
        c.moveToFirst();
        ArrayList<MovementMarker> movementMarkers = new ArrayList<>();
        for (int i = 0; i < c.getCount(); i++) {
            MovementMarker m = new MovementMarker(c.getString(INDEX_TITLE),
                    c.getString(INDEX_TIME),
                    new LatLng(c.getDouble(INDEX_LAT),c.getDouble(INDEX_LNG)),
                    "Speed: " + c.getString(INDEX_SNIP));
            c.moveToNext();
            movementMarkers.add(m);
        }


        //TODO cycle through cursor to creat all markers

        return movementMarkers;
    }


}
