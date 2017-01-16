package devicroft.burnboy.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
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

import java.util.List;

import devicroft.burnboy.Data.DbHelper;
import devicroft.burnboy.Data.DbQueries;
import devicroft.burnboy.Data.MovementLogProviderContract;
import devicroft.burnboy.Models.MovementLog;
import devicroft.burnboy.R;
import devicroft.burnboy.Receivers.NotificationCancelReceiver;
import devicroft.burnboy.Services.LogService;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MAIN LOG";

    private Messenger messenger;
    LogService.LogBinder logBinder = null;
    private static boolean  serviceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO uncomment initialiseAd();
        setupFAB();

        IntentFilter filter = new IntentFilter("devicroft.BurnBoy.CANCEL_NOTIFY");
        this.registerReceiver(new NotificationCancelReceiver(), filter);
        if(getIntent().getAction() == Intent.ACTION_DELETE){
            Intent intent=new Intent();
            intent.setAction("devicroft.BurnBoy.CANCEL_NOTIFY");
            intent.putExtra("id",NotificationCancelReceiver.NOTIFICATION_ID);
            sendBroadcast(intent);
        }

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LogService.getName().equals(service.service.getClassName())) {
                Log.d(LOG_TAG, " REBIND on SERVICE");
                Toast.makeText(this,"Still logging movement", Toast.LENGTH_SHORT);
                this.bindService(new Intent(this, LogService.class), serviceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    //MIDDLE icon on fab list
    private View.OnClickListener startLoggingFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(LOG_TAG,"FAB CLICK startLoggingFab");
            checkForPermission(PERMISSION_REQUEST_GPS_FINE);
            checkForPermission(PERMISSION_REQUEST_GPS_COARSE);
            checkForPermission(PERMISSION_REQUEST_INTERNET);

            FloatingActionButton startStop = (FloatingActionButton) findViewById(R.id.fab_start_logging);
            if(serviceRunning){
                Log.d(LOG_TAG, "Stopped tracking pressed");
                Toast.makeText(getApplicationContext(),"Stopped tracking", Toast.LENGTH_SHORT).show();
                stopService();
                serviceRunning = false;
                startStop.setLabelText("Start tracking");
            }else{
                Log.d(LOG_TAG, "Start tracking pressed");
                Toast.makeText(getApplicationContext(),"Started tracking", Toast.LENGTH_SHORT).show();
                initialiseService();
                serviceRunning = true;
                startStop.setLabelText("Stop tracking");
            }

        }
    };

    private void initialiseService() {
        Log.d(LOG_TAG, "initService");
        this.startService(new Intent(this, LogService.class));
        this.bindService(new Intent(this, LogService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void stopService(){
        Log.d(LOG_TAG, "stopService");
        this.unbindService(serviceConnection);
        this.stopService(new Intent(this, LogService.class));
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binderService) {
            Log.d(LOG_TAG, "serviceConnected" + componentName.flattenToShortString());
            logBinder = (LogService.LogBinder) binderService;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(LOG_TAG, "serviceDisconnected" + componentName.flattenToShortString());
            logBinder = null;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu");
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected");
        switch(item.getItemId()){
            case R.id.action_map:
                Log.d(LOG_TAG, "map");
                //start map view activity
                Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                mapIntent.putExtra("source", "browse");
                startActivity(mapIntent);
                break;
            case R.id.action_licenses:
                Log.d(LOG_TAG,"licenses");
                dispatchLicenseDialog();
                break;
            case R.id.action_history:
                Log.d(LOG_TAG,"history");
                Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(historyIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.action_delete:
                Log.d(LOG_TAG,"delete");
                dispatchDeleteAllDialog();
                break;
            default:
                Log.e(LOG_TAG,"error, default");
        }
        return false;
    }
    private void dispatchDeleteAllDialog(){
        Log.d(LOG_TAG, "dispatchDeleteAllDialog");
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.action_delete))
                .setMessage(getString(R.string.delete_all_propose))
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dispatchDeleteAllSnackbar();
                    }
                })
                .show();

    }
    private void dispatchLicenseDialog() {
        Log.d(LOG_TAG, "dispatchLicenseDialog");
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Licenses")
                .setMessage(Html.fromHtml(
                        getString(R.string.license_fab)  +
                                "<br><br>" +
                        getString(R.string.license_mpandroidchart) +
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
        Log.d(LOG_TAG,"setupFAB");
        //https://github.com/Clans/FloatingActionButton
        FloatingActionButton startButton = (FloatingActionButton) findViewById(R.id.fab_start_logging);
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.fab_add_activity);
        FloatingActionButton weightButton = (FloatingActionButton) findViewById(R.id.fab_log_weight);

        startButton.setOnClickListener(startLoggingFabClickListener);
        addButton.setOnClickListener(addLogFabClickListener);
        weightButton.setOnClickListener(weightLogFabClickListener);

        if(serviceRunning){
            startButton.setLabelText("Start tracking");
        }
    }

    //TOP icon on fab list
    private View.OnClickListener addLogFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(LOG_TAG, "FAB CLICK addLogFab");
            //TODO add activity for manually inputting point locations, time, medium (bike, run etc)
            int count = getTableCount(MovementLogProviderContract.MOVEMENT_URI);
            dispatchToast("Marker count: " + Integer.toString(getTableCount(MovementLogProviderContract.MARKER_URI)) + "Movement: " + count);
        }
    };
    //BOTTOM icon on fab list
    private View.OnClickListener weightLogFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(LOG_TAG,"FAB CLICK weightLogFab");
            //TODO initialise weight logging intent to activity
            int id = getIdOfLastInserted();
            deleteLog(id);

            //dispatchToast("implement weight log activity, with graph");
        }
    };

    private void dispatchToast(int stringID){
        Log.d(LOG_TAG,"dispatchToast stringID");
        Toast.makeText(this, getString(stringID), Toast.LENGTH_SHORT).show();
    }
    private void dispatchToast(String string){
        Log.d(LOG_TAG,"dispatchToast stringmessage");
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serviceConnection!=null) {
            stopService();
            serviceConnection = null;
        }
    }

    private void dispatchDeleteAllSnackbar() {
        Log.d(LOG_TAG,"dispatchDeleteAllSnackbar");
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.activity_main),  getString(R.string.delete_message), Snackbar.LENGTH_LONG)
                /*
                TODO figure out a nice way to undo a deletion
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar sb = Snackbar.make( findViewById(R.id.activity_main), getString(R.string.delete_all_undo_message), Snackbar.LENGTH_SHORT);
                        sb.show();
                    }
                }
                */
                ;

        //callback so i will actually delete when the snackbar is dismissed
        snackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                dispatchToast("Marker count: " + Integer.toString(getTableCount(MovementLogProviderContract.MARKER_URI)));
                //dbHelper.deleteAll();
            }
            @Override
            public void onShown(Snackbar snackbar) {
                deleteAllLogs();
            }
        });
        snackbar.show();
    }
    private boolean isServiceRunning(Class<?> serviceClass) {
        //https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
        ActivityManager manager = (ActivityManager) getSystemService(this.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getSimpleName()) ||
                    runningServiceInfo.service.getClassName().equals("LogService.java")){
                return true;
            }
        }
        return false;
    }

    private boolean initialiseAd(){
        Log.d(LOG_TAG,"initialiseAd");
        //load up an ad
        Log.d(LOG_TAG, "Starting ad" + getString(R.string.app_ad_id));
        //MobileAds.initialize(getApplicationContext(), getString(R.string.app_ad_id));
        AdView adView = (AdView) findViewById(R.id.main_banner_ad_view);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)    //TODO take off testDevice before release
                .build();
        adView.loadAd(adRequest);
        return true;

    }

    /*
    *       CONTENT PROVIDER METHODS METHOODSSS
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
    //for placeholders (?) must have an equal number of new String[] arguments to make it work
    public int deleteLog(int id){
        Log.d(LOG_TAG,"deleteLog");
        //delete individual movement log
        int success = getContentResolver().delete(
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
    private void deleteAllLogs(){
        Log.d(LOG_TAG,"deleteAllLogs");
        getContentResolver().delete(
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
        //TODO weird thing where i have 5 markers always there unable to delete
    }
    private void dispatchMkrMovCountsToast(){
        dispatchToast("Marker count: " + Integer.toString(getTableCount(MovementLogProviderContract.MARKER_URI)));
    }
    private int getTableCount(Uri tableUriFromContract){
        Log.d(LOG_TAG,"gettablecount");
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
            PERMISSION METHODS
     */
    private static final int PERMISSION_REQUEST_INTERNET = 1;
    private static final int PERMISSION_REQUEST_GPS_COARSE = 2;
    private static final int PERMISSION_REQUEST_GPS_FINE = 3;
   //future add an int array as a parameter to pass in multiple permissions at the same time
    private void checkForPermission(final int PERMISSION_REQUEST_CONSTANT){
        //https://developer.android.com/training/permissions/requesting.html

        //determine what permission we are after
        String manifestPermission;
        String rationale;
        switch(PERMISSION_REQUEST_CONSTANT){
            case PERMISSION_REQUEST_INTERNET:

                manifestPermission = android.Manifest.permission.INTERNET;
                rationale = getString(R.string.rationale_permission_internet);
                break;
            case PERMISSION_REQUEST_GPS_COARSE:
                manifestPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION;
                rationale = getString(R.string.rationale_permission_coarse);
                break;
            case PERMISSION_REQUEST_GPS_FINE:
                manifestPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
                rationale = getString(R.string.rationale_permission_fine);
                break;
            //add any more if need more permissions
            default:
                manifestPermission = android.Manifest.permission.INTERNET;
                rationale = getString(R.string.rationale_permission_internet);
        }

        //pass through
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                manifestPermission);

        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    manifestPermission)) {
                //user gets shown a quick rationale if seen and denied/skipped before requesting
                Toast.makeText(this, rationale, Toast.LENGTH_LONG).show();


            }
            //user hasnt seen this before so doesn't need permission rationale to show
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{manifestPermission},
                    PERMISSION_REQUEST_INTERNET);


        }

        //if user has denied permission before, we now show them why we need it

    }

    //callbackc method for permission request dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSION_REQUEST_INTERNET:
                //results empty if cancelled
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //have permission
                    Toast.makeText(this, getString(R.string.permission_features_enabled), Toast.LENGTH_SHORT).show();

                } else {
                    //denied permission

                }

                break;
            case PERMISSION_REQUEST_GPS_COARSE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //have permission
                    Toast.makeText(this, getString(R.string.permission_features_enabled), Toast.LENGTH_SHORT).show();

                } else {
                    //denied permission

                }
                break;
            case PERMISSION_REQUEST_GPS_FINE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //have permission
                    Toast.makeText(this, getString(R.string.permission_features_enabled), Toast.LENGTH_SHORT).show();

                } else {
                    //denied permission

                }
                break;
            default:

        }

    }


    /*
    *       dbhelper methods  (when using dbHelper solely to do all data stuff


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