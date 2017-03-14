/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.richard.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.richard.inventoryapp.data.CakeContract;
import com.example.richard.inventoryapp.data.CakeContract.CakeEntry;

/**
 * Allows user to create a new cake or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the cake data loader */
    private static final int EXISTING_CAKE_LOADER = 0;

    /** Content URI for the existing cake (null if it's a new cake) */
    private Uri mCurrentCakeUri;

    /** EditText field to enter the cake's name */
    private EditText mNameEditText;

    /** EditText field to enter the cake's quantity */
    private EditText mQuantityEditText;

    /** EditText field to enter the cake's price */
    private EditText mPriceEditText;

    /** EditText field to enter the cake's type */
    private Spinner mOccasionSpinner;

    /**
     * Type of the cake. The possible valid values are in the CakeContract.java file:
     * {@link CakeEntry#OCCASION_UNKNOWN}, {@link CakeEntry#OCCASION_BIRTHDAY} or {@link CakeEntry#OCCASION_WEDDING}.
     */
    private int mType = CakeContract.CakeEntry.OCCASION_UNKNOWN;

    /** Boolean flag that keeps track of whether the cake has been edited (true) or not (false) */
    private boolean mCakeHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mCakeHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCakeHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new cake or editing an existing one.
        Intent intent = getIntent();
        mCurrentCakeUri = intent.getData();

        // If the intent DOES NOT contain a cake content URI, then we know that we are
        // creating a new cake.
        if (mCurrentCakeUri == null) {
            // This is a new cake, so change the app bar to say "Add a Cake"
            setTitle(getString(R.string.editor_activity_title_new_cake));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a cake that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing cake, so change app bar to say "Edit Cake"
            setTitle(getString(R.string.editor_activity_title_edit_cake));

            // Initialize a loader to read the cake data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_CAKE_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_cake_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_cake_breed);
        mPriceEditText = (EditText) findViewById(R.id.edit_cake_weight);
        mOccasionSpinner = (Spinner) findViewById(R.id.spinner_cake_occasion);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mOccasionSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of the cake.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mOccasionSpinner.setAdapter(typeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mOccasionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.occasion_birthday))) {
                        mType = CakeEntry.OCCASION_BIRTHDAY;
                    } else if (selection.equals(getString(R.string.occasion_wedding))) {
                        mType = CakeEntry.OCCASION_WEDDING;
                    } else {
                        mType = CakeEntry.OCCASION_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = CakeContract.CakeEntry.OCCASION_UNKNOWN;
            }
        });
    }

    /**
     * Get user input from editor and save cake into database.
     */
    private void saveCake() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mQuantityEditText.getText().toString().trim();
        String weightString = mPriceEditText.getText().toString().trim();

        // Check if this is supposed to be a new cake
        // and check if all the fields in the editor are blank
        if (mCurrentCakeUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) &&
                TextUtils.isEmpty(weightString) && mType == CakeContract.CakeEntry.OCCASION_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new cake.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and cake attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(CakeContract.CakeEntry.COLUMN_CAKE_NAME, nameString);
        values.put(CakeContract.CakeEntry.COLUMN_CAKE_QUANTITY, breedString);
        values.put(CakeContract.CakeEntry.COLUMN_CAKE_OCCASION, mType);
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int weight = 0;
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }
        values.put(CakeContract.CakeEntry.COLUMN_CAKE_PRICE, weight);

        // Determine if this is a new or existing cake by checking if mCurrentCakeUri is null or not
        if (mCurrentCakeUri == null) {
            // This is a NEW cake, so insert a new cake into the provider,
            // returning the content URI for the new cake.
            Uri newUri = getContentResolver().insert(CakeContract.CakeEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_cake_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_cake_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING cake, so update the cake with content URI: mCurrentCakeUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentCakeUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentCakeUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_cake_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_cake_successful),
                        Toast.LENGTH_SHORT).show();
            }
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
        // If this is a new cake, hide the "Delete" menu item.
        if (mCurrentCakeUri == null) {
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
                // Save cake to database
                saveCake();
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
                // If the cake hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mCakeHasChanged) {
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
        // If the cake hasn't changed, continue with handling back button press
        if (!mCakeHasChanged) {
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all cake attributes, define a projection that contains
        // all columns from the cake table
        String[] projection = {
                CakeEntry._ID,
                CakeEntry.COLUMN_CAKE_NAME,
                CakeEntry.COLUMN_CAKE_QUANTITY,
                CakeContract.CakeEntry.COLUMN_CAKE_OCCASION,
                CakeContract.CakeEntry.COLUMN_CAKE_PRICE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentCakeUri,         // Query the content URI for the current cake
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

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of cake attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(CakeContract.CakeEntry.COLUMN_CAKE_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(CakeContract.CakeEntry.COLUMN_CAKE_QUANTITY);
            int typeColumnIndex = cursor.getColumnIndex(CakeContract.CakeEntry.COLUMN_CAKE_OCCASION);
            int priceColumnIndex = cursor.getColumnIndex(CakeContract.CakeEntry.COLUMN_CAKE_PRICE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            int price = cursor.getInt(priceColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(quantity);
            mPriceEditText.setText(Integer.toString(price));

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (type) {
                case CakeContract.CakeEntry.OCCASION_BIRTHDAY:
                    mOccasionSpinner.setSelection(1);
                    break;
                case CakeContract.CakeEntry.OCCASION_WEDDING:
                    mOccasionSpinner.setSelection(2);
                    break;
                default:
                    mOccasionSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mOccasionSpinner.setSelection(0); // Select "Unknown" type
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
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the cake.
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
     * Prompt the user to confirm that they want to delete this cake.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the cake.
                deleteCake();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the cake.
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
     * Perform the deletion of the cake in the database.
     */
    private void deleteCake() {
        // Only perform the delete if this is an existing cake.
        if (mCurrentCakeUri != null) {
            // Call the ContentResolver to delete the cake at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentCakeUri
            // content URI already identifies the cake that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentCakeUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_cake_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_cake_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}