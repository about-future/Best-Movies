package com.future.bestmovies.utils;

import android.content.Context;
import android.content.res.Configuration;

import com.future.bestmovies.R;
import com.future.bestmovies.data.MoviePreferences;

public class ScreenUtils {
    // Check the screen orientation and return true if in landscape
    public static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    // Create a category title, depending on the selected category and set the resulting text
    // to the mMovieCategory object. The title is selected and saved in Settings
    public static String createCategoryTitle(Context context) {
        String queryType = MoviePreferences.getPreferredQueryType(context);
        // Log.v(TAG, "Set title");
        switch (queryType) {
            case "top_rated":
                return context.getString(R.string.pref_category_label_top_rated);
            case "upcoming":
                return context.getString(R.string.pref_category_label_upcoming);
            case "now_playing":
                return context.getString(R.string.pref_category_label_now_playing);
            default:
                return context.getString(R.string.pref_category_label_popular);
        }
    }
}
