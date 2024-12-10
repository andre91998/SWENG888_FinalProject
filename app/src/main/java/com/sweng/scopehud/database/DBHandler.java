package com.sweng.scopehud.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import com.sweng.scopehud.util.Scope;
import com.sweng.scopehud.util.ScopeZero;
import java.util.ArrayList;
import java.util.Date;

public class DBHandler extends SQLiteOpenHelper {

    // Database constants
    private static final String DB_NAME = "scopeDB";
    private static final int DB_VERSION = 3;

    // Table and column names for Scopes
    private static final String SCOPE_TABLE_NAME = "scopes";
    private static final String ID_COL = "id";
    private static final String NAME_COL = "name";
    private static final String BRAND_COL = "brand";
    private static final String MAXMAG_COL = "maxmagnification";
    private static final String VARMAG_COL = "variablemagnification";
    private static final String DISTANCE_COL = "zerodistance";
    private static final String WINDAGE_COL = "zerowindage";
    private static final String ELEVATION_COL = "zeroelevation";
    private static final String DATE_COL = "zerodate";
    private static final String LOCATION_LAT_COL = "locationLatitude";
    private static final String LOCATION_LONG_COL = "locationLongitude";
    private static final String TOWN_COL = "town";
    private static final String STATE_COL = "state";

    // Table and column names for User Settings
    private static final String USER_SETTINGS_TABLE_NAME = "user_settings";
    private static final String USER_ID_COL = "user_id";
    private static final String USERNAME_COL = "username";
    private static final String ADDRESS_COL = "address";
    private static final String CITY_COL = "city";
    //private static final String STATE_COL = "state";
    private static final String COUNTRY_COL = "country";
    private static final String PROFILE_IMAGE_URI_COL = "profile_image_uri";
    private static final String PROFILE_IMAGE_COL = "profile_image";

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Scopes table
        String createScopesTable = "CREATE TABLE " + SCOPE_TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT,"
                + BRAND_COL + " TEXT,"
                + MAXMAG_COL + " REAL,"
                + VARMAG_COL + " INTEGER,"
                + DISTANCE_COL + " INTEGER,"
                + WINDAGE_COL + " REAL,"
                + ELEVATION_COL + " REAL,"
                + DATE_COL + " INTEGER," //DATE IS STORED IN UNIX TIME (seconds since  1970-01-01 00:00:00 UTC)
                + LOCATION_LAT_COL + " REAL,"
                + LOCATION_LONG_COL + " REAL,"
                + TOWN_COL + " TEXT, "
                + STATE_COL + " TEXT )";
        db.execSQL(createScopesTable);

        // Create User Settings table
        String createUserSettingsTable = "CREATE TABLE " + USER_SETTINGS_TABLE_NAME + " ("
                + USER_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USERNAME_COL + " TEXT, "
                + ADDRESS_COL + " TEXT, "
                + CITY_COL + " TEXT, "
                + STATE_COL + " TEXT, "
                + COUNTRY_COL + " TEXT, "
                + PROFILE_IMAGE_COL + " BLOB)";
        db.execSQL(createUserSettingsTable);
    }

    /**
     * This method is use to add new product table entry
     * @param scopeName
     * @param scopeBrand
     * @param scopeMaxMagnification
     * @param scopeHasVariableMagnification
     * @param zeroDistance
     * @param zeroWindage
     * @param zeroElevation
     * @param zeroDate
     * @param latitude
     * @param longitude
     * @param town
     * @param state
     */
    public void addNewScope(String scopeName, String scopeBrand, float scopeMaxMagnification,
                            boolean scopeHasVariableMagnification, int zeroDistance,
                            float zeroWindage, float zeroElevation, Date zeroDate, double latitude,
                            double longitude, String town, String state) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(NAME_COL, scopeName);
        values.put(BRAND_COL, scopeBrand);
        values.put(MAXMAG_COL, scopeMaxMagnification);
        values.put(VARMAG_COL, scopeHasVariableMagnification ? 1 : 0);
        values.put(DISTANCE_COL, zeroDistance);
        values.put(WINDAGE_COL, zeroWindage);
        values.put(ELEVATION_COL, zeroElevation);
        values.put(DATE_COL, zeroDate.getTime());
        values.put(LOCATION_LAT_COL, latitude);
        values.put(LOCATION_LONG_COL, longitude);
        values.put(TOWN_COL, town);
        values.put(STATE_COL, state);


        db.insert(SCOPE_TABLE_NAME, null, values);

        db.close();
    }

    /**
     * Guarantee that the tables are properly initialized with the write columns based on the current
     * app version
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + SCOPE_TABLE_NAME + " ADD COLUMN " + TOWN_COL + " TEXT");
            db.execSQL("ALTER TABLE " + SCOPE_TABLE_NAME + " ADD COLUMN " + STATE_COL + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + USER_SETTINGS_TABLE_NAME + " ADD COLUMN" + PROFILE_IMAGE_COL + " BLOB");
        }
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + SCOPE_TABLE_NAME + " ADD COLUMN " + LOCATION_LAT_COL + " REAL");
            db.execSQL("ALTER TABLE " + SCOPE_TABLE_NAME + " ADD COLUMN " + LOCATION_LONG_COL + " REAL");
        }
        db.execSQL("DROP TABLE IF EXISTS " + SCOPE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + USER_SETTINGS_TABLE_NAME);
        onCreate(db);
    }

    /**
     * Method to insert or update user settings
     * @param userId
     * @param username
     * @param address
     * @param city
     * @param state
     * @param country
     * @param profileImage
     */
    public void upsertUserSettings(int userId, String username, String address, String city, String state, String country, byte[] profileImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME_COL, username);
        values.put(ADDRESS_COL, address);
        values.put(CITY_COL, city);
        values.put(STATE_COL, state);
        values.put(COUNTRY_COL, country);
        values.put(PROFILE_IMAGE_COL, profileImage);

        // Check if user settings exist
        Cursor cursor = db.query(USER_SETTINGS_TABLE_NAME, null, USER_ID_COL + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            // Update existing record
            db.update(USER_SETTINGS_TABLE_NAME, values, USER_ID_COL + "=?", new String[]{String.valueOf(userId)});
        } else {
            // Insert new record
            values.put(USER_ID_COL, userId); // Ensure the user ID is included for a new record
            db.insert(USER_SETTINGS_TABLE_NAME, null, values);
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }

    /**
     * Method to get user settings
     * @param userId
     * @return
     */
    public Cursor getUserSettings(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(USER_SETTINGS_TABLE_NAME, null, USER_ID_COL + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);
    }

    /**
     Method to update user settings     * @param userId
     * @param username
     * @param address
     * @param city
     * @param state
     * @param country
     * @param profileImage
     */
    public void updateUserSettings(int userId, String username, String address, String city, String state, String country, byte[] profileImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME_COL, username);
        values.put(ADDRESS_COL, address);
        values.put(CITY_COL, city);
        values.put(STATE_COL, state);
        values.put(COUNTRY_COL, country);
        values.put(PROFILE_IMAGE_COL, profileImage);

        db.update(USER_SETTINGS_TABLE_NAME, values, USER_ID_COL + "=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    /**
     * Method to query all scopes from the database
     * @return
     */
    public ArrayList<Scope> queryAllScopes() {
        ArrayList<Scope> scopeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SCOPE_TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                Location location = new Location("");
                location.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(LOCATION_LAT_COL)));
                location.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(LOCATION_LAT_COL)));
                Scope scope = new Scope(
                        cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(BRAND_COL)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(MAXMAG_COL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(VARMAG_COL)) == 1,
                        new ScopeZero(
                                cursor.getInt(cursor.getColumnIndexOrThrow(DISTANCE_COL)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(WINDAGE_COL)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(ELEVATION_COL)),
                                new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DATE_COL))),
                                location
                        ),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(LOCATION_LAT_COL)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(LOCATION_LONG_COL))

                );
                scopeList.add(scope);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return scopeList;
    }

    /**
     Additional method for deleting user settings if needed     * @param userId
     */
    public void deleteUserSettings(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USER_SETTINGS_TABLE_NAME, USER_ID_COL + "=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    /**
     * Method for removing a scope from the DB
     * @param scopeId
     */
    public void deleteScope(int scopeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SCOPE_TABLE_NAME, ID_COL + "=?", new String[]{String.valueOf(scopeId)});
        db.close();
    }
}
