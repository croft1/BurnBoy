package devicroft.burnboy.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;

import devicroft.burnboy.Data.DbHelper;
import devicroft.burnboy.Data.DbQueries;
import devicroft.burnboy.Data.MovementLogProviderContract;
import devicroft.burnboy.HistoryExpandableListAdapter;
import devicroft.burnboy.Models.MovementLog;
import devicroft.burnboy.Models.MovementMarker;
import devicroft.burnboy.R;


public class HistoryActivity extends AppCompatActivity {
    private static final String LOG_TAG = "HISTORY LOG";
    ExpandableListView list;
    HistoryExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //display list of previous activities logged
        //hold to delete, or swipe left to show bin button
        //press to expand the list to show more details
        //expand shows the map button too, which goes to the view activity on map part

        list = (ExpandableListView) findViewById(R.id.historyList);
        adapter = new HistoryExpandableListAdapter(this);
        list.setAdapter(adapter);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                int toDeleteId = (int) view.getTag(R.string.row_delete_key);

                Log.d(LOG_TAG,"delete log from long click");
                //delete individual movement log
                int success = getContentResolver().delete(
                        MovementLogProviderContract.MOVEMENT_URI,   //set uri
                        DbQueries.ID_EQUALS_PLACEHOLDER,   //selection clause to find the id
                        new String[]{"" + String.valueOf(toDeleteId)}    //selection args (after WHERE ...)
                );

                Snackbar.make(findViewById(R.id.activity_history), getString(R.string.delete_message), Snackbar.LENGTH_SHORT).show();

                //probably only need one of these
                adapter.notifyDataSetChanged();
                adapter.notifyDataSetInvalidated();

                return (success>0)?true:false;
            }
        });
        Toast.makeText(this,"Hold row to delete log",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG,"onBackPressed");
        super.onBackPressed();
        overridePendingTransition(0,0);
    }





}
