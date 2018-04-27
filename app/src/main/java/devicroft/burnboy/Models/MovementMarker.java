package devicroft.burnboy.Models;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;

import devicroft.burnboy.Data.DbHelper;

import static devicroft.burnboy.Data.MovementLogProviderContract.AUTHORITY;

/**
 * Created by m on 01-Jan-17.
 */

public class MovementMarker {

    private static final String LOG_TAG = "MARKER";
    private int id;
    private String title;
    private String time;
    private LatLng latlng;
    private String snippet;
    public static final String CONTENT_PATH = "content://"+AUTHORITY+"/"+ DbHelper.TABLENAME_MARKER+"/";



    public MovementMarker(String title, String time, LatLng latlng, String snippet) {
        this.title = title;
        this.time = time;
        this.latlng = latlng;
        this.snippet = snippet;
    }

    /*
    public MovementMarker() {
        this.title = "test";
        this.time = Calendar.getInstance().getTime().toString();
        this.latlng = new LatLng(-34, 151);
        this.snippet = "Sydney";
    }

    //to randomly assign a lng value
    public MovementMarker(int i) {
        this.title = "test";
        this.time = Calendar.getInstance().getTime().toString();
        this.latlng = new LatLng(-34, i);
        this.snippet = "test";
    }
    */
    public MarkerOptions getAsMarkerOptions(){
        return new MarkerOptions()
                .position(latlng)
                .title(title)
                .snippet(snippet + time);

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }
    public long getLongTime() {
        try{
            DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            Date date = format.parse(getTime());
            return date.getTime();
        }catch(ParseException e ){
            Log.e(LOG_TAG, "couldnt parse dateTime from string");
        }
        return 0;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public double getTotalTime(){
        double sumTime = 0;
        try{
            DateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss zZ yyyy", Locale.ENGLISH);
            Date date = format.parse(getTime());
            sumTime += date.getTime();
        }catch(ParseException e ){
            Log.e(LOG_TAG, "couldnt parse date from string" + e.getMessage());
        }
        return sumTime;
    }
}
