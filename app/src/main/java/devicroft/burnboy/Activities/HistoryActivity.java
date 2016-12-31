package devicroft.burnboy.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import devicroft.burnboy.R;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //display list of previous activities logged
        //hold to delete, or swipe left to show bin button
        //press to expand the list to show more details
        //expand shows the map button too, which goes to the view activity on map part


    }
}
