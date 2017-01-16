package devicroft.burnboy.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import devicroft.burnboy.Data.DbHelper;
import devicroft.burnboy.Data.MovementLogProviderContract;
import devicroft.burnboy.R;
import devicroft.burnboy.Services.LogService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String LOG_TAG = "MAPS LOG";
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try{
            //TODO for tracking progress function from notification
        }catch(NullPointerException e){
            Log.i(LOG_TAG, "activity not started from notification progress button, ignore");
        }catch(RuntimeException e){
            Log.i(LOG_TAG, "activity not started from notification progress button, ignore");
        }



    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG,"onMapReady");
        mMap = googleMap;

        //if intent is of a particula
        String source = getIntent().getStringExtra("source");
        if(source.equals("history")){
            setupIndividualLogMap();

        }else if(source.equals("browse")){
            setupLogStartPointMap();
            testSetupMap();
        }else if(source.equals("trackingService")){
            setupViewProgressMap();
        }

        //define map settings
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setBuildingsEnabled(true);
        try{mMap.setMyLocationEnabled(true);}catch(SecurityException e){e.printStackTrace();}
        mMap.setBuildingsEnabled(true);





    }

    private void setupViewProgressMap() {
        //TODO
        //method called when the aciton on notification bar is pressed, to view the current tracking map
    }

    private void testSetupMap(){
        Log.d(LOG_TAG,"testsetupmap");
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    private void setupLogStartPointMap() {
        Log.d(LOG_TAG,"setupStartPOintMap");
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_display_simple));
        ArrayList<LatLng> startPoints = new ArrayList<>();

        //TODO build query to find the marker with the earliest time for each foreign key (MovementLog)
        //table, null null null null will return every marker we have
        Cursor c = getContentResolver().query(MovementLogProviderContract.MARKER_URI,
                null,
                null,
                null,
                null);

        c.moveToFirst();
        for (int i = 0; i < c.getCount() ; i++) {

            Date start = new Date(getIntent().getLongExtra("start", 0));
            String title = String.valueOf(dateFormat.format(start)) + " log";
            LatLng newPosition =  new LatLng(c.getDouble(c.getColumnIndex(DbHelper.COL_LAT)), c.getDouble(c.getColumnIndex(DbHelper.COL_LNG)));
            startPoints.add(newPosition);
            mMap.addMarker(new MarkerOptions()
                    .position(startPoints.get(i))
                    .title(title));

            //TODO update colour of marker depending on how long ago it was

        }
        if(startPoints.size() > 0){
        //animate camera towards the starting location
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoints.get(0), 16));
        }else{
            //when there aren't any logs created, show notification.
            Toast.makeText(this, getString(R.string.toast_map_empty), Toast.LENGTH_SHORT).show();
        }

        //TODO create button that moves and focusses on next marker
        setupNextButton();

    }

    private void setupNextButton() {
        Log.d(LOG_TAG,"setupNextButton");
        //TODO create fab for this

    }

    private View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO move from current marker focus, to next marker focus
            Log.d(LOG_TAG,"nextButtonClickListener");
            LatLng nextMarker = new LatLng(32,-151);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nextMarker, 12));
        }
    };

    private void setupIndividualLogMap() {
        Log.d(LOG_TAG,"setupIndivLogMap");
        //TODO fix npe, geos is empty
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_display_simple));
        ArrayList<LatLng> geos = getIntent().getParcelableArrayListExtra("markers");
        Date start = new Date(getIntent().getLongExtra("start", 0));
        Date end = new Date(getIntent().getLongExtra("end", 0));
        String title = "Start: " + String.valueOf(dateFormat.format(start)) +
                "End: " + String.valueOf(dateFormat.format(end));

        for (int i = 0; i < geos.size() ; i++) {
            mMap.addMarker(new MarkerOptions()
            .position(geos.get(i))
            .title(title));

        }
        //animate camera towards the starting location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(geos.get(0), 16));


    }

}
