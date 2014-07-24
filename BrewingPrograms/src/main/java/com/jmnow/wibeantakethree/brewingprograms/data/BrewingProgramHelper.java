package com.jmnow.wibeantakethree.brewingprograms.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by John-Michael on 7/10/2014.
 */
public class BrewingProgramHelper extends SQLiteOpenHelper {

    // DB NAME
    public static final String DATABASE_NAME = "brewing_programs";
    public static final String COLUMN_ID = "_rowid_";
    public static final String COLUMN_ID_ALIASED = "_rowid_ AS _id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_ON_ONE = "on_one";
    public static final String COLUMN_OFF_ONE = "off_one";
    public static final String COLUMN_ON_TWO = "on_two";
    public static final String COLUMN_OFF_TWO = "off_two";
    public static final String COLUMN_ON_THREE = "on_three";
    public static final String COLUMN_OFF_THREE = "off_three";
    public static final String COLUMN_ON_FOUR = "on_four";
    public static final String COLUMN_OFF_FOUR = "off_four";
    public static final String COLUMN_ON_FIVE = "on_five";
    public static final String COLUMN_OFF_FIVE = "off_five";
    public static final String COLUMN_ORIGINAL_AUTHOR = "original_author";
    public static final String COLUMN_SHORT_URL = "short_url";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_MODIFIED_AT = "modified_at";

    // DB Version
    private static final int DATABASE_VERSION = 1;

    public BrewingProgramHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL for creating table
        // WE LEAVE OFF AN ID COLUMN AND USE THE SQLITE FEATURE
        // _rowid_ for this purpose!!
        String CREATE_BREWINGPROGRAM_TABLE = "CREATE TABLE " + DATABASE_NAME + " ( " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_ON_ONE + " INTEGER, " +
                COLUMN_OFF_ONE + " INTEGER, " +
                COLUMN_ON_TWO + " INTEGER, " +
                COLUMN_OFF_TWO + " INTEGER, " +
                COLUMN_ON_THREE + " INTEGER, " +
                COLUMN_OFF_THREE + " INTEGER, " +
                COLUMN_ON_FOUR + " INTEGER, " +
                COLUMN_OFF_FOUR + " INTEGER, " +
                COLUMN_ON_FIVE + " INTEGER, " +
                COLUMN_OFF_FIVE + " INTEGER, " +
                COLUMN_ORIGINAL_AUTHOR + " TEXT DEFAULT \"\", " +
                COLUMN_SHORT_URL + " TEXT DEFAULT \"\", " +
                COLUMN_CREATED_AT + " TEXT DEFAULT current_timestamp, " +
                COLUMN_MODIFIED_AT + " TEXT DEFAULT current_timestamp ) ";


        // create brewing programs table
        db.execSQL(CREATE_BREWINGPROGRAM_TABLE);
        // add in some data
        createHelper(db, "Starter Program", "A basic program", 70, 0, 0, 0);
        createHelper(db, "Level One", "Some customization", 20, 20, 50, 0);
        createHelper(db, "Heavy Delay", "Allows a great steep time", 15, 50, 60, 0);
        createHelper(db, "Starter Program", "A basic program", 70, 0, 0, 0);
        createHelper(db, "Level One", "Some customization", 20, 20, 50, 0);
        createHelper(db, "Heavy Delay", "Allows a great steep time", 15, 50, 60, 0);
        createHelper(db, "Starter Program", "A basic program", 70, 0, 0, 0);
        createHelper(db, "Level One", "Some customization", 20, 20, 50, 0);
        createHelper(db, "Heavy Delay", "Allows a great steep time", 15, 50, 60, 0);
        createHelper(db, "Starter Program", "A basic program", 70, 0, 0, 0);
        createHelper(db, "Level One", "Some customization", 20, 20, 50, 0);
        createHelper(db, "Heavy Delay", "Allows a great steep time", 15, 50, 60, 0);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // simple upgrade script which destroys old schema and initializes
        // new schema.
        // TODO: Make non-destructive upgrade script.

        // drop old if exists
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        // create the new version
        this.onCreate(db);
    }

    private void createHelper(SQLiteDatabase db, String name, String description, int onOne, int offOne, int onTwo, int offTwo) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_ON_ONE, onOne);
        values.put(COLUMN_OFF_ONE, offOne);
        values.put(COLUMN_ON_TWO, onTwo);
        values.put(COLUMN_OFF_TWO, offTwo);
        db.insert(DATABASE_NAME, null, values);
    }
}
