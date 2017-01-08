package devicroft.burnboy.Activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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

import devicroft.burnboy.Data.DbHelper;
import devicroft.burnboy.Data.DbQueries;
import devicroft.burnboy.Data.MovementLogProviderContract;
import devicroft.burnboy.Models.MovementLog;
import devicroft.burnboy.R;


public class MainActivity extends AppCompatActivity {

    DbHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO uncomment initialiseAd();

        setupFAB();

    }

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
                mapIntent.putExtra("source", "browse");
                startActivity(mapIntent);
                break;
            case R.id.action_licenses:
                doLicenseDialog();
                break;
            case R.id.action_history:
                Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(historyIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.action_delete:
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

    private void setupFAB() {
        //https://github.com/Clans/FloatingActionButton
        FloatingActionButton startButton = (FloatingActionButton) findViewById(R.id.fab_start_logging);
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.fab_add_activity);
        FloatingActionButton weightButton = (FloatingActionButton) findViewById(R.id.fab_log_weight);

        startButton.setOnClickListener(startLoggingFabClickListener);
        addButton.setOnClickListener(addLogFabClickListener);
        weightButton.setOnClickListener(weightLogFabClickListener);
    }

    /*
            CLICK LISTENERS
     */

    //TOP icon on fab list
    private View.OnClickListener addLogFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO add activity for manually inputting point locations, time, medium (bike, run etc)
            int count = getTableCount(MovementLogProviderContract.MOVEMENT_URI);
            addLog(new MovementLog());
            dispatchToast(Integer.toString(getTableCount(MovementLogProviderContract.MOVEMENT_URI) - count)  + " added");
        }
    };

    //MIDDLE icon on fab list
    private View.OnClickListener startLoggingFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivityLogging();
            //dispatchToast(R.string.start_fitnesslogging);
        }
    };

    //BOTTOM icon on fab list
    private View.OnClickListener weightLogFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO initialise weight logging intent to activity
            int id = getIdOfLastInserted();
            deleteLog(id);

            //dispatchToast("implement weight log activity, with graph");
        }
    };

    //when a list item is pressed, it the value inside deleted
    private View.OnLongClickListener listDeleteClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            //get id from object selected and delete

            deleteLog(1);   //TODO add in delete item
            return false;
        }
    };


    //END CLICK LISTENERS

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
                deleteAllLogs();
                //dbHelper.deleteAll();
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
        //initDbh();

        addLog(new MovementLog());

    }


    //END TEST METHODS

    /*
    *       CONTENT PROVIDER METHODS METHOODSSS
     */

    private void addLog(MovementLog log){
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

    private String getCountSummaryString(){
        //query count on movement table to get a [count] array that has an int, instead of returning all values and couting them. faster
        Cursor c = getContentResolver().query(MovementLogProviderContract.MOVEMENT_URI, new String[] {DbQueries.GET_TABLE_COUNT_AS_CURSORINT}, null, null, null);
        c.moveToFirst();
        StringBuilder summary = new StringBuilder(100);
        summary.append("Movement count: ");
        summary.append(Integer.toString(c.getInt(0)));

        //do the same for marker table
        c = getContentResolver().query(MovementLogProviderContract.MARKER_URI,
                new String[] {DbQueries.GET_TABLE_COUNT_AS_CURSORINT},
                null,
                null,
                null);
        c.moveToFirst();
        summary.append(" Marker count: ");
        summary.append(Integer.toString(c.getInt(0)));

        Log.d("cr", summary.toString());
        return summary.toString();
    }

    private int getIdOfLastInserted(){
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

    //for placeholders (?) must have an equal number of new String[] arguments to make it work
    private void deleteLog(int id){
        //delete individual movement log
        int success = getContentResolver().delete(
                MovementLogProviderContract.MOVEMENT_URI,   //set uri
                DbQueries.ID_EQUALS_PLACEHOLDER,   //selection clause to find the id
                new String[]{"" + String.valueOf(id)}    //selection args (after WHERE ...)
            );

    }

    /*
            delete all logs clears all rows from the movement table
            marker table has movement id as FK, so it will cascade delete itself (many markers to a log)
            we fetch a content resolver, define the table uri, and since were deleting all, no arguments are needed
            log and show user we did something in the background via toast
     */
    private void deleteAllLogs(){
        getContentResolver().delete(
                MovementLogProviderContract.MOVEMENT_URI,       //just calling delete on movement deletes all, using CASCADE
                null,   //selection clause
                null    //selection args (after WHERE ...)
        );
        /*
        //probably only need this whilst testing, cascade should always work
        getContentResolver().delete(
                MovementLogProviderContract.MARKER_URI,
                null,   //selection clause
                null    //selection args (after WHERE ...)
        );
        */
        Log.d("cr", "All logs deleted from db");
        dispatchToast("Marker count: " + Integer.toString(getTableCount(MovementLogProviderContract.MARKER_URI)));
    }

    private int getTableCount(Uri tableUriFromContract){
        Cursor c = getContentResolver().query(
                tableUriFromContract,  //content uri of table
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


    private void initDbh(){
        dbHelper = new DbHelper(this);
        MovementLog m = new MovementLog();
        dbHelper.insertNewLog(m);
        dbHelper.insertNewLog(new MovementLog());
        dbHelper.insertNewLog(new MovementLog());
        dispatchToast("Log count: " + Integer.toString(getTableCount(MovementLogProviderContract.MOVEMENT_URI)) +
                "Marker count: " + Integer.toString(getTableCount(MovementLogProviderContract.MARKER_URI)));
        dbHelper.delete(m.getStartTime().getTime());
        dispatchToast("Log count: " + Integer.toString(getTableCount(MovementLogProviderContract.MOVEMENT_URI)) +
                "Marker count: " + Integer.toString(getTableCount(MovementLogProviderContract.MARKER_URI)));
    }

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
    */

    }