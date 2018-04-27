package devicroft.burnboy.Data;

/**
 * Created by m on 01-Jan-17.
 */

public class DbQueries {
    //
    public static final String SELECT_MOST_RECENT_MOVEMENTLOG_ID = DbHelper.COL_ID_MOVE + " from " + DbHelper.TABLENAME_MOVEMENT;
    public static final String GET_TABLE_COUNT_AS_CURSORINT = "count(*) AS count";
    public static final String ID_EQUALS_PLACEHOLDER = "id = ?";
    public static final String FOREIGN_KEYS_ON = "PRAGMA foreign_keys = ON;";

    //TODO make better use of this class
}
