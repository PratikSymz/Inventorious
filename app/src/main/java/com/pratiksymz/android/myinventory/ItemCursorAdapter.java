package com.pratiksymz.android.myinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.pratiksymz.android.myinventory.data.InventoryContract.ItemsEntry;

import static com.pratiksymz.android.myinventory.data.InventoryContract.ItemsEntry.CONTENT_URI;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the {@link Cursor}.
 */
public class ItemCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context The context
     * @param cursor  The cursor from which to get the data.
     */
    public ItemCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return The newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context App context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.item_name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.item_quantity);
        TextView dateTextView = (TextView) view.findViewById(R.id.item_date_added);
        TextView priceTextView = (TextView) view.findViewById(R.id.item_price);
        Button itemSellButton = (Button) view.findViewById(R.id.item_sell_button);

        // Find the columns of item attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(ItemsEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_PRICE);
        int dateColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_DATE_OF_ADDITION);

        // Read the item attributes from the Cursor for the current item
        final int itemId = cursor.getInt(idColumnIndex);
        String itemName = cursor.getString(nameColumnIndex);
        final int itemQuantity = cursor.getInt(quantityColumnIndex);
        int itemPrice = cursor.getInt(priceColumnIndex);
        String itemDateOfAddition = cursor.getString(dateColumnIndex);

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(itemName);
        quantityTextView.setText(String.valueOf(itemQuantity));
        priceTextView.setText("$" + String.valueOf(itemPrice));
        dateTextView.setText(itemDateOfAddition);

        // Set up the SELL button to do the appropriate action
        itemSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemQuantity > 1) {
                    int tempQuantity = itemQuantity;
                    tempQuantity--;

                    ContentValues values = new ContentValues();
                    values.put(ItemsEntry.COLUMN_ITEM_QUANTITY, tempQuantity);
                    Uri currentItemUri = ContentUris.withAppendedId(CONTENT_URI, itemId);

                    // Create the new uri of the current product
                    context.getContentResolver().update(currentItemUri, values, null, null);
                } else {
                    String toastMessage = "You can't reduce the stock of the product to 0!";
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}