package com.future.bestmovies.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.future.bestmovies.R;
import com.future.bestmovies.utils.ImageUtils;

public class MoviePreferences {
    private static final String TAG = MoviePreferences.class.getSimpleName();
    private static final String SCREEN_WIDTH = "screen_width";

    public static String getPreferredQueryType(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String movieCategoryKey = context.getString(R.string.pref_category_key);
        String defaultCategory = context.getString(R.string.pref_category_popular);
        return sp.getString(movieCategoryKey, defaultCategory);
    }

    public static String getLastPageNumber(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String pageNumberKey = context.getString(R.string.pref_page_number_key);
        String defaultPageNumber = context.getString(R.string.pref_page_number_default);
        return sp.getString(pageNumberKey, defaultPageNumber);
    }

    public static void setLastPageNumber(Context context, String pageNumber) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String pageNumberKey = context.getString(R.string.pref_page_number_key);
        editor.putString(pageNumberKey, pageNumber);
        editor.apply();
    }

    public static void setImageSizeForRecyclerView(Context context, String optimalImageWidth) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String optimalImageWidthKey = context.getString(R.string.pref_screen_width_key);
        //Log.v(TAG, "setImageSizeForRecyclerView: " + optimalImageWidth);
        editor.putString(optimalImageWidthKey, optimalImageWidth);
        editor.apply();
    }

    public static String getImageSizeForRecyclerView(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String optimalImageWidthKey = context.getString(R.string.pref_screen_width_key);
        Log.v(TAG, "getImageSizeForRecyclerView: " + sp.getString(optimalImageWidthKey, ImageUtils.IMAGE_SIZE500));
        return sp.getString(optimalImageWidthKey, ImageUtils.IMAGE_SIZE500);
    }

    public static String getPreferredImageSize(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String preferredImageSizeKey = context.getString(R.string.pref_image_size_key);
        String defaultImageSize = context.getString(R.string.pref_image_size_optimal);
        //Log.v(TAG, "getPreferredImageSize: " + sp.getString(preferredImageSizeKey, defaultImageSize));
        return sp.getString(preferredImageSizeKey, defaultImageSize);
    }

    public static void setPreferredImageSize(Context context, int preferredImageSize) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String preferredImageSizeKey = context.getString(R.string.pref_image_size_key);
        //Log.v(TAG, "setPreferredImageSize: " + preferredImageSize);
        editor.putInt(preferredImageSizeKey, preferredImageSize);
        editor.apply();
    }

    public static boolean isImageSizeAvailable(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.contains(SCREEN_WIDTH);
    }

}
