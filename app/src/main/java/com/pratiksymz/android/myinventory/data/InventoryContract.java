package com.pratiksymz.android.myinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */
public class InventoryContract {

    /**
     * To prevent someone from accidentally instantiating the contract class,
     * give it an empty constructor.
     */
    private InventoryContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.pratiksymz.android.myinventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.pratiksymz.android.inventoryapp/items/ is a valid path for
     * looking at inventory items data. content://com.pratiksymz.android.inventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_ITEMS = "items";

    /**
     * Inner class that defines constant values for the Inventory Items database table.
     * Each entry in the table represents a single item in the inventory.
     */
    public static abstract class ItemsEntry implements BaseColumns {
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The content URI to access the inventory items data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        /**
         * Name of database table for inventory items
         */
        public static final String TABLE_NAME = "items";

        /**
         * Unique ID number for the item (only for use in the database table).
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of the item.
         * Type: TEXT
         */
        public static final String COLUMN_ITEM_NAME = "name";

        /**
         * Price of the item.
         * Type: INTEGER
         */
        public static final String COLUMN_ITEM_PRICE = "price";

        /**
         * Description of the item.
         * Type: TEXT
         */
        public static final String COLUMN_ITEM_DESC = "description";

        /**
         * Supplier Name of the item.
         * Type: TEXT
         */
        public static final String COLUMN_ITEM_SUPPLIER_NAME = "supplier_name";

        /**
         * Supplier Email of the item.
         * Type: TEXT
         */
        public static final String COLUMN_ITEM_SUPPLIER_EMAIL = "supplier_email";

        /**
         * Quantity of the item.
         * Type: INTEGER
         */
        public static final String COLUMN_ITEM_QUANTITY = "quantity";

        /**
         * Date of Addition of the item.
         * Type: INTEGER
         */
        public static final String COLUMN_ITEM_DATE_OF_ADDITION = "date_added";

        /**
         * location/Origin of the item.
         * Type: TEXT
         */
        public static final String COLUMN_ITEM_LOCATION = "location";

        /**
         * Image ID of the item.
         * Type: INTEGER
         */
        public static final String COLUMN_ITEM_IMAGE = "image";

    }
}
