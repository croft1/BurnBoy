package devicroft.burnboy.Receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by m on 12-Jan-17.
 */

public class NotificationCancelReceiver extends BroadcastReceiver {

    public static final String LOG_TAG = "NOTIF_CANCEL_RCVR";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        if(intent.hasExtra("notificationID")){
            Log.d(LOG_TAG, "found extra 'notificationID'");
            //when received, takes the intent with notification id and the cancels that notif
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(intent.getIntExtra("notificationID", 0));
        }
    }

    @Override
    public IBinder peekService(Context myContext, Intent service) {
        return super.peekService(myContext, service);
    }

    public NotificationCancelReceiver() {
        super();
    }
}
