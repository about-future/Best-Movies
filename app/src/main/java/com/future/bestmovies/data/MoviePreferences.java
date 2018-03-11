package com.future.bestmovies.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.future.bestmovies.R;
import com.future.bestmovies.utils.ImageUtils;

public class MoviePreferences {
    private static final String TAG = MoviePreferences.class.getSimpleName();
    private static final String IMAGE_WIDTH = "image_width";

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

    public static void setImageSizeForRecyclerView(Context context, String imageWidth) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String imageWidthKey = context.getString(R.string.pref_image_width_key);
        Log.v(TAG, "setImageSizeForRecyclerView: " + imageWidth);
        editor.putString(imageWidthKey, imageWidth);
        editor.apply();
    }

    public static String getImageSizeForRecyclerView(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String optimalImageWidthKey = context.getString(R.string.pref_image_width_key);
        Log.v(TAG, "getImageSizeForRecyclerView: " + sp.getString(optimalImageWidthKey, ImageUtils.IMAGE_SIZE185));
        return sp.getString(optimalImageWidthKey, ImageUtils.IMAGE_SIZE185);
    }

    public static String getPreferredImageQuality(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String preferredImageQualityKey = context.getString(R.string.pref_image_quality_key);
        String defaultImageQuality = context.getString(R.string.pref_image_quality_optimal);
//        Log.v(TAG, "getPreferredImageQuality: " + sp.getString(preferredImageQualityKey, defaultImageQuality));
        return sp.getString(preferredImageQualityKey, defaultImageQuality);
    }

    public static boolean isImageWidthAvailable(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.contains(IMAGE_WIDTH);
    }

}
