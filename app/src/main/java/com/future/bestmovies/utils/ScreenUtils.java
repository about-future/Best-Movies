package com.future.bestmovies.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.future.bestmovies.R;
import com.future.bestmovies.data.MoviePreferences;

public class ScreenUtils {
    private static final String TAG = ScreenUtils.class.getSimpleName();

    // Check the screen orientation and return true if it's landscape or false if it's portrait
    public static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    // Return the movie category title. The category title is selected and saved in Settings.
    public static String createCategoryTitle(Context context) {
        String queryType = MoviePreferences.getPreferredQueryType(context);
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

    // Return the width, height of the screen in pixels and the screen density
    // (i.e. {720.0, 1280.0, 2.0})
    private static float[] getScreenSize(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        return new float[]{
                (float) displayMetrics.widthPixels,
                (float) displayMetrics.heightPixels,
                displayMetrics.density};
    }

    // Return the current width of the screen in DPs.
    // For a phone the width in portrait mode can be 360dp and in landscape mode 640dp.
    // For a tablet the width in portrait mode can be 800dp and in landscape mode 1280dp
    public static int getScreenWidthInDps (Context context) {
        // Get the screen sizes and density
        float[] screenSize = getScreenSize(context);
        // Divide the first item of the array(screen width) by the last one(screen density),
        // round the resulting number and return it
        return Math.round(screenSize[0] / screenSize[2]);
    }

    // Return the smallest width of the screen in DPs (i.e. 360 or 800)
    public static int getSmallestScreenWidthInDps (Context context) {
        // Get screen sizes
        float[] screenSize = getScreenSize(context);

        // Check which size is smaller, divided by the screen density, round it and return it.
        if (screenSize[0] < screenSize[1]) {
            return Math.round(screenSize[0] / screenSize[2]);
        } else {
            return Math.round(screenSize[1] / screenSize[2]);
        }
    }

    // Return the number of columns the will be used for our MainActivity RecyclerView, based on the
    // smallest screen width and the orientation of the device.
    public static int getNumberOfColumns (Context context) {
        // If the user has a phone
        if (getSmallestScreenWidthInDps(context) < 600) {
            // If the phone is in landscape
            if (isLandscapeMode(context)) {
                // We have 3 columns
                return 3;
            } else {
                // Otherwise, in portrait, we have 2 columns
                return 2;
            }
        } else {
            // Otherwise, user must have a tablet
            // If the tablet is in landscape
            if (isLandscapeMode(context)) {
                // We have 5 columns
                return 5;
            } else {
                // Otherwise, in portrait, we have 4 columns
                return 4;
            }
        }
    }
}
