package devicroft.burnboy.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by m on 31-Dec-16.
 */

public class MovementLogContentProvider extends ContentProvider {

    private static final int MOV_ALL = 111;
    private static final int MOV_FIND = 222;
    private static final int MKR_ALL = 333;
    private static final int MKR_FIND = 444;

    private LogDBHelper dbHelper = null;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        //declare URI that can be matched to the table we want
        uriMatcher.addURI(MovementLogProviderContract.AUTHORITY, LogDBHelper.TABLENAME_MOVEMENT, MOV_ALL);
        uriMatcher.addURI(MovementLogProviderContract.AUTHORITY, LogDBHelper.TABLENAME_MARKER, MKR_ALL);
        //when we append a number ot the end of the base uri, we will be able to search for the individual recipe item
        uriMatcher.addURI(MovementLogProviderContract.AUTHORITY, LogDBHelper.TABLENAME_MOVEMENT + "/#", MOV_FIND);
        uriMatcher.addURI(MovementLogProviderContract.AUTHORITY, LogDBHelper.TABLENAME_MARKER + "/#", MKR_FIND);
    }


    /*

                OVERRIDE METHODS

     */


    @Override
    public boolean onCreate() {
        Log.d("Provider", "Created");
        this.dbHelper =  new LogDBHelper(this.getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d("Provider", uri.toString() + " " + uriMatcher.match(uri));
        //get db to query
        SQLiteDatabase db  = dbHelper.getWritableDatabase();

        //determine the scope to query
        switch(uriMatcher.match(uri)) {
            case MKR_FIND:
                //update marker selection to the narrowed scope we wanted of an individual
                selection = LogDBHelper.COL_ID_MARKER + " = " + uri.getLastPathSegment();
            case MKR_ALL:
                //return the cursor that points to what we want, note 'selection' is potentially modified from * to 1
                return db.query(dbHelper.TABLENAME_MARKER, projection, selection, selectionArgs, null, null, sortOrder);

            //REMEMBER, MUST BE IN THIS ORDER - FIND, then moves on to all after seleciton has been refined from the fine statement, then returns.
            case MOV_FIND:
                selection = LogDBHelper.COL_ID_MOVE + " = " + uri.getLastPathSegment();
            case MOV_ALL:
                return db.query(dbHelper.TABLENAME_MOVEMENT, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        if(uri.getLastPathSegment() == null) {
            //returns cursor for db of the whole table
            return MovementLogProviderContract.CONTENT_TYPE_MULTIPLE;
        }else {
            //returns individual cursor of match
            return MovementLogProviderContract.CONTENT_TYPE_SINGLE;
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        //get db
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName = null;

        switch(uriMatcher.match(uri)){
            case 111:
            case 222:
                tableName = LogDBHelper.TABLENAME_MOVEMENT;
                break;
            case 333:
            case 444:
                tableName = LogDBHelper.TABLENAME_MARKER;
                break;
            default:
                Log.e("provider", "NO URIMATCH WHEN INSERTING, REEVALUATE LIFE");
        }
        //add switch if other tables are added
        long id = db.insert(tableName, null, contentValues);
        db.close();

        //just for logging, to see if and where the content was appended
        Uri nu = ContentUris.withAppendedId(uri, id);
        Log.d("provider", nu.toString() + "added");

        return nu;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        Log.d("provider", "deleting" + uri.toString());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(uri.toString(), where, whereArgs);
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new UnsupportedOperationException("not implemented");
        //return 0;
    }

    //END OVERRIDE
}
