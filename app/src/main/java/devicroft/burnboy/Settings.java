package devicroft.burnboy;

/**
 * Created by m on 31-Dec-16.
 */

public class Settings {

    //save all items in a database
    //set the values of settings on creation
    //change when settings has been started

    //SETTINGS AVAILABLE
    boolean unitsIsKilometers = true;
    int timeBetweenLocationFetch = 5000;        //milliseconds
    boolean nightMode = false;

    //singleton settings method as we only ever need one settings active at a time
    private static Settings singleton = new Settings( );
    private Settings() { }

    public static Settings getInstance( ) {
        return singleton;
    }

    public boolean isUnitsIsKilometers() {
        return unitsIsKilometers;
    }

    public void setUnitsIsKilometers(boolean unitsIsKilometers) {
        this.unitsIsKilometers = unitsIsKilometers;
    }

    public int getTimeBetweenLocationFetch() {
        return timeBetweenLocationFetch;
    }

    public void setTimeBetweenLocationFetch(int timeBetweenLocationFetch) {
        this.timeBetweenLocationFetch = timeBetweenLocationFetch;
    }

    public boolean isNightMode() {
        return nightMode;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
    }
}
