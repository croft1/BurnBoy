package devicroft.burnboy.Models;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import devicroft.burnboy.Data.DbHelper;

import static devicroft.burnboy.Data.MovementLogProviderContract.AUTHORITY;

/**
 * Created by m on 31-Dec-16.
 */

public class MovementLog {

    int _id;
    private ArrayList<MovementMarker> markers = new ArrayList<>();
    private ArrayList<Date> markerDate = new ArrayList<>();
    private Date startTime;
    private Date endTime;
    public static final String CONTENT_PATH = "content://"+AUTHORITY+"/"+ DbHelper.TABLENAME_MOVEMENT+"/";
    private double totalDistanceMoved;



    SimpleDateFormat displayDateFormat = new SimpleDateFormat("HH:mm:ss aaa -> HH:mm:ss aaa - EEE, d MMM, yyyy");
    SimpleDateFormat totalDurationFormat = new SimpleDateFormat("HH:mm:ss aaa -> HH:mm:ss aaa - EEE, d MMM, yyyy");

    public MovementLog() {
        //starting a new log fresh
        startTime = Calendar.getInstance().getTime();
        endTime = new Date(startTime.getTime() + 600000);   //default is 1 hour end time
        doAddNewMarker(new MovementMarker());
        doAddNewMarker(new MovementMarker());
        doAddNewMarker(new MovementMarker(2));
        doAddNewMarker(new MovementMarker(4));
        doAddNewMarker(new MovementMarker(3));
    }

    public MovementLog(long start, long end) {
        startTime = new Date(start);
        endTime = new Date(end);
    }

    /*


     */


    //goes through all markers stored and sums the distance between each in successive order
    private void calculateAllMarkersDistanceMoved(){
        for(int i = 0; i < markers.size() - 1; i++){
            Location current = new Location("");
            current.setLongitude(markers.get(i).getLatlng().longitude);
            current.setLatitude(markers.get(i).getLatlng().latitude);
            Location next = new Location("");
            next.setLongitude(markers.get(i + 1).getLatlng().longitude);
            next.setLatitude(markers.get(i + 1).getLatlng().latitude);
            totalDistanceMoved += current.distanceTo(next);
        }
    }

    private void addToTotalDuration(){

    }

    private void addToTotalDistance(LatLng latlng){
        /*
        Location current = new Location("");
        current.setLongitude(markers.get(markers.size()).getLatlng().longitude);
        current.setLatitude(markers.get(markers.size()).getLatlng().latitude);
        Location next = new Location("");
        next.setLongitude(latlng.longitude);
        next.setLatitude(latlng.latitude);
        totalDistanceMoved += current.distanceTo(next);
        */

        totalDistanceMoved += calculateDistance(
                markers.get(markers.size() -1).getLatlng().latitude,
                markers.get(markers.size() -1).getLatlng().longitude,
                latlng.latitude,
                latlng.longitude
        );
    }

    //here as we add more and more markers to this object, we tally up the total distance between each marker.
    //we do it here to avoid one big calculation (see calculateAllMarkersDistanceMoved when we are inflating the layout: its already nicely stored
    private void doAddNewMarker(MovementMarker m){
        markers.add(m);
        addToTotalDistance(m.getLatlng());
        //TODO add time addToTotalDuration();
    }


    /*
            CALCULATION METHODS
     */

    //https://stackoverflow.com/questions/6981916/how-to-calculate-distance-between-two-locations-using-their-longitude-and-latitu
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

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
    }

    public ArrayList<MovementMarker> getMarkers() {
        return markers;
    }

    public void setMarkers(ArrayList<MovementMarker> markers) {
        this.markers = markers;
    }

    public ArrayList<Date> getMarkerDate() {
        return markerDate;
    }

    public void setMarkerDate(ArrayList<Date> markerDate) {
        this.markerDate = markerDate;
    }

    public Date getStartTime() {
        return startTime;
    }

    public String getFormattedStartDate(){
        return displayDateFormat.format(startTime);
    }

    public String getFormattedEndDate(){
        return displayDateFormat.format(endTime);
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public double getTotalDistanceMoved() {
        return totalDistanceMoved;
    }

    public void setTotalDistanceMoved(float totalDistanceMoved) {
        this.totalDistanceMoved = totalDistanceMoved;
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
        return totalDurationFormat.format(new Date(getStartTime().getTime() - getEndTime().getTime()));
    }
}
