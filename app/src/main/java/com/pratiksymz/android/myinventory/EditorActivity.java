package com.pratiksymz.android.myinventory;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pratiksymz.android.myinventory.data.InventoryContract.ItemsEntry;
import com.pratiksymz.android.myinventory.location.MapsLocationActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.pratiksymz.android.myinventory.data.InventoryContract.ItemsEntry.CONTENT_URI;
import static java.lang.Integer.parseInt;

/**
 * Allows user to create a new item or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Class LOG_TAG
     */
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * Identifier for the item data loader
     */
    private static final int EXISTING_ITEM_LOADER = 0;

    /**
     * Content URI for the existing item (null if it's a new item)
     */
    private Uri mCurrentItemUri;

    /**
     * EditText field to enter the item's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the item's description
     */
    private EditText mDescEditText;

    /**
     * EditText field to enter the item's quantity
     */
    private EditText mQuantityEditText;

    /**
     * ImageButton field to increase the item's quantity
     */
    private ImageButton mAddQuantityButton;

    /**
     * ImageButton field to decrease the item's quantity
     */
    private ImageButton mSubtractQuantityButton;

    /**
     * EditText field to enter the item's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the item's supplier name
     */
    private EditText mSupplierNameEditText;

    /**
     * EditText field to enter the item's supplier email
     */
    private EditText mSupplierEmailEditText;

    /**
     * TextView field to show the item's date of addition
     */
    private TextView mDateOfAddition;

    /**
     * String field to save the item's date of addition
     */
    private String itemDateOfAddition;

    /**
     * EditText field to show/edit the item's location/origin
     */
    private EditText mItemLocation;

    /**
     * ImageButton field to retrieve the current location/origin
     */
    private ImageButton mAddItemLocationButton;

    /**
     * String field to save the item's location (origin)
     */
    private String itemLocationAddress;

    /**
     * Integer field to store the LOCATION_REQUEST constant
     */
    private static final int LOCATION_REQUEST = 1;

    /**
     * Button to order the item saved by sending an email to the supplier
     */
    private Button mOrderItem;

    /**
     * ImageView field to show the item's image
     */
    private ImageView mItemImage;

    /**
     * ImageButton field to add item image
     */
    private ImageButton mAddImageButton;

    /**
     * View field to display an overlay over the image
     */
    private View mImageFadeView;

    /**
     * Integer field to store the GALLERY_REQUEST constant
     */
    private static final int PICTURE_GALLERY_REQUEST = 2;

    /**
     * String field to save the item's image path
     */
    private String picturePath;

    /**
     * Bitmap field to save the item's image
     */
    private Bitmap picture;

    /**
     * Identifier for the image URI loader
     */
    private static final String STATE_PICTURE_URI = "STATE_PICTURE_URI";

    /**
     * Uri field to save the item's image uri
     */
    private Uri pictureUri;

    /**
     * Boolean flag that keeps track of whether the item has been edited (true) or not (false)
     */
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // If the intent DOES NOT contain an item content URI, then we know that we are
        // creating a new item.
        if (mCurrentItemUri == null) {
            // This is a new item, so change the app bar to say "Add an Item"
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit Item"
            setTitle(getString(R.string.editor_activity_title_edit_item));

            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mDescEditText = (EditText) findViewById(R.id.edit_item_description);

        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mAddQuantityButton = (ImageButton) findViewById(R.id.item_add_quantity);
        mSubtractQuantityButton = (ImageButton) findViewById(R.id.item_subtract_quantity);

        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);

        mAddImageButton = (ImageButton) findViewById(R.id.add_image_action_button);
        mItemImage = (ImageView) findViewById(R.id.image_view_item_image);
        mImageFadeView = findViewById(R.id.view_fade_image);

        mSupplierNameEditText = (EditText) findViewById(R.id.edit_item_supplier_name);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_item_supplier_email);
        mDateOfAddition = (TextView) findViewById(R.id.text_view_item_date);

        mItemLocation = (EditText) findViewById(R.id.item_add_location_edit_text);
        mAddItemLocationButton = (ImageButton) findViewById(R.id.item_add_location_button);

        mOrderItem = (Button) findViewById(R.id.order_item_button);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mAddImageButton.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mItemLocation.setOnTouchListener(mTouchListener);
        mAddItemLocationButton.setOnTouchListener(mTouchListener);

        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to open the image directory
                Intent openPictureGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                // Initialize a File type object which is of Picture Directory Environment
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                // Get the picture directory path from the File type object
                String pictureDirectoryPath = pictureDirectory.getPath();
                // Parse the path to obtain a URI
                Uri data = Uri.parse(pictureDirectoryPath);
                openPictureGallery.setDataAndType(data, "image/*");
                startActivityForResult(openPictureGallery, PICTURE_GALLERY_REQUEST);
            }
        });

        /* Quantity Modification */
        // Set initial value of quantity
        mQuantityEditText.setText("1");

        // Add an OnClick listener on the ADD button to increase the value of quantity
        mAddQuantityButton.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                int currentQuantity = parseInt(mQuantityEditText.getText().toString().trim());
                currentQuantity++;
                mQuantityEditText.setText(String.valueOf(currentQuantity));
            }
        });

        // Add an OnClick listener on the SUBTRACT button to decrease the value of quantity and check
        // that the value of quantity dosen't go below 0
        mSubtractQuantityButton.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                String currentProduct = mNameEditText.getText().toString();
                String toastMessage;
                int currentQuantity = parseInt(mQuantityEditText.getText().toString());
                if (currentQuantity > 1) {
                    currentQuantity--;
                    mQuantityEditText.setText(String.valueOf(currentQuantity));
                } else {
                    if (currentProduct != null && !currentProduct.isEmpty()) {
                        toastMessage = "You can't reduce the stock of " + currentProduct + " to 0!";
                    } else {
                        toastMessage = "You can't reduce the stock of the product to 0!";
                    }

                    mQuantityEditText.setText(String.valueOf(currentQuantity));
                    Toast.makeText(v.getContext(), toastMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });


        /* DATE EXTRACTION */
        // Get the date of addition of the item, i.e., the current date
        Calendar calender = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        itemDateOfAddition = dateFormat.format(calender.getTime());
        mDateOfAddition.setText(itemDateOfAddition);


        /* LOCATION EXTRACTION */
        mAddItemLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(EditorActivity.this, MapsLocationActivity.class);
                startActivityForResult(mapIntent, LOCATION_REQUEST);
            }
        });

        /* Order Item by sending an email to Supplier */
        mOrderItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String orderQuantity = mQuantityEditText.getText().toString().trim();
                String productName = mNameEditText.getText().toString().trim();
                String supplierEmail = mSupplierEmailEditText.getText().toString().trim();

                if (orderQuantity.length() != 0 && !TextUtils.isEmpty(productName) &&
                        !TextUtils.isEmpty(supplierEmail)) {
                    String emailAddress = "mailto:" + supplierEmail;
                    String subjectHeader = "Order For: " + productName;
                    String orderMessage = "Please send " + orderQuantity + " units of " + productName + ". "
                            + " \n\n" + "Thank you.";

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse(emailAddress));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subjectHeader);
                    emailIntent.putExtra(Intent.EXTRA_TEXT, orderMessage);
                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(emailIntent);
                    }
                } else {
                    String toastMessage = getString(R.string.editor_activity_give_order_information);
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Get user input from editor and save item into database.
     */
    private void saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String descString = mDescEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();
        String dateString = itemDateOfAddition;
        String locationString = mItemLocation.getText().toString().trim();

        // Check if this is supposed to be a new item
        // and check if all the fields in the editor are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(descString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(supplierEmailString) &&
                TextUtils.isEmpty(dateString) && TextUtils.isEmpty(locationString) &&
                TextUtils.isEmpty(picturePath)) {
            // Since no fields were modified, we can return early without creating a new item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        if (nameString.isEmpty() || descString.isEmpty() || quantityString.isEmpty() ||
                priceString.isEmpty() || supplierNameString.isEmpty() || supplierEmailString.isEmpty()) {
            Toast.makeText(this, R.string.editor_activity_give_all_the_information, Toast.LENGTH_SHORT).show();
            return;
        }

        // Construct a picture path from the picture URI and add it to the content values
        if (pictureUri != null) {
            picturePath = pictureUri.toString().trim();
        } else {
            picturePath = Uri.parse(
                    "android.resource://" + "com.pratiksymz.android.myinventory" + "/" + R.drawable.gradient_drawable
            ).toString().trim();
        }

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ItemsEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemsEntry.COLUMN_ITEM_DESC, descString);

        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ItemsEntry.COLUMN_ITEM_QUANTITY, quantity);

        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ItemsEntry.COLUMN_ITEM_PRICE, price);

        values.put(ItemsEntry.COLUMN_ITEM_IMAGE, picturePath);
        values.put(ItemsEntry.COLUMN_ITEM_SUPPLIER_NAME, supplierNameString);
        values.put(ItemsEntry.COLUMN_ITEM_SUPPLIER_EMAIL, supplierEmailString);

        if (dateString != null) {
            values.put(ItemsEntry.COLUMN_ITEM_DATE_OF_ADDITION, dateString);
        }

        values.put(ItemsEntry.COLUMN_ITEM_LOCATION, locationString);

        // Determine if this is a new or existing item by checking if mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {
            // This is a NEW item, so insert a new item into the provider,
            // returning the content URI for the new item.
            Uri newUri = getContentResolver().insert(CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Helper method which loads the item's image upon completion of the loading of the activity.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        // Checking if the request code and result code for the PICTURE_GALLERY_REQUEST match our request
        if (requestCode == PICTURE_GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                try {
                    // This is the address of the image on the sd card
                    pictureUri = resultData.getData();
                    int takeFlags = resultData.getFlags();
                    takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    picturePath = pictureUri.toString();
                    // Declare a stream to read the data from the card
                    InputStream inputStream;
                    // We are getting an input stream based on the Uri of the image
                    inputStream = getContentResolver().openInputStream(pictureUri);
                    // Get a bitmap from the stream
                    picture = BitmapFactory.decodeStream(inputStream);
                    // Show the image to the user
                    mImageFadeView.setVisibility(View.INVISIBLE);
                    mItemImage.setImageBitmap(picture);
                    picturePath = pictureUri.toString();
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            getContentResolver().takePersistableUriPermission(pictureUri, takeFlags);
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Show the user a Toast message that the Image is not available
                    Toast.makeText(this, getString(R.string.editor_activity_unable_to_open_image),
                            Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == LOCATION_REQUEST && resultCode == RESULT_OK) {
            if (resultData != null) {
                try {
                    // Retrieve location address from resultData
                    itemLocationAddress = resultData.getStringExtra("currentAddress");
                    mItemLocation.setText(itemLocationAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Show the user a Toast message that the Image is not available
                    Toast.makeText(this, "Unable to retrieve address",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Helper method which is used to obtain the image from the image uri in Bitmap format.
     */
    public Bitmap getBitmapFromUri(Uri uri) {
        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mItemImage.getWidth();
        int targetH = mItemImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            if (input != null)
                input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            Bitmap.createScaledBitmap(bitmap, targetH, targetW, false);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fnfe) {
            Log.e(LOG_TAG, "Image not found!", fnfe);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image!", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {
                // Error Handled
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (pictureUri != null)
            outState.putString(STATE_PICTURE_URI, pictureUri.toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_PICTURE_URI) &&
                !savedInstanceState.getString(STATE_PICTURE_URI).equals("")) {
            pictureUri = Uri.parse(savedInstanceState.getString(STATE_PICTURE_URI));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                saveItem();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the item table
        String[] projection = {
                ItemsEntry._ID,
                ItemsEntry.COLUMN_ITEM_NAME,
                ItemsEntry.COLUMN_ITEM_DESC,
                ItemsEntry.COLUMN_ITEM_QUANTITY,
                ItemsEntry.COLUMN_ITEM_IMAGE,
                ItemsEntry.COLUMN_ITEM_PRICE,
                ItemsEntry.COLUMN_ITEM_SUPPLIER_NAME,
                ItemsEntry.COLUMN_ITEM_SUPPLIER_EMAIL,
                ItemsEntry.COLUMN_ITEM_DATE_OF_ADDITION,
                ItemsEntry.COLUMN_ITEM_LOCATION
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,        // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        ViewTreeObserver viewTreeObserver = mItemImage.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mItemImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mItemImage.setImageBitmap(getBitmapFromUri(pictureUri));
                }
            }
        });

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Finding the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_DESC);
            int quantityColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_IMAGE);
            int priceColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_SUPPLIER_EMAIL);
            int dateColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_DATE_OF_ADDITION);
            int locationColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_LOCATION);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String pictureStringUri = cursor.getString(imageColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            String date = cursor.getString(dateColumnIndex);
            String location = cursor.getString(locationColumnIndex);
            Uri pictureUriData = Uri.parse(pictureStringUri);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mDescEditText.setText(description);
            mQuantityEditText.setText(String.valueOf(quantity));
            mPriceEditText.setText(String.valueOf(price));
            mSupplierNameEditText.setText(supplierName);
            mSupplierEmailEditText.setText(supplierEmail);
            mDateOfAddition.setText(date);
            mItemLocation.setText(location);
            pictureUri = pictureUriData;
            if (pictureUri.toString().contains("drawable"))
                mItemImage.setImageURI(pictureUri);
            else {
                Bitmap bitmap = getBitmapFromUri(pictureUri);
                mItemImage.setImageBitmap(bitmap);

            }
            mImageFadeView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDescEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mItemImage.setImageResource(R.drawable.add_image);
        mSupplierNameEditText.setText("");
        mSupplierEmailEditText.setText("");
        mDateOfAddition.setText(itemDateOfAddition);
        mItemLocation.setText(itemLocationAddress);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}