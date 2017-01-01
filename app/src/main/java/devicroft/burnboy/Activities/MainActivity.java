package devicroft.burnboy.Activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import devicroft.burnboy.Data.DbQueries;
import devicroft.burnboy.Data.LogDBHelper;
import devicroft.burnboy.Data.MovementLogContentProvider;
import devicroft.burnboy.Data.MovementLogProviderContract;
import devicroft.burnboy.MovementLog;
import devicroft.burnboy.R;


public class MainActivity extends AppCompatActivity {

    LogDBHelper dbHelper;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_map:
                //start map view activity
                Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(mapIntent);
                break;
            case R.id.action_licenses:
                doLicenseDialog();
                break;
            case R.id.action_history:
                Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(historyIntent);
                break;
            case R.id.action_delete_all:
                dispatchDeleteAllSnackbar();
                break;
            default:

        }
        return false;
    }



    private void doLicenseDialog() {
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Licenses")
                .setMessage(Html.fromHtml(
                        getString(R.string.license_fab) +
                                getString(R.string.app_name)))    //https://stackoverflow.com/questions/3235131/set-textview-text-from-html-formatted-string-resource-in-xml
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        TextView textView = (TextView) d.findViewById(android.R.id.message);
        textView.setScroller(new Scroller(this));
        textView.setVerticalScrollBarEnabled(true);
        textView.setAllCaps(false);
        textView.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseAd();
        setupFAB();

        dbHelper = new LogDBHelper(this);

        dbTest();


    }

    private void setupFAB() {
        //https://github.com/Clans/FloatingActionButton
        FloatingActionButton startButton = (FloatingActionButton) findViewById(R.id.fab_start_logging);
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.fab_add_activity);
        FloatingActionButton weightButton = (FloatingActionButton) findViewById(R.id.fab_log_weight);

        startButton.setOnClickListener(startLoggingFabClickListener);
        addButton.setOnClickListener(addLogFabClickListener);
        weightButton.setOnClickListener(weightLogFabClickListener);

    }

    private View.OnClickListener startLoggingFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivityLogging();
            //dispatchToast(R.string.start_fitnesslogging);

        }
    };

    private View.OnClickListener weightLogFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO initialise weight logging intent to activity
            dispatchToast("implement weight log activity, with graph");
        }
    };

    private View.OnClickListener addLogFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO add activity for manually inputting point locations, time, medium (bike, run etc)
            int count = getLogCount();
            testAddLog(new MovementLog());
            dispatchToast(Integer.toString(getLogCount() - count)  + " added");
        }
    };


    private void dispatchToast(int stringID){
        Toast.makeText(this, getString(stringID), Toast.LENGTH_SHORT).show();
    }
    private void dispatchToast(String string){
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    private void dispatchDeleteAllSnackbar() {
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.activity_main),  getString(R.string.delete_all_message), Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar sb = Snackbar.make( findViewById(R.id.activity_main), getString(R.string.delete_all_undo_message), Snackbar.LENGTH_SHORT);
                        sb.show();
                    }
                });

        //callback so i will actually delete when the snackbar is dismissed
        snackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                //TODO deleteAllLogs();
                dbHelper.deleteAll();
            }
            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
            }
        });
        snackbar.show();
    }

    private boolean startActivityLogging(){
        //TODO initialise tracking functionality

        return false;
    }



    private boolean initialiseAd(){
        //load up an ad
        Log.d("main", "Starting ad" + getString(R.string.app_ad_id));
        //MobileAds.initialize(getApplicationContext(), getString(R.string.app_ad_id));
        AdView adView = (AdView) findViewById(R.id.main_banner_ad_view);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)    //TODO take off testDevice before release
                .build();
        adView.loadAd(adRequest);
        return true;

    }


    /*
            TEST METHODS
     */

    private void dbTest(){
        MovementLog m = new MovementLog();
        dbHelper.insertNewLog(m);
        dbHelper.insertNewLog(new MovementLog());
        dbHelper.insertNewLog(new MovementLog());

        //testAddLog(new MovementLog());
        dispatchToast("Log count: " + Integer.toString(getLogCount()) + "Marker count: " + Integer.toString(getMarkerCount()));
        dbHelper.delete(m.getStartTime().getTime());
        dispatchToast("Log count: " + Integer.toString(getLogCount()) + "Marker count: " + Integer.toString(getMarkerCount()));
    }


    //END TEST METHODS

    /*
    *       CONTENT PROVIDER METHODS METHOODSSS
     */

    private void testAddLog(MovementLog log){
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
            values.put(MovementLogProviderContract.MKR_LAT, log.getMarkers().get(i).getLatlng().latitude);
            values.put(MovementLogProviderContract.MKR_LNG, log.getMarkers().get(i).getLatlng().longitude);
            values.put(MovementLogProviderContract.MKR_SNIPPET, log.getMarkers().get(i).getSnippet());
            values.put(MovementLogProviderContract.MKR_TITLE, log.getMarkers().get(i).getTitle());
            values.put(MovementLogProviderContract.MKR_FK_MOVEMENT_ID, getIdOfLastInserted());
            cr.insert(MovementLogProviderContract.MARKER_URI, values);
        }
    }

    private int getIdOfLastInserted(){
        Cursor c = getContentResolver().query(
                MovementLogProviderContract.MOVEMENT_URI,  //content uri of table
                new String[] {DbQueries.SELECT_MOST_RECENT_MOVEMENTLOG_ID},  //to return for each row
                null,           //selection clause
                null,           //selection args
                null);          //sort order
        //go to the entry with the count integer
        c.moveToFirst();
        //get the integer from the ID column
        return c.getColumnIndex(LogDBHelper.COL_ID_MOVE);
    }

    private void deleteLog(int id){
        //delete individual movement log
        getContentResolver().delete(
                ContentUris.withAppendedId(MovementLogProviderContract.MOVEMENT_URI, id),
                null,   //selection clause
                null    //selection args (after WHERE ...)
        );
    }

    private void deleteAllLogs(){
        getContentResolver().delete(
                MovementLogProviderContract.MOVEMENT_URI,
                null,   //selection clause
                null    //selection args (after WHERE ...)
        );
    }

    private int getLogCount(){
        // for now it just displays the nubmer of rows in the db
        //getting the count and returning a cursor with 1 entry that has the count integer
        String[] ids = new String[]{
                MovementLogProviderContract.MOV_ID
        };
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(
                MovementLogProviderContract.MOVEMENT_URI,  //content uri of table
                ids,  //to return for each row
                null,           //selection clause
                null,           //selection args
                null);          //sort order
        return c.getCount();
    }

    private int getMarkerCount(){
        // for now it just displays the nubmer of rows in the db
        //getting the count and returning a cursor with 1 entry that has the count integer
        Cursor c = getContentResolver().query(
                MovementLogProviderContract.MARKER_URI,  //content uri of table
                new String[] {"count(*) AS count"},  //to return for each row
                null,           //selection clause
                null,           //selection args
                null);          //sort order
        //go to the entry with the count integer
        c.moveToFirst();
        //display it
        return c.getInt(0);
    }


    //END CONTENT PROVIDER METHODS

    /*
    *       dbhelper methods
     */

    private int helperMarkerCount(){
        return dbHelper.getLogCount();
    }

    private void helperMarkerAdd(MovementLog m){
        dbHelper.insertNewLog(m);

    }

    private void helperMarkerDelete(MovementLog m){
        dbHelper.delete(m.getStartTime().getTime());
    }

    private void helperMarkerDeleteAll(){
        dbHelper.deleteAll();
    }


    //END DBHELPER METHODS

    }
