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

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.richard.inventoryapp.data.CakeContract;
import com.example.richard.inventoryapp.data.CakeContract.CakeEntry;

import java.text.DecimalFormat;

/**
 * Displays list of cakes that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the cake data loader */
    private static final int CAKE_LOADER = 0;

    /** Adapter for the ListView */
    CakeCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the cake data
        ListView cakeListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        cakeListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of cake data in the Cursor.
        // There is no cake data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new CakeCursorAdapter(this, null);
        cakeListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        cakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific cake that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link CakeEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.richard.inventoryapp/cakes/2"
                // if the cake with ID 2 was clicked on.
                Uri currentUri = ContentUris.withAppendedId(CakeEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentUri);

                // Launch the {@link EditorActivity} to display the data for the current cake.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(CAKE_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded cake data into the database. For debugging purposes only.
     */
    private void insertCake() {

        DecimalFormat df = new DecimalFormat("#.00");
        String price = df.format(7.9594);

        // Create a ContentValues object where column names are the keys,
        // and Toto's cake attributes are the values.
        ContentValues values = new ContentValues();
        values.put(CakeEntry.COLUMN_CAKE_NAME, "Racing Car");
        values.put(CakeContract.CakeEntry.COLUMN_CAKE_QUANTITY, 10);
        values.put(CakeEntry.COLUMN_CAKE_OCCASION, CakeEntry.OCCASION_BIRTHDAY);
        values.put(CakeContract.CakeEntry.COLUMN_CAKE_PRICE, price);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link CakeEntry#CONTENT_URI} to indicate that we want to insert
        // into the cakes database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(CakeEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all cakes in the database.
     */
    private void deleteAllCakes() {
        int rowsDeleted = getContentResolver().delete(CakeEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from cake database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertCake();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllCakes();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                CakeEntry._ID,
                CakeEntry.COLUMN_CAKE_NAME,
                CakeEntry.COLUMN_CAKE_QUANTITY,
                CakeEntry.COLUMN_CAKE_OCCASION,
                CakeEntry.COLUMN_CAKE_PRICE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                CakeEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link CakeCursorAdapter} with this new cursor containing updated cake data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
