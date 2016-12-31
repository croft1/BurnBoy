package devicroft.burnboy.Data;

import android.net.Uri;

/**
 * Created by m on 31-Dec-16.
 */

public class MovementLogProviderContract {

    //outlining the parameters for what everything is worth and pointing to
    //reememember i need to put the name of the actual provider class here, not just the package name
    public static final String AUTHORITY = "devicroft.rosettarecipes.RecipeProvider";

    public static final Uri MOVEMENT_URI = Uri.parse("content://"+AUTHORITY+"/"+LogDBHelper.TABLENAME_MOVEMENT);
    public static final Uri MARKER_URI = Uri.parse("content://"+AUTHORITY+"/"+LogDBHelper.TABLENAME_MARKER);
    public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"/#");

    //MOVEMENT TABLE
    public static final String MOV_ID = LogDBHelper.COL_ID_MOVE;
    public static final String MOV_START_TIME = LogDBHelper.COL_STARTTIME;
    public static final String MOV_END_TIME = LogDBHelper.COL_ENDTIME;

    //MARKER TABLE
    public static final String MKR_ID_MARKER = LogDBHelper.COL_ID_MARKER;
    public static final String MKR_TITLE = LogDBHelper.COL_TITLE;
    public static final String MKR_SNIPPET = LogDBHelper.COL_SNIPPET;
    public static final String MKR_LAT = LogDBHelper.COL_LAT;
    public static final String MKR_LNG = LogDBHelper.COL_LNG;
    public static final String MKR_TIME = LogDBHelper.COL_TIME;
    public static final String MKR_FK_MOVEMENT_ID = LogDBHelper.COL_FK_MOVEMENT_ID;

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/MovementLogContentProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/MovementLogContentProvider.data.text";

}
