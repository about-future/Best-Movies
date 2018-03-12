package com.future.bestmovies.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.future.bestmovies.R;


public class MoviePreferences {
    private static final String TAG = MoviePreferences.class.getSimpleName();
    private static final String IMAGE_WIDTH = "image_width";
    private static final int DEFAULT_IMAGE_WIDTH = 185;

    public static String getPreferredQueryType(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String movieCategoryKey = context.getString(R.string.pref_category_key);
        String defaultCategory = context.getString(R.string.pref_category_popular);
        Log.v(TAG, "getQueryType: " + sp.getString(movieCategoryKey, defaultCategory));
        return sp.getString(movieCategoryKey, defaultCategory);
    }

    public static int getLastPageNumber(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String pageNumberKey = context.getString(R.string.pref_page_number_key);
        int defaultPageNumber = Integer.parseInt(context.getString(R.string.pref_page_number_default));
        Log.v(TAG, "getPageNumber: " + sp.getInt(pageNumberKey, defaultPageNumber));
        return sp.getInt(pageNumberKey, defaultPageNumber);
    }

    public static void setLastPageNumber(Context context, int pageNumber) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String pageNumberKey = context.getString(R.string.pref_page_number_key);
        editor.putInt(pageNumberKey, pageNumber);
        editor.apply();
        Log.v(TAG, "setPageNumber: " + pageNumber);
    }

    public static void setImageWidthForRecyclerView(Context context, int imageWidth) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String imageWidthKey = context.getString(R.string.pref_image_width_key);
        Log.v(TAG, "setImageSizeForRecyclerView: " + imageWidth);
        editor.putInt(imageWidthKey, imageWidth);
        editor.apply();
    }

    public static int getImageWidthForRecyclerView(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String optimalImageWidthKey = context.getString(R.string.pref_image_width_key);
        Log.v(TAG, "getImageSizeForRecyclerView: " + sp.getInt(optimalImageWidthKey, DEFAULT_IMAGE_WIDTH));
        return sp.getInt(optimalImageWidthKey, DEFAULT_IMAGE_WIDTH);
    }

    public static String getPreferredImageQuality(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String preferredImageQualityKey = context.getString(R.string.pref_image_quality_key);
        String defaultImageQuality = context.getString(R.string.pref_image_quality_optimal);
        Log.v(TAG, "getPreferredImageQuality: " + sp.getString(preferredImageQualityKey, defaultImageQuality));
        return sp.getString(preferredImageQualityKey, defaultImageQuality);
    }

    public static boolean isImageWidthAvailable(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Log.v(TAG, "isImageWidthAvailable: " + sp.contains(IMAGE_WIDTH));
        return sp.contains(IMAGE_WIDTH);
    }
}
