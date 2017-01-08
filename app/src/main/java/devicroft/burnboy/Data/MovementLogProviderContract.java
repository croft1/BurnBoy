package devicroft.burnboy.Data;

import android.net.Uri;

import devicroft.burnboy.Models.MovementLog;
import devicroft.burnboy.Models.MovementMarker;

/**
 * Created by m on 31-Dec-16.
 */

public class MovementLogProviderContract {

    //outlining the parameters for what everything is worth and pointing to
    //reememember i need to put the name of the actual provider class here, not just the package name
    //double check the depth of the path for the provider
    public static final String AUTHORITY = "devicroft.burnboy.Data.MovementLogContentProvider";

    public static final Uri MOVEMENT_URI = Uri.parse(MovementLog.CONTENT_PATH);
    public static final Uri MARKER_URI = Uri.parse(MovementMarker.CONTENT_PATH);
    public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"/#");

    //MOVEMENT TABLE
    public static final String MOV_ID = DbHelper.COL_ID_MOVE;
    public static final String MOV_START_TIME = DbHelper.COL_STARTTIME;
    public static final String MOV_END_TIME = DbHelper.COL_ENDTIME;

    //MARKER TABLE
    public static final String MKR_ID_MARKER = DbHelper.COL_ID_MARKER;
    public static final String MKR_TITLE = DbHelper.COL_TITLE;
    public static final String MKR_SNIPPET = DbHelper.COL_SNIPPET;
    public static final String MKR_LAT = DbHelper.COL_LAT;
    public static final String MKR_LNG = DbHelper.COL_LNG;
    public static final String MKR_TIME = DbHelper.COL_TIME;
    public static final String MKR_FK_MOVEMENT_ID = DbHelper.COL_FK_MOVEMENT_ID;

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/MovementLogContentProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/MovementLogContentProvider.data.text";

}
