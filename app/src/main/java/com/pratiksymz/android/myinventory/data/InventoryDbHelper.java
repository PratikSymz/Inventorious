package com.pratiksymz.android.myinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pratiksymz.android.myinventory.data.InventoryContract.ItemsEntry;

/**
 * Database helper for Inventory app. Manages database creation and version management.
 */
public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    public static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the inventory items table
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + ItemsEntry.TABLE_NAME + " (" +
                ItemsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ItemsEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                ItemsEntry.COLUMN_ITEM_PRICE + " INTEGER NOT NULL DEFAULT 0, " +
                ItemsEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                ItemsEntry.COLUMN_ITEM_DATE_OF_ADDITION + " TEXT NOT NULL, " +
                ItemsEntry.COLUMN_ITEM_SUPPLIER_NAME + " TEXT NOT NULL, " +
                ItemsEntry.COLUMN_ITEM_SUPPLIER_EMAIL + " TEXT NOT NULL, " +
                ItemsEntry.COLUMN_ITEM_IMAGE + " TEXT, " +
                ItemsEntry.COLUMN_ITEM_LOCATION + " TEXT, " +
                ItemsEntry.COLUMN_ITEM_DESC + " TEXT);";

        // Execute the SQL Statement
        db.execSQL(SQL_CREATE_ITEMS_TABLE);
        Log.v(LOG_TAG, "SQL Items Table Created!");
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
