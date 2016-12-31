package devicroft.burnboy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by m on 13-Dec-16.
 */

public class MovementTrackingService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //TODO create a movement tracker
        //https://stackoverflow.com/questions/28535703/best-way-to-get-user-gps-location-in-background-in-android
        //https://stackoverflow.com/questions/8828639/android-get-gps-location-via-a-service

        return null;
    }
}
