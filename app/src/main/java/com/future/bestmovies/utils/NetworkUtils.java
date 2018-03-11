package com.future.bestmovies.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.data.Cast;
import com.future.bestmovies.data.MoviePreferences;

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
    private static final String CREDITS = "credits";
    private static final String DEFAULT_ID = "xxx";

    /* This method prepares the build of our main API URL, using two stored preferences and by
     * calling a more complex method that builds the actual URL
     * @param context is used for fetching the users preferences or default preferences if the app
     * is run for the first time. In this case, the queryType = "popular" and pageNumber = "1"
     */
    private static URL getUrl(Context context) {
        String queryType = MoviePreferences.getPreferredQueryType(context);
        String pageNumber = MoviePreferences.getLastPageNumber(context);
        return buildMovieApiUrl(queryType, pageNumber);
    }

    /* This method will build and return the API Url for a chategory of movies
     * @param queryType is used to query a certain category of movies
     * @pram pageNumber is used to select the page with results
     */
    private static URL buildMovieApiUrl(String queryType, String pageNumber) {
        Uri movieQueryUri = Uri.parse(API_MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE)
                .appendPath(queryType)
                .appendQueryParameter(PAGE_NUMBER, pageNumber)
                .appendQueryParameter(API_KEY, DEFAULT_ID)
                .build();

        try {
            URL movieQueryUrl = new URL(movieQueryUri.toString());
            //Log.v(TAG, "URL: " + movieQueryUrl);
            return movieQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* This method will build and return the API URL for movie cast(credits)
     * @param movieId is used to build the url
     */
    private static URL buildCastMovieApiUrl(String movieId) {
        Uri movieQueryUri = Uri.parse(API_MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE)
                .appendPath(movieId)
                .appendPath(CREDITS)
                .appendQueryParameter(API_KEY, DEFAULT_ID)
                .build();

        try {
            URL movieQueryUrl = new URL(movieQueryUri.toString());
            //Log.v(TAG, "CAST URL: " + movieQueryUrl);
            return movieQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* This method opens a HTTP connection, returns the content of the HTTP response or null if there
     * is no response and closes the connection
     * @param url is the source where we fetch the HTTP response from
     * @throws IOException related to network and stream reading
     */
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

    /* This method performs a network request using a URL, parses the JSON from that request and
     * returns an array of Movie objects
     * @param context is used to access getUrl utility method
     */
    public static Movie[] fetchMovieData(Context context) {
        try {
            // Create and return Api url
            URL url = getUrl(context);
            // Use the URL to retrieve the JSON response from Movie API
            String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(url);
            // Parse JSON response and return array of Movie objects
            return JsonUtils.parseMoviesJson(jsonMovieResponse);
        } catch (Exception e) {
            Log.e(TAG, "Error accessing the Server:", e);
        }

        // If something went wrong, we return an empty array of Movie objects
        return new Movie[]{};
    }

    /* This method performs a network request using a URL, parses the JSON from that request and
     * returns an array of Cast objects
     * @param movieId is used to access buildCastMovieApiUrl utility method
     */
    public static Cast[] fetchMovieCast(String movieId) {
        try {
            // Create and return the Api URL for movie cast, based on movie id
            URL url = buildCastMovieApiUrl(movieId);
            // Use the URL to retrieve the JSON response from Movie API
            String jsonMovieCastResponse = NetworkUtils.getResponseFromHttpUrl(url);
            // Parse the JSON into an array of Cast objects
            return JsonUtils.parseMovieCastJson(jsonMovieCastResponse);
        } catch (Exception e) {
            Log.e(TAG, "Error accessing the Server:", e);
        }

        // If something went wrong, we return an empty array of Cast objects
        return new Cast[]{};
    }

    /* This method performs a state of network connectivity test and returns true or false
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
}
