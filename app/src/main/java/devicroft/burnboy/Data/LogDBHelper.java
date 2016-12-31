package devicroft.burnboy.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;

import devicroft.burnboy.MovementLog;


/**
 * Created by m on 31-Dec-16.
 */

public class LogDBHelper extends SQLiteOpenHelper {

    //to avoid confusion with actual android activities, MOVEMENT is used instead

    private static final String DATABASE_NAME = "burnBoyDb";
    private static final int dbVersionNumber = 1;
    private static final String TABLENAME_MOVEMENT = "MOVEMENT";
    private static final String TABLENAME_MARKER = "LATLNG";

    //MOVEMENT TABLE
    private static final String COL_ID_MOVE = "id";
    private static final String COL_STARTTIME = "start_date";
    private static final String COL_ENDTIME = "end_date";
    //LATLNG TABLE
    private static final String COL_ID_MARKER = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_SNIPPET = "snippet";
    private static final String COL_LAT = "latitude";
    private static final String COL_LNG = "longitude";
    private static final String COL_TIME = "time";
    private static final String COL_FK_MOVEMENT_ID = "fk_movement_id";

    public static final String PRAGMA_FOREIGNKEYS_ON = "PRAGMA foreign_keys=ON";
    private static final String CREATE_MOVEMENT_LOG_TABLE =
            "CREATE TABLE " +  TABLENAME_MOVEMENT + " ( " +
                    COL_ID_MOVE + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    COL_STARTTIME + " INTEGER, " +
                    COL_ENDTIME + " INTEGER" +
                    ");";
    private static final String CREATE_MARKER_TABLE =
            "CREATE TABLE " +  TABLENAME_MARKER + " ( " +
                    COL_ID_MARKER + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    COL_TITLE + " TEXT NOT NULL, " +
                    COL_SNIPPET + " TEXT NOT NULL, " +
                    COL_LAT + " DOUBLE NOT NULL, " +
                    COL_LNG + " DOUBLE NOT NULL, " +
                    COL_TIME + " INTEGER NOT NULL, " +
                    COL_FK_MOVEMENT_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + COL_FK_MOVEMENT_ID + ") REFERENCES " + TABLENAME_MOVEMENT + "(" + COL_ID_MOVE + ")" +
                    ");";
    private static final String QUERY_MARKERS_WHERE_ID =
            "SELECT * FROM " + TABLENAME_MARKER +
                    " WHERE " + COL_ID_MARKER + " = ";

    private static final String QUERY_DELETE_ALL_MARKERS =
            "DELETE FROM " + TABLENAME_MARKER;

    private static final String QUERY_DELETE_ALL_LOGS =
            "DELETE FROM " + TABLENAME_MOVEMENT;

    private static final String QUERY_SELECT_ALL_LOGS =
            "SELECT * FROM " + TABLENAME_MOVEMENT;

    private static final String QUERY_SELECT_ALL_MARKERS =
            "SELECT * FROM " + TABLENAME_MARKER;

    public LogDBHelper(Context context){
        super(context, DATABASE_NAME, null, dbVersionNumber);
        Log.d("db",  "db created");
    }

    public LogDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    public LogDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /*
     *
     *      DB USAGE METHODS
     *              pre-made .execSql  queries to easily insert, update or delete entries in the database
    */


    public void insertNewLog(MovementLog log){
        //create a new log, therefore new id, to db
        //done either when the tracking is finished, or when the service is finished abruptly
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STARTTIME, log.getStartTime().getTime());
        values.put(COL_ENDTIME, log.getEndTime().getTime());
        db.insert(TABLENAME_MOVEMENT, null, values);
        insertMarkers(db, log.getMarkers());


    }

    private void insertMarkers(SQLiteDatabase db, ArrayList<Marker> m){
        for(int i = 0; i < m.size(); i++){
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, m.get(i).getTitle());
            values.put(COL_SNIPPET, m.get(i).getSnippet());
            values.put(COL_LAT, m.get(i).getPosition().latitude);
            values.put(COL_LNG, m.get(i).getPosition().longitude);
            values.put(COL_TIME, 0);        //TODO implement time keeping functionality for each marker. perhaps putting date in tag
            db.insert(TABLENAME_MARKER, null, values);
        }
    }

    private int titleIndex = 1;
    private  int snippetIndex = 2;
    private  int latIndex = 3;
    private int lngIndex = 4;
    private  int timeIndex = 5;

    public ArrayList<String[]> fetchMarkers(int id){
        //find id of log event in db then return all of that logs marker data in marker objects back
        ArrayList<String[]> logMarkers = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(QUERY_MARKERS_WHERE_ID + id, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            logMarkers.add(cursorToMarkerData(cursor));
            cursor.moveToNext();
        }

        return logMarkers;
    }

    private String[] cursorToMarkerData(Cursor cursor){
        MarkerOptions m = new MarkerOptions().visible(false);
        String[] s = {cursor.getString(titleIndex),
                    cursor.getString(snippetIndex),
                    cursor.getString(latIndex),
            cursor.getString(lngIndex),
            cursor.getString(timeIndex)};

            return s;
    }

    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(QUERY_DELETE_ALL_LOGS);
        db.execSQL(QUERY_DELETE_ALL_MARKERS);
        db.close();
    }

    public int getLogCount(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery(QUERY_SELECT_ALL_LOGS, null);
        int count = c.getCount();
        db.close();
        return count;

    }




    //END DB USAGE METHODS

     /*
     *  OVERRIDE METHODS
    */

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PRAGMA_FOREIGNKEYS_ON);
        db.execSQL(CREATE_MOVEMENT_LOG_TABLE);
        Log.d("db", "movement table created");
        db.execSQL(CREATE_MARKER_TABLE);
        Log.d("db", "latlng table created");

    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.d("db", "opened");
        super.onOpen(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_MARKER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_MOVEMENT);
        onCreate(db);
    }

        @Override
    public String getDatabaseName() {
        return super.getDatabaseName();
    }

    @Override
    public void setWriteAheadLoggingEnabled(boolean enabled) {
        super.setWriteAheadLoggingEnabled(enabled);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        Log.d("db", "got writable");
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        Log.d("db", "got readable");
        return super.getReadableDatabase();
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    //END OVERRIDE METHODS
}
