package devicroft.burnboy.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;

import devicroft.burnboy.Data.DbHelper;

import static devicroft.burnboy.Data.MovementLogProviderContract.AUTHORITY;

/**
 * Created by m on 01-Jan-17.
 */

public class MovementMarker {

    private int id;
    private String title;
    private String time;
    private LatLng latlng;
    private String snippet;
    public static final String CONTENT_PATH = "content://"+AUTHORITY+"/"+ DbHelper.TABLENAME_MOVEMENT+"/";


    public MovementMarker(String title, String time, LatLng latlng, String snippet) {
        this.title = title;
        this.time = time;
        this.latlng = latlng;
        this.snippet = snippet;
    }

    public MovementMarker() {
        this.title = "test";
        this.time = Calendar.getInstance().getTime().toString();
        this.latlng = new LatLng(-34, 151);
        this.snippet = "Sydney";
    }

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
}
