package devicroft.burnboy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;

import devicroft.burnboy.Activities.HistoryActivity;
import devicroft.burnboy.Activities.MapsActivity;
import devicroft.burnboy.Models.MovementLog;

/**
 * Created by m on 08-Jan-17.
 */

public class HistoryExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<MovementLog> logs;
    private Button mapButton;
    private ExpandableListView listView;


    public HistoryExpandableListAdapter(Context context, ArrayList<MovementLog> logs) {
        this.context = context;
        this.logs = logs;
    }

    public HistoryExpandableListAdapter() {
        super();
    }

    /*

     */



    /*
            START OVERRIDES
     */

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public void onGroupExpanded(int groupPosition) {

        super.onGroupExpanded(groupPosition);

    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return logs.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return logs.size();
    }

    @Override
    public Object getGroup(int i) {
        return logs.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return logs.get(i);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_history_header, null);
        }
        TextView count = (TextView) view.findViewById(R.id.summary_number_label);
        TextView times = (TextView) view.findViewById(R.id.summary_startend_times_label);
        times.setText(logs.get(i).getFormattedStartDate() + " -> " +  logs.get(i).getFormattedEndDate());
        count.setText(String.valueOf(i + 1));

        view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        return view;
    }

    @Override
    public View getChildView(final int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_history_expanded, null);
        }
        TextView distance = (TextView) view.findViewById(R.id.expanded_distance_label);
        TextView duration = (TextView) view.findViewById(R.id.expanded_duration_label);
        distance.setText("Distance: " + String.valueOf(logs.get(i).getTotalDistanceMoved()));
        duration.setText("Duration: " + logs.get(i).getDisplayTotalDuration());
        //need to have for list
        Button mapButton = (Button) view.findViewById(R.id.expanded_map_button);
        mapButton.setFocusable(false);
        mapButton.setClickable(false);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapIntent = new Intent(context, MapsActivity.class);
                mapIntent.putParcelableArrayListExtra("markers", logs.get(i).getAllMarkerLatLng());
                mapIntent.putExtra("start", logs.get(i).getStartTime().getTime());
                mapIntent.putExtra("end", logs.get(i).getEndTime().getTime());
                mapIntent.putExtra("source", "history");
                context.startActivity(mapIntent);   //https://stackoverflow.com/questions/20235650/start-activity-when-custom-listview-button-clicked

                Activity a = (Activity) context;    //https://stackoverflow.com/questions/30931866/overridependingtransition-not-work-in-adapterandroid
                a.overridePendingTransition(0, 0);
            }
        });

        return view;
    }



    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


    ///GETTER SETTERS

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ArrayList<MovementLog> getLogs() {
        return logs;
    }

    public void addLog(MovementLog logs) {
        this.logs.add(logs);
        if(mapButton != null){
            mapButton.setEnabled(true);
        }
    }


    //other




}
