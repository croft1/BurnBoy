package devicroft.burnboy.Activities;

import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import devicroft.burnboy.Data.DbHelper;
import devicroft.burnboy.Data.MovementLogProviderContract;
import devicroft.burnboy.Models.MovementMarker;
import devicroft.burnboy.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //if intent is of a particula
        String source = getIntent().getStringExtra("source");
        if(source.equals("history")){
            setupIndividualLogMap();

        }else if(source.equals("browse")){
            setupLogStartPointMap();
            testSetupMap();
        }

        //define map settings
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setBuildingsEnabled(true);
        try{mMap.setMyLocationEnabled(true);}catch(SecurityException e){e.printStackTrace();}
        mMap.setBuildingsEnabled(true);





    }

    private void testSetupMap(){
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    private void setupLogStartPointMap() {
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
        //animate camera towards the starting location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoints.get(0), 16));

        //TODO create button that moves and focusses on next marker
        setupNextButton();

    }

    private void setupNextButton() {
        //TODO create fab for this

    }

    private View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO move from current marker focus, to next marker focus
            LatLng nextMarker = new LatLng(32,-151);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nextMarker, 12));
        }
    };

    private void setupIndividualLogMap() {
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
