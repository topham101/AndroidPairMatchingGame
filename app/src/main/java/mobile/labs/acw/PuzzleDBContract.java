package mobile.labs.acw;

import android.provider.BaseColumns;

/**
 * Created by Matt on 26/03/2017.
 */

public final class PuzzleDBContract {
    private PuzzleDBContract(){}

    //region -- TABLES --
    //region Puzzle Table
    public static abstract class PuzzleEntry implements BaseColumns {
        public static final String TABLE_NAME = "Puzzle";
        public static final String COLUMN_NAME_NAME = "Name";
        public static final String COLUMN_NAME_PICTURESET_ID = "PictureSet_ID";
        public static final String COLUMN_NAME_ROWS = "Rows";
        public static final String COLUMN_NAME_LAYOUT = "Layout";
        public static final String COLUMN_NAME_HIGHSCORE = "Highscore";
    }
    //endregion

    //region PictureSet Table
    public static abstract class PictureSetEntry implements BaseColumns {
        public static final String TABLE_NAME = "PictureSet";
        public static final String COLUMN_NAME_NAME = "Name";
    }
    //endregion

    //region PictureSet_Relationship Table
    public static abstract class PictureSetRelationshipEntry implements BaseColumns {
        public static final String TABLE_NAME = "PictureSet_Relationship";
        public static final String COLUMN_NAME_PICTURESET_ID = "PictureSet_ID";
        public static final String COLUMN_NAME_PICTURE_ID = "Picture_ID";
    }
    //endregion

    //region Picture Table
    public static abstract class PictureEntry implements BaseColumns {
        public static final String TABLE_NAME = "Picture";
        public static final String COLUMN_NAME_NAME = "Name";
    }
    //endregion
    //endregion

    //region SQL CREATE strings
    public static final String SQL_CREATE_PUZZLE_TABLE = "CREATE TABLE " + PuzzleEntry.TABLE_NAME + " (" + PuzzleEntry._ID + " INTEGER PRIMARY KEY, " + PuzzleEntry.COLUMN_NAME_NAME + " TEXT UNIQUE NOT NULL, " + PictureSetRelationshipEntry.COLUMN_NAME_PICTURESET_ID + " INTEGER NOT NULL, " + PuzzleEntry.COLUMN_NAME_ROWS + " INTEGER NOT NULL, " + PuzzleEntry.COLUMN_NAME_LAYOUT + " TEXT NOT NULL, " + PuzzleEntry.COLUMN_NAME_HIGHSCORE + " INTEGER DEFAULT 0, FOREIGN KEY (" + PuzzleEntry.COLUMN_NAME_PICTURESET_ID + ") REFERENCES " + PictureSetEntry.TABLE_NAME + "(" + PictureSetEntry._ID + "));";
    public static final String SQL_CREATE_PICTURESET_TABLE = "CREATE TABLE " + PictureSetEntry.TABLE_NAME + " (" + PictureSetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PictureSetEntry.COLUMN_NAME_NAME + " TEXT UNIQUE NOT NULL);";
    public static final String SQL_CREATE_PICTURESET_RELATIONSHIP_TABLE = "CREATE TABLE " + PictureSetRelationshipEntry.TABLE_NAME + " (" + PictureSetRelationshipEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PictureSetRelationshipEntry.COLUMN_NAME_PICTURESET_ID + " INTEGER NOT NULL, " + PictureSetRelationshipEntry.COLUMN_NAME_PICTURE_ID + " INTEGER NOT NULL, FOREIGN KEY (" + PictureSetRelationshipEntry.COLUMN_NAME_PICTURESET_ID + ") REFERENCES " + PictureSetEntry.TABLE_NAME + "(" + PictureSetEntry._ID + "), FOREIGN KEY (" + PictureSetRelationshipEntry.COLUMN_NAME_PICTURE_ID + ") REFERENCES " + PictureEntry.TABLE_NAME + "(" + PictureEntry._ID + "));";
    public static final String SQL_CREATE_PICTURE_TABLE = "CREATE TABLE " + PictureEntry.TABLE_NAME + "(" + PictureEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PictureEntry.COLUMN_NAME_NAME + " TEXT UNIQUE NOT NULL);";
    //endregion

    //region SQL DROP strings
    public static final String SQL_DELETE_PUZZLE_TABLE = "DROP TABLE IF EXISTS " + PuzzleEntry.TABLE_NAME;
    public static final String SQL_DELETE_PICTURESET_TABLE = "DROP TABLE IF EXISTS " + PictureSetEntry.TABLE_NAME;
    public static final String SQL_DELETE_PICTURESET_RELATIONSHIP_TABLE = "DROP TABLE IF EXISTS " + PictureSetRelationshipEntry.TABLE_NAME;
    public static final String SQL_DELETE_PICTURE_TABLE = "DROP TABLE IF EXISTS " + PictureEntry.TABLE_NAME;
    //endregion
}

























