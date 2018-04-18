package com.future.bestmovies.movie;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.future.bestmovies.R;


public class MoviePreferences {
    private static final String TAG = MoviePreferences.class.getSimpleName();
    private static final String CATEGORY = "category";
    private static final String IMAGE_WIDTH = "image_width";
    private static final int DEFAULT_IMAGE_WIDTH = 185;

    public static String getPreferredQueryType(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultCategory = context.getString(R.string.category_popular);
        Log.v(TAG, "getQueryType: " + sp.getString(CATEGORY, defaultCategory));
        return sp.getString(CATEGORY, defaultCategory);
    }

    public static void setPreferredQueryType(Context context, String category) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CATEGORY, category);
        Log.v("CATEGORY", category);
        editor.apply();
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

    public static int getLastSearchPageNumber(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String pageNumberKey = context.getString(R.string.pref_search_page_number_key);
        int defaultPageNumber = Integer.parseInt(context.getString(R.string.pref_search_page_number_default));
        Log.v(TAG, "getSearchPageNumber: " + sp.getInt(pageNumberKey, defaultPageNumber));
        return sp.getInt(pageNumberKey, defaultPageNumber);
    }

    public static void setLastSearchPageNumber(Context context, int pageNumber) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String pageNumberKey = context.getString(R.string.pref_search_page_number_key);
        editor.putInt(pageNumberKey, pageNumber);
        editor.apply();
        Log.v(TAG, "setSearchPageNumber: " + pageNumber);
    }

    public static void setImageWidthForRecyclerView(Context context, int imageWidth) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String imageWidthKey = context.getString(R.string.pref_image_width_key);
        //Log.v(TAG, "setImageSizeForRecyclerView: " + imageWidth);
        editor.putInt(imageWidthKey, imageWidth);
        editor.apply();
    }

    public static int getImageWidthForRecyclerView(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String optimalImageWidthKey = context.getString(R.string.pref_image_width_key);
        //Log.v(TAG, "getImageSizeForRecyclerView: " + sp.getInt(optimalImageWidthKey, DEFAULT_IMAGE_WIDTH));
        return sp.getInt(optimalImageWidthKey, DEFAULT_IMAGE_WIDTH);
    }

    public static String getPreferredImageQuality(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String preferredImageQualityKey = context.getString(R.string.pref_image_quality_key);
        String defaultImageQuality = context.getString(R.string.pref_image_quality_optimal);
        //Log.v(TAG, "getPreferredImageQuality: " + sp.getString(preferredImageQualityKey, defaultImageQuality));
        return sp.getString(preferredImageQualityKey, defaultImageQuality);
    }

    public static boolean isImageWidthAvailable(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //Log.v(TAG, "isImageWidthAvailable: " + sp.contains(IMAGE_WIDTH));
        return sp.contains(IMAGE_WIDTH);
    }
}
