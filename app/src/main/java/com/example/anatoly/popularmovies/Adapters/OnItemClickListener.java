package com.example.anatoly.popularmovies.Adapters;

import android.view.View;

/**
 * Interface to be used in all adapters;
 */
public interface OnItemClickListener {

    /**
     * This method is called when an item is clicked
     * @param position - position of the item in list
     * @param view - using view to make one listener for different views
     *             and also to pass some String value in the Tag field.
     *             (e.g. Key value of the Trailer - to make YouTube link from it)
     */
    void itemClicked(int position, View view);
}
