package devicroft.burnboy.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import devicroft.burnboy.Data.LogDBHelper;
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

        dbTest();


    }

    private void setupFAB() {
        //https://github.com/Clans/FloatingActionButton
        FloatingActionButton startButton = (FloatingActionButton) findViewById(R.id.start_activity);
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add_activity);

        startButton.setOnClickListener(startLoggingFabClickListener);
        addButton.setOnClickListener(addLogFabClickListener);

    }

    private View.OnClickListener startLoggingFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivityLogging();
            //dispatchToast(R.string.start_fitnesslogging);
            dispatchToast(Integer.toString(dbHelper.getLogCount()));
        }
    };

    private View.OnClickListener addLogFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO add activity for manually inputting point locations, time, medium (bike, run etc)
            int count = dbHelper.getLogCount();
            testAddLog();
            dispatchToast(Integer.toString(dbHelper.getLogCount() - count)  + " added");
        }
    };


    private void dispatchToast(int stringID){
        Toast.makeText(this, getString(stringID), Toast.LENGTH_SHORT).show();
    }
    private void dispatchToast(String string){
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    private boolean startActivityLogging(){
        //initialise class to start tracking

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
    *       TEST METHOODSSS
     */
    private void dbTest(){
        dbHelper = new LogDBHelper(this);
        testAddLog();
        dispatchToast(Integer.toString(dbHelper.getLogCount()));
        dbHelper.deleteAll();   //TODO remove delete call

    }

    private void testAddLog(){
        dbHelper.insertNewLog(new MovementLog(1000, 2000));
        dbHelper.insertNewLog(new MovementLog());
    }

    }
