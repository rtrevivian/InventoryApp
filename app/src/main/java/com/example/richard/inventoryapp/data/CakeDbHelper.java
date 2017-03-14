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
package com.example.richard.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.richard.inventoryapp.data.CakeContract.CakeEntry;

/**
 * Database helper for Cakes app. Manages database creation and version management.
 */
public class CakeDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = CakeDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "bakery.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link CakeDbHelper}.
     *
     * @param context of the app
     */
    public CakeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the cakes table
        String SQL_CREATE_CAKES_TABLE =  "CREATE TABLE " + CakeEntry.TABLE_NAME + " ("
                + CakeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CakeEntry.COLUMN_CAKE_NAME + " TEXT NOT NULL, "
                + CakeEntry.COLUMN_CAKE_OCCASION + " INTEGER NOT NULL, "
                + CakeEntry.COLUMN_CAKE_PRICE + " REAL NOT NULL DEFAULT 0, "
                + CakeEntry.COLUMN_CAKE_QUANTITY + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_CAKES_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}