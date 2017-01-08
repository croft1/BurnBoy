package devicroft.burnboy.Activities;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(true);




    }

    private void testSetupMap(){
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    private void setupLogStartPointMap() {

    }

    private void setupIndividualLogMap() {
        ArrayList<LatLng> geos = getIntent().getParcelableArrayListExtra("markers");
        Date start = new Date(getIntent().getLongExtra("start", 0));
        Date end = new Date(getIntent().getLongExtra("end", 0));
        String title = "Start: " + String.valueOf(start.getTime()) +
                "End: " + String.valueOf(end.getTime());

        for (int i = 0; i < geos.size() ; i++) {
            mMap.addMarker(new MarkerOptions()
            .position(geos.get(i))
            .title(title));

        }
        //animate camera towards the starting location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(geos.get(0), 14));

    }

}
