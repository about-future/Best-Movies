package com.future.bestmovies.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public class NetworkUtils {
    public static final String API_ID = "xxx";
    private static final String YOUTUBE_VIDEO_BASE_URL = "https://www.youtube.com/watch";
    private static final String YOUTUBE_PARAMETER = "v";

    /* Perform a state of network connectivity test and return true or false.
     * @param context is used to create a reference to the ConnectivityManager
     */
    public static boolean isConnected(Context context) {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo activeNetwork = null;
        if(cm != null){
            activeNetwork = cm.getActiveNetworkInfo();
        }

        // Return true if there is an active network and  if the device is connected or connecting
        // to the active network, otherwise return false
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /* Build and return the YOUTUBE Uri for movie trailers and teasers
     * @param movieKey is used to build the Uri
     */
    public static Uri buildVideoUri (String movieKey) {
        return Uri.parse(YOUTUBE_VIDEO_BASE_URL).buildUpon()
                .appendQueryParameter(YOUTUBE_PARAMETER, movieKey)
                .build();
    }
}
