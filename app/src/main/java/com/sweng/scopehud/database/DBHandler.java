package com.sweng.scopehud.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sweng.scopehud.util.Scope;
import com.sweng.scopehud.util.ScopeZero;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

public class DBHandler extends SQLiteOpenHelper implements Serializable {

    // Database Name
    private static final String DB_NAME = "scopeDB";

    // Database version
    private static final int DB_VERSION = 1;

    //Column Names
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

    // creating a constructor for our database handler.
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQLITE query to set column names
        // along with their data types.
        String query = "CREATE TABLE " + SCOPE_TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT,"
                + BRAND_COL + " TEXT,"
                + MAXMAG_COL + " REAL,"
                + VARMAG_COL + " INTEGER,"
                + DISTANCE_COL + " INTEGER,"
                + WINDAGE_COL + " REAL,"
                + ELEVATION_COL + " REAL,"
                + DATE_COL + " INTEGER)"; //DATE IS STORED IN UNIX TIME (seconds since  1970-01-01 00:00:00 UTC)

        // execute query
        db.execSQL(query);
    }

    // this method is use to add new product table entry
    public void addNewScope(String scopeName, String scopeBrand, float scopeMaxMagnification,
                            boolean scopeHasVariableMagnification, int zeroDistance,
                            float zeroWindage, float zeroElevation, Date zeroDate) {

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

        db.insert(SCOPE_TABLE_NAME, null, values);

        db.close();
    }

    public ArrayList<Scope> queryAllScopes() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursorScopes = db.rawQuery("SELECT * FROM " + SCOPE_TABLE_NAME, null);

        ArrayList<Scope>  scopeList = new ArrayList<Scope>();

        if (cursorScopes.moveToFirst()) {
            do {
                scopeList.add(new Scope(cursorScopes.getInt(0),
                        cursorScopes.getString(1), cursorScopes.getString(2),
                        cursorScopes.getFloat(3), cursorScopes.getInt(4) == 1,
                        new ScopeZero(cursorScopes.getInt(5), cursorScopes.getFloat(6),
                                cursorScopes.getFloat(7), new Date(cursorScopes.getLong(7)))));
            } while(cursorScopes.moveToNext());
        }

        cursorScopes.close();
        return scopeList;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + SCOPE_TABLE_NAME);
        onCreate(db);
    }
}