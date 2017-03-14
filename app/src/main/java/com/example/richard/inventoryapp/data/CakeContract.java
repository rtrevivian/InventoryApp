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

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Cakes app.
 */
public final class CakeContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private CakeContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.richard.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.richard.inventoryapp/cakes/ is a valid path for
     * looking at cake data. content://com.example.richard.inventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_CAKES = "cakes";

    /**
     * Inner class that defines constant values for the cakes database table.
     * Each entry in the table represents a single cake.
     */
    public static final class CakeEntry implements BaseColumns {

        /** The content URI to access the cake data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CAKES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of cakes.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CAKES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single cake.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CAKES;

        /** Name of database table for cakes */
        public final static String TABLE_NAME = "cakes";

        /**
         * Unique ID number for the cake (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the cake.
         *
         * Type: TEXT
         */
        public final static String COLUMN_CAKE_NAME = "name";

        /**
         * Occasion of the cake.
         *
         * The only possible values are {@link #OCCASION_UNKNOWN}, {@link #OCCASION_BIRTHDAY} or {@link #OCCASION_WEDDING}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_CAKE_OCCASION = "occasion";

        /**
         * Price of the cake.
         *
         * Type: REAL
         */
        public final static String COLUMN_CAKE_PRICE = "price";

        /**
         * Quantity of the cake.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_CAKE_QUANTITY = "quantity";

        /**
         * Possible values for the occasion of the cake.
         */
        public static final int OCCASION_UNKNOWN = 0;
        public static final int OCCASION_BIRTHDAY = 100;
        public static final int OCCASION_WEDDING = 200;

        /**
         * Returns whether or not the given occasion is {@link #OCCASION_UNKNOWN}, {@link #OCCASION_BIRTHDAY} or {@link #OCCASION_WEDDING}.
         */
        public static boolean isValidType(int occasion) {
            if (occasion == OCCASION_UNKNOWN || occasion == OCCASION_BIRTHDAY || occasion == OCCASION_WEDDING) {
                return true;
            }
            return false;
        }
    }

}

