package com.future.bestmovies.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import com.future.bestmovies.data.Movie;
import com.future.bestmovies.data.MoviePreferences;
import org.json.JSONException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final String API_MOVIE_BASE_URL = "https://api.themoviedb.org/3";
    private static final String MOVIE = "movie";
    private static final String PAGE_NUMBER = "page";
    private static final String API_KEY = "api_key";
    private static final String DEFAULT_ID = "2e40dce982635fd7c13175c8b1cb1f54";

    private static final int TYPE_WIFI = 1;
    private static final int TYPE_MOBILE = 2;
    private static final int TYPE_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_NOT_CONNECTED = 0;
    private static final int NETWORK_STAUS_WIFI = 1;
    private static final int NETWORK_STATUS_MOBILE = 2;

    public static URL getUrl(Context context) {
        String queryType = MoviePreferences.getPreferredQueryType(context);
        int pageNumber = 1; //MoviePreferences.getLastPageNumber(contex);
        return buildMovieApiUrlWithQueryTypeAndPageNumber(queryType, pageNumber);
    }

    // This method will build the API Url based on the selected category and selected page number
    private static URL buildMovieApiUrlWithQueryTypeAndPageNumber(String queryType, int pageNumber) {
        Uri movieQueryUri = Uri.parse(API_MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE)
                .appendPath(queryType)
                .appendQueryParameter(PAGE_NUMBER, Integer.toString(pageNumber))
                .appendQueryParameter(API_KEY, DEFAULT_ID)
                .build();

        try {
            URL movieQueryUrl = new URL(movieQueryUri.toString());
            Log.v(TAG, "URL: " + movieQueryUrl);
            return movieQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }

    public static Movie[] fetchMovieData(Context context) {
        URL url = getUrl(context);

        String jsonMovieResponse = null;
        try {
            // Use the URL to retrieve the JSON response from Movie API
            jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(url);

        } catch (Exception e) {
            Log.e(TAG, "Error accessing to Server:", e);
        }

        Movie[] movieResults = null;
        try {
            // Parse the JSON into an array of Movie objects
            movieResults = JsonUtils.parseMoviesJson(jsonMovieResponse);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing Movies JSON: ", e);
        }

        return movieResults;
    }

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static int getConnectivityStatusString(Context context) {
        int conn = getConnectivityStatus(context);
        int status = 0;
        if (conn == TYPE_WIFI) {
            status = NETWORK_STAUS_WIFI;
        } else if (conn == TYPE_MOBILE) {
            status =NETWORK_STATUS_MOBILE;
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = NETWORK_STATUS_NOT_CONNECTED;
        }
        return status;
    }
}
