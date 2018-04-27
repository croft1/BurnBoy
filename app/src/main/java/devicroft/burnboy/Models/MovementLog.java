package devicroft.burnboy.Models;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import devicroft.burnboy.Data.DbHelper;

import static devicroft.burnboy.Data.MovementLogProviderContract.AUTHORITY;

/**
 * Created by m on 31-Dec-16.
 */

public class MovementLog {

    private int _id;
    private ArrayList<MovementMarker> markers = new ArrayList<>();
    private Date startTime = null;
    private Date endTime = null;
    public static final String CONTENT_PATH = "content://"+AUTHORITY+"/"+ DbHelper.TABLENAME_MOVEMENT+"/";



    private boolean savedInDatabase = false;



    SimpleDateFormat timeDisplayFormat = new SimpleDateFormat("HH:mm:ss aaa");
    SimpleDateFormat fullDateDisplayFormat = new SimpleDateFormat("EEE, d MMM, yyyy");
    SimpleDateFormat totalDurationFormat = new SimpleDateFormat("HH:mm:ss");


    public MovementLog(int id, long startInMillis, long endInMillis) {
        //for retrieving saved logs
        _id = id;
        startTime = new Date(startInMillis);
        endTime = new Date(endInMillis);
        this.markers = markers;
    }

    //for new logs, that only can have a start. others are default
    public MovementLog(long start) {
        startTime = new Date(start);
        //endTime = new Date(start + 100000);       keep default, or not keep it
    }

    //goes through all markers stored and sums the distance between each in successive order
    //https://developer.android.com/reference/android/location/Location.html
    //https://stackoverflow.com/questions/14394366/find-distance-between-two-points-on-map-using-google-map-api-v2
    public double getTotalDistanceMoved(){
        float totalDistance = 0;
        for(int i = 0; i < markers.size() - 1; i++){
            float[] dist = new float[2];
            Location current = new Location("");
            current.setLongitude(markers.get(i).getLatlng().longitude);
            current.setLatitude(markers.get(i).getLatlng().latitude);
            Location next = new Location("");
            next.setLongitude(markers.get(i + 1).getLatlng().longitude);
            next.setLatitude(markers.get(i + 1).getLatlng().latitude);
            Location.distanceBetween(current.getLatitude(), current.getLongitude(),
                    current.getLatitude(), current.getLongitude(), dist);
            totalDistance += dist[0];
        }
        return totalDistance;
    }

    /*
            CALCULATION METHODS
     */

    //https://stackoverflow.com/questions/6981916/how-to-calculate-distance-between-two-locations-using-their-longitude-and-latitu
    //no
    /*
            GETTERS AND SETTERS
     */

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
        this.setSavedInDatabase(true);
    }

    public ArrayList<MovementMarker> getMarkers() {
        return markers;
    }

    public void setMarkers(ArrayList<MovementMarker> markers) {
        this.markers = markers;
    }

    public Date getStartTime() {
        return startTime;
    }


    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }


    public ArrayList<LatLng> getAllMarkerLatLng(){
        //because im lazy and this is a quick way to do this. used to pass all marker info to map activity
        ArrayList<LatLng> m = new ArrayList<>();
        for (int i = 0; i < markers.size(); i++) {
            m.add(markers.get(i).getLatlng());
        }
        return m;
    }

    public String getDisplayTotalDuration(){
        //take times, subtract their doubles, create new date out of that, then format it and voila
        return totalDurationFormat.format(new Date(getEndTime().getTime() - getStartTime().getTime()));
    }

    public String getFormattedStartDate(){
        return fullDateDisplayFormat.format(startTime);
    }

    public String getFormattedEndDate(){
        return fullDateDisplayFormat.format(endTime);
    }
    public String getFormattedEndTime(){return timeDisplayFormat.format(endTime);}
    public String getFormattedStartTime(){
        return timeDisplayFormat.format(startTime);
    }




    public boolean hasMarker(){
        return (markers.size() > 0) ? true : false;
    }

    public boolean isSavedInDatabase() {
        return savedInDatabase;
    }

    public void setSavedInDatabase(boolean savedInDatabase) {
        this.savedInDatabase = savedInDatabase;
    }



}
