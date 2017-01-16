package devicroft.burnboy.Receivers;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import devicroft.burnboy.Data.DbHelper;
import devicroft.burnboy.Data.MovementLogProviderContract;
import devicroft.burnboy.Services.LogService;
import devicroft.burnboy.Services.MovementTrackingService;

/**
 * Created by m on 12-Jan-17.
 */

public class ActivityTrackingBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(MovementTrackingService.ACTION_RECEIVE_NEW_LOCATION)){
            Toast.makeText(context, "NEWLOC SERV RCVR Successful!!! "  , Toast.LENGTH_SHORT)
                    .show();


            //when notification is cancelled
            if(intent.hasExtra("cancel")){



            }

            /*
            was used when i thought it might be best to use a receiver to get new data and save it


            Location location = intent.getParcelableExtra("gps");
            //TODO do osmething with location when we get it from service
            //add location to database from here

            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MovementLogProviderContract.MKR_TITLE, location.getProvider());      //TODO change getProvider() to something more meaningful
            values.put(MovementLogProviderContract.MKR_LAT, location.getLatitude());
            values.put(MovementLogProviderContract.MKR_LNG, location.getLongitude());
            values.put(MovementLogProviderContract.MKR_SNIPPET, "Altitude: " + String.valueOf(location.getAltitude())); //TODO change to meaningful snippet, MAYBE. i like altitude there, though refactoring would need to be done
            values.put(MovementLogProviderContract.MKR_TIME, location.getTime());
            values.put(MovementLogProviderContract.MKR_FK_MOVEMENT_ID, LogService.);
            cr.insert(MovementLogProviderContract.MARKER_URI, values);

                */
        }
    }

}
