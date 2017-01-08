package devicroft.burnboy.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;

import devicroft.burnboy.Data.DbHelper;
import devicroft.burnboy.Data.MovementLogProviderContract;
import devicroft.burnboy.HistoryExpandableListAdapter;
import devicroft.burnboy.Models.MovementLog;
import devicroft.burnboy.Models.MovementMarker;
import devicroft.burnboy.R;

public class HistoryActivity extends AppCompatActivity {

    ExpandableListView list;
    HistoryExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //display list of previous activities logged
        //hold to delete, or swipe left to show bin button
        //press to expand the list to show more details
        //expand shows the map button too, which goes to the view activity on map part

        list = (ExpandableListView) findViewById(R.id.historyList);
        adapter = new HistoryExpandableListAdapter(this, fetchLogs());
        list.setAdapter(adapter);

    }

    private ArrayList<MovementLog> fetchLogs() {
        Cursor cursor = getContentResolver().query(MovementLogProviderContract.MOVEMENT_URI, null, null, null, null);

        ArrayList<MovementLog> logs = new ArrayList<>();



        for (int i = 0; i < logs.size(); i++) {
            MovementLog m = new MovementLog(
            cursor.getLong(cursor.getColumnIndex(MovementLogProviderContract.MOV_START_TIME)),
            cursor.getLong(cursor.getColumnIndex(MovementLogProviderContract.MOV_END_TIME))
            );
            m.set_id(cursor.getInt(cursor.getColumnIndex(MovementLogProviderContract.MOV_ID)));
            logs.get(i).setMarkers(fetchMarkers(logs.get(i).get_id()));

        }

        return logs;
    }

    private ArrayList<MovementMarker> fetchMarkers(int id){
        ArrayList<MovementMarker> markers = new ArrayList<>();

        //So on the MARKER TABLE
        //we return all
        //that, inside the FK column that
        //matches the id desired
        Cursor c = getContentResolver().query(
                MovementLogProviderContract.MARKER_URI,  //content uri of table
                null,           //to return ALL for each row
                MovementLogProviderContract.MKR_FK_MOVEMENT_ID + "=?",           //selection clause
                new String[]{String.valueOf(id)},           //selection args
                null);          //sort order
        //go to the entry with the count integer
        c.moveToFirst();

        for (int i = 0; i < c.getCount(); i++) {
            MovementMarker m = new MovementMarker();
            String got = c.getString(c.getColumnIndex(MovementLogProviderContract.MKR_LAT));
            Double lat = Double.parseDouble(got);
            got = c.getString(c.getColumnIndex(MovementLogProviderContract.MKR_LNG));
            Double lng = Double.parseDouble(got);
            m.setLatlng(new LatLng(lat, lng));
            m.setSnippet(c.getString(c.getColumnIndex(MovementLogProviderContract.MKR_SNIPPET)));
            m.setTitle(c.getString(c.getColumnIndex(MovementLogProviderContract.MKR_TITLE)));
            m.setTime(c.getString(c.getColumnIndex(MovementLogProviderContract.MKR_TIME)));
            m.setId(c.getInt(c.getColumnIndex(MovementLogProviderContract.MKR_ID_MARKER)));
            markers.add(new MovementMarker());
        }
        return markers;
    }



}
