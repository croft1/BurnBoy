package devicroft.burnboy.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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

    private DbHelper dbHelper = null;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        //declare URI that can be matched to the table we want
        uriMatcher.addURI(MovementLogProviderContract.AUTHORITY, DbHelper.TABLENAME_MOVEMENT + "/#", MOV_FIND);
        uriMatcher.addURI(MovementLogProviderContract.AUTHORITY, DbHelper.TABLENAME_MOVEMENT, MOV_ALL);
        //when we append a number ot the end of the base uri, we will be able to search for the individual recipe item
        uriMatcher.addURI(MovementLogProviderContract.AUTHORITY, DbHelper.TABLENAME_MARKER + "/#", MKR_FIND);
        uriMatcher.addURI(MovementLogProviderContract.AUTHORITY, DbHelper.TABLENAME_MARKER, MKR_ALL);
    }


    /*

                OVERRIDE METHODS

     */


    //create reference to dbhelper instance
    @Override
    public boolean onCreate() {
        Log.d("Provider", "Created");
        this.dbHelper =  new DbHelper(this.getContext());
        return false;
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d("CP", uri.toString() + " " + uriMatcher.match(uri));
        //get db to query
        SQLiteDatabase db  = dbHelper.getWritableDatabase();

        //determine the scope to query
        switch(uriMatcher.match(uri)) {
            case MKR_FIND:
                //update marker selection to the narrowed scope we wanted of an individual
                selection = DbHelper.COL_ID_MARKER + " = " + uri.getLastPathSegment();
            case MKR_ALL:
                //return the cursor that points to what we want, note 'selection' is potentially modified from * to 1
                return db.query(dbHelper.TABLENAME_MARKER, projection, selection, selectionArgs, null, null, sortOrder);

            //REMEMBER, MUST BE IN THIS ORDER - FIND, then moves on to all after seleciton has been refined from the fine statement, then returns.
            case MOV_FIND:
                selection = DbHelper.COL_ID_MOVE + " = " + uri.getLastPathSegment();
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
                tableName = DbHelper.TABLENAME_MOVEMENT;
                break;
            case 333:
            case 444:
                tableName = DbHelper.TABLENAME_MARKER;
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
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //db.beginTransaction();

        db.execSQL("PRAGMA foreign_keys = ON;");    //need this every time we open a db to allow for features like cascade to work

        int count = 0;

        Log.d("db", "deleting from" + uri + where);
        try{
            count = db.delete(DbHelper.TABLENAME_MOVEMENT, where, whereArgs);
        }catch(SQLiteException e){
            Log.e("db", "deletion error");
            e.printStackTrace();
        }catch(CursorIndexOutOfBoundsException exception){
            Log.e("db", "No more to delete");
            return -1;
        }
       // db.endTransaction();
        db.close();

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //db.beginTransaction();

        db.execSQL("PRAGMA foreign_keys = ON;");    //need this every time we open a db to allow for features like cascade to work, just in case

        int updateCount = 0;

        Log.d("db", "updating " + uri + where);
        updateCount = db.update(DbHelper.TABLENAME_MOVEMENT, contentValues, where, whereArgs);
        try{
            db.update(DbHelper.TABLENAME_MARKER, contentValues, where, whereArgs);
        }catch(SQLiteException e){
            Log.e("db", "update error");
            e.printStackTrace();
        }
        // db.endTransaction();
        db.close();

        return updateCount;
    }

    //END OVERRIDE
}
