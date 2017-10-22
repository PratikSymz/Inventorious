package com.pratiksymz.android.myinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.pratiksymz.android.myinventory.data.InventoryContract.ItemsEntry;

/**
 * {@link ContentProvider} for Inventory app.
 */
public class InventoryProvider extends ContentProvider {

    /* Tag for the log messages */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /* Database helper object */
    private InventoryDbHelper mDbHelper;

    /* URI matcher code for the content URI for the inventory items table */
    private static final int ITEMS = 100;

    /* URI matcher code for the content URI for a single item in the inventory items table */
    private static final int ITEM_ID = 101;

    /**
     * Uri matcher object to match a content Uri to the corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.pratiksymz.android.myinventory/items" will
        // map to the integer code {@link #ITEMS}. This URI is used to provide access to MULTIPLE
        // rows of the items table.
        mUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, ITEMS);

        // The content URI of the form "content://com.pratiksymz.android.inventoryapp/items/#" will
        // map to the integer code {@link #ITEM_ID}. This URI is used to provide access to ONE single
        // row of the items table.

        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.pratiksymz.android.inventoryapp/items/3" matches, but
        // "content://com.pratiksymz.android.myinventory/items" (without a number at the end) doesn't match.
        mUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments,
     * and sort order.
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get a readable database
        SQLiteDatabase sqLiteDatabase = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        final int uriMatch = mUriMatcher.match(uri);
        switch (uriMatch) {
            // For the ITMES code, query the items table directly with the given
            // projection, selection, selection arguments, and sort order. The cursor
            // could contain multiple rows of the items table.
            case ITEMS:
                cursor = sqLiteDatabase.query(
                        ItemsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            // For the ITEM_ID code, extract out the ID from the URI.
            // For an example URI such as "content://com.pratiksymz.android.myinventory/items/3",
            // the selection will be "_id=?" and the selection argument will be a
            // String array containing the actual ID of 3 in this case.
            //
            // For every "?" in the selection, we need to have an element in the selection
            // arguments that will fill in the "?". Since we have 1 question mark in the
            // selection, we have 1 String in the selection arguments' String array.
            case ITEM_ID:
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the items table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = sqLiteDatabase.query(
                        ItemsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the cursor,
        // so we know that what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int uriMatch = mUriMatcher.match(uri);
        switch (uriMatch) {
            case ITEM_ID:
                return ItemsEntry.CONTENT_ITEM_TYPE;
            case ITEMS:
                return ItemsEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + uriMatch);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int uriMatch = mUriMatcher.match(uri);
        switch (uriMatch) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert an item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    public Uri insertItem(Uri uri, ContentValues contentValues) {
        // Get writable database
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();

        // Check that the name is not null
        String itemName = contentValues.getAsString(ItemsEntry.COLUMN_ITEM_NAME);
        if (itemName == null) {
            throw new IllegalArgumentException("Item requires a name!");
        }

        // If the price is provided, check that it's greater than or equal to 0
        Integer price = contentValues.getAsInteger(ItemsEntry.COLUMN_ITEM_PRICE);
        // Price can also be taken as null.
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Item requires a valid price!");
        }

        // Check that the supplier name is not null
        String supplierName = contentValues.getAsString(ItemsEntry.COLUMN_ITEM_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Item requires a supplier name!");
        }

        // No need to check the Supplier Email, any value is valid (including null).

        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = contentValues.getAsInteger(ItemsEntry.COLUMN_ITEM_QUANTITY);
        // Quantity can also be taken as null.
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Item requires a valid quantity!");
        }

        // Check that the item description is not null
        String itemDesc = contentValues.getAsString(ItemsEntry.COLUMN_ITEM_DESC);
        if (itemDesc == null) {
            throw new IllegalArgumentException("Item requires a description!");
        }

        // No need to check the location and Image, any value is valid (including null).

        // No need to check for the Date Of Addition because it's automatically updated.

        // Insert the new item with the given values
        long id = sqLiteDatabase.insert(ItemsEntry.TABLE_NAME, null, contentValues);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the item content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Track the number of rows that were deleted
        int rowsDeleted = 0;

        // Get writable database
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
        final int uriMatch = mUriMatcher.match(uri);

        switch (uriMatch) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = sqLiteDatabase.delete(ItemsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = ItemsEntry._ID + "=?";
                // Delete a single row given by the ID in the URI
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = sqLiteDatabase.delete(ItemsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int uriMatch = mUriMatcher.match(uri);
        switch (uriMatch) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);

            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more items).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        }

        // If the {@link ItemsEntry#COLUMN_ITEM_NAME} key is present,
        // check that the name value is not null.
        if (contentValues.containsKey(ItemsEntry.COLUMN_ITEM_NAME)) {
            String itemName = contentValues.getAsString(ItemsEntry.COLUMN_ITEM_NAME);
            if (itemName == null) {
                throw new IllegalArgumentException("Item requires a name!");
            }
        }

        // If the {@link ItemsEntry#COLUMN_ITEM_PRICE} key is present,
        // check that the price value is valid.
        if (contentValues.containsKey(ItemsEntry.COLUMN_ITEM_PRICE)) {
            // Check that the price is greater than or equal to 0 CURRENCY
            Integer price = contentValues.getAsInteger(ItemsEntry.COLUMN_ITEM_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Item requires a valid price");
            }
        }

        // If the {@link ItemsEntry#COLUMN_ITEM_SUPPLIER_NAME} key is present,
        // check that the supplier name value is not null.
        if (contentValues.containsKey(ItemsEntry.COLUMN_ITEM_SUPPLIER_NAME)) {
            String supplierName = contentValues.getAsString(ItemsEntry.COLUMN_ITEM_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Item requires a supplier name!");
            }
        }

        // If the {@link ItemsEntry#COLUMN_ITEM_SUPPLIER_EMAIL} key is present,
        // check that the supplier email value is not null.
        if (contentValues.containsKey(ItemsEntry.COLUMN_ITEM_SUPPLIER_EMAIL)) {
            String supplierEmail = contentValues.getAsString(ItemsEntry.COLUMN_ITEM_SUPPLIER_EMAIL);
            if (supplierEmail == null) {
                throw new IllegalArgumentException("Item requires a supplier email!");
            }
        }

        // If the {@link ItemsEntry#COLUMN_ITEM_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (contentValues.containsKey(ItemsEntry.COLUMN_ITEM_QUANTITY)) {
            // Check that the price is greater than or equal to 0 units
            Integer quantity = contentValues.getAsInteger(ItemsEntry.COLUMN_ITEM_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Item requires a valid quantity");
            }
        }

        // If the {@link ItemsEntry#COLUMN_ITEM_DESC} key is present,
        // check that the description value is not null.
        if (contentValues.containsKey(ItemsEntry.COLUMN_ITEM_DESC)) {
            String itemDesc = contentValues.getAsString(ItemsEntry.COLUMN_ITEM_DESC);
            if (itemDesc == null) {
                throw new IllegalArgumentException("Item requires a description!");
            }
        }

        // No need to check the location and Image, any value is valid (including null).

        // No need to check for the Date Of Addition because it's automatically updated.

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ItemsEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
