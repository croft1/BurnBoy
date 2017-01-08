package devicroft.burnboy.Models;

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
    ArrayList<MovementMarker> markers = new ArrayList<>();
    ArrayList<Date> markerDate = new ArrayList<>();
    Date startTime;
    Date endTime;
    public static final String CONTENT_PATH = "content://"+AUTHORITY+"/"+ DbHelper.TABLENAME_MOVEMENT+"/";

    public MovementLog() {
        //starting a new log fresh
        startTime = Calendar.getInstance().getTime();
        endTime = new Date(startTime.getTime() + 600000);   //default is 1 hour end time
        markers.add(new MovementMarker());
        markers.add(new MovementMarker());
        markers.add(new MovementMarker());
        markers.add(new MovementMarker());
        markers.add(new MovementMarker());
    }

    public MovementLog(long start, long end) {
        startTime = new Date(start);
        endTime = new Date(end);

    }

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

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }








}
