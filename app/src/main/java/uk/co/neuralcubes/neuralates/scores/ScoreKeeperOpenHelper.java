package uk.co.neuralcubes.neuralates.scores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by jmanart on 22/04/2016.
 */
public class ScoreKeeperOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ScoreBoard.db";

    public static abstract class ScoreEntry implements BaseColumns {
        public static final String TABLE_NAME = "score";
        public static final String COLUMN_NAME_ENTRY_ID = "user";
        public static final String COLUMN_NAME_SCORE = "score";
        public static final String COLUMN_NAME_ACCUMULATED = "accumulated";
        public static final String COLUMN_NAME_DURATION = "duration";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }

    private static final String SQL_CREATE_DATABASE = "CREATE TABLE " + ScoreEntry.TABLE_NAME +
            " ( " + ScoreEntry._ID + " INTEGER PRIMARY KEY, " +
            ScoreEntry.COLUMN_NAME_ENTRY_ID + " TEXT, " +
            ScoreEntry.COLUMN_NAME_SCORE + " FLOAT, " +
            ScoreEntry.COLUMN_NAME_ACCUMULATED + " FLOAT, " +
            ScoreEntry.COLUMN_NAME_DURATION + " FLOAT, " +
            ScoreEntry.COLUMN_NAME_TIMESTAMP + " DATE " +
            ")";

    public ScoreKeeperOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DATABASE);
    }

    public void onUpgrade(SQLiteDatabase db, int from, int to) {
    }
}
