package devicroft.burnboy.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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
            Bundle b = intent.getExtras();


            //TODO do osmething with location when we get it from service

        }
    }

    public ActivityTrackingBroadcastReceiver() {
        super();
    }
}
