package devicroft.burnboy.Data;

/**
 * Created by m on 01-Jan-17.
 */

public class DbQueries {
    //
    public static final String SELECT_MOST_RECENT_MOVEMENTLOG_ID = "SELECT " + LogDBHelper.COL_ID_MOVE + " from " + LogDBHelper.TABLENAME_MOVEMENT + " order by " + LogDBHelper.COL_ID_MOVE + " DESC limit 1";
}
