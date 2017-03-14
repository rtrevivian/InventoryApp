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

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.richard.inventoryapp.data.CakeContract;

/**
 * {@link CakeCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of cake data as its data source. This adapter knows
 * how to create list items for each row of cake data in the {@link Cursor}.
 */
public class CakeCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link CakeCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public CakeCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the cake data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current cake can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView occasionTextView = (TextView) view.findViewById(R.id.occasion);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        // Find the columns of cake attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(CakeContract.CakeEntry.COLUMN_CAKE_NAME);
        int occasionColumnIndex = cursor.getColumnIndex(CakeContract.CakeEntry.COLUMN_CAKE_OCCASION);
        int priceColumnIndex = cursor.getColumnIndex(CakeContract.CakeEntry.COLUMN_CAKE_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(CakeContract.CakeEntry.COLUMN_CAKE_QUANTITY);

        // Read the cake attributes from the Cursor for the current cake
        String cakeName = cursor.getString(nameColumnIndex);

        int cakeOccasion = cursor.getInt(occasionColumnIndex);
        String cakeOccasionString = new String();
        switch (cakeOccasion) {
            case CakeContract.CakeEntry.OCCASION_BIRTHDAY:
                cakeOccasionString = context.getString(R.string.occasion_birthday);
                break;
            case CakeContract.CakeEntry.OCCASION_WEDDING:
                cakeOccasionString = context.getString(R.string.occasion_wedding);
                break;
            default:
                cakeOccasionString = context.getString(R.string.occasion_unknown);
                break;
        }

        Double cakePrice = cursor.getDouble(priceColumnIndex);
        String cakePriceString = "$" + cakePrice;

        int cakeQuantity = cursor.getInt(quantityColumnIndex);
        String cakeQuantityString = cakeQuantity + " left";

        // Update the TextViews with the attributes for the current cake
        nameTextView.setText(cakeName);
        occasionTextView.setText(cakeOccasionString);
        priceTextView.setText(cakePriceString);
        quantityTextView.setText(cakeQuantityString);
    }
}
