package com.future.bestmovies.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.future.bestmovies.data.Actor;
import com.future.bestmovies.data.Credits;
import com.future.bestmovies.data.Movie;
import com.future.bestmovies.data.Cast;
import com.future.bestmovies.data.MovieDetails;
import com.future.bestmovies.data.MoviePreferences;
import com.future.bestmovies.data.Review;
import com.future.bestmovies.data.Video;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final String API_MOVIE_BASE_URL = "https://api.themoviedb.org/3";
    private static final String MOVIE = "movie";
    private static final String PAGE_NUMBER = "page";
    private static final String CREDITS = "credits";
    private static final String VIDEOS = "videos";
    private static final String REVIEWS = "reviews";
    private static final String PERSON = "person";
    private static final String MOVIE_CREDITS = "movie_credits";
    private static final String API_KEY = "api_key";
    private static final String API_ID = "xxx";

    private static final String YOUTUBE_VIDEO_BASE_URL = "https://www.youtube.com/watch";
    private static final String YOUTUBE_PARAMETER = "v";

    /* This method returns the main API URL.
     * Using two preferences as parameters, calls a more complex method that builds the actual URL.
     * @param context is used for fetching the users preferences or default preferences, if the app
     * is run for the first time. In default case, the queryType is "popular" and pageNumber is "1"
     */
    private static URL getUrl(Context context) {
        String queryType = MoviePreferences.getPreferredQueryType(context);
        int pageNumber = MoviePreferences.getLastPageNumber(context);
        return buildMoviesApiUrl(queryType, pageNumber);
    }

    /* Build and return the API Url for a category of movies
     * @param queryType is used to query a certain category of movies
     * @pram pageNumber is used to select the page with results
     */
    private static URL buildMoviesApiUrl(String queryType, int pageNumber) {
        Uri movieQueryUri = Uri.parse(API_MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE)
                .appendPath(queryType)
                .appendQueryParameter(PAGE_NUMBER, String.valueOf(pageNumber))
                .appendQueryParameter(API_KEY, API_ID)
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

    /* Build and return the API URL for movie details
     * @param movieId is used to build the url
     */
    private static URL buildMovieDetailsApiUrl(int movieId) {
        Uri movieQueryUri = Uri.parse(API_MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE)
                .appendPath(String.valueOf(movieId))
                .appendQueryParameter(API_KEY, API_ID)
                .build();

        try {
            return new URL(movieQueryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Build and return the API URL for movie cast(credits)
     * @param movieId is used to build the url
     */
    private static URL buildMovieCastApiUrl(int movieId) {
        Uri movieQueryUri = Uri.parse(API_MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE)
                .appendPath(String.valueOf(movieId))
                .appendPath(CREDITS)
                .appendQueryParameter(API_KEY, API_ID)
                .build();

        try {
            return new URL(movieQueryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Build and return the API URL for movie videos
     * @param movieId is used to build the url
     */
    private static URL buildMovieVideosApiUrl(int movieId) {
        Uri movieQueryUri = Uri.parse(API_MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE)
                .appendPath(String.valueOf(movieId))
                .appendPath(VIDEOS)
                .appendQueryParameter(API_KEY, API_ID)
                .build();

        try {
            return new URL(movieQueryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Build and return the API URL for movie reviews
     * @param movieId is used to build the url
     */
    private static URL buildMovieReviewsApiUrl(int movieId) {
        Uri movieQueryUri = Uri.parse(API_MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE)
                .appendPath(String.valueOf(movieId))
                .appendPath(REVIEWS)
                .appendQueryParameter(API_KEY, API_ID)
                .build();

        try {
            return new URL(movieQueryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Build and return the API URL for an actor details
     * @param actorId is used to build the url
     */
    private static URL buildActorApiUrl(int actorId) {
        Uri actorQueryUri = Uri.parse(API_MOVIE_BASE_URL).buildUpon()
                .appendPath(PERSON)
                .appendPath(Integer.toString(actorId))
                .appendQueryParameter(API_KEY, API_ID)
                .build();

        try {
            URL movieQueryUrl = new URL(actorQueryUri.toString());
            Log.v(TAG, "ACTOR URL: " + movieQueryUrl);
            return movieQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Build and return the API URL for an actor details
     * @param actorId is used to build the url
     */
    private static URL buildActorCreditsApiUrl(int actorId) {
        Uri actorQueryUri = Uri.parse(API_MOVIE_BASE_URL).buildUpon()
                .appendPath(PERSON)
                .appendPath(Integer.toString(actorId))
                .appendPath(MOVIE_CREDITS)
                .appendQueryParameter(API_KEY, API_ID)
                .build();

        try {
            URL movieQueryUrl = new URL(actorQueryUri.toString());
            Log.v(TAG, "ACTOR CREDITS URL: " + movieQueryUrl);
            return movieQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Open a HTTP connection, return the content of the HTTP response or null if there is no
     * response and close the connection.
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

    /* Perform a network request using a URL, parse the JSON from that request and return an array
     * of Movie objects.
     * @param context is used to access getUrl utility method
     */
    public static ArrayList<Movie> fetchMovieCategory(Context context) {
        try {
            // Create and return Api url
            URL url = getUrl(context);
            // Use the URL to retrieve the JSON response from Movie API
            String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(url);
            // Parse JSON response and return array of Movie objects
            return JsonUtils.parseMovieCategoryJson(jsonMovieResponse);
        } catch (Exception e) {
            Log.e(TAG, "Error accessing the Server:", e);
            // If something went wrong, we return an empty array of Movie objects
            return null;
        }
    }

    /* Perform a network request using a URL, parse the JSON from that request and return
     * a Movie object.
     * @param context is used to access getUrl utility method
     */
    public static MovieDetails fetchMovieDetails(int movieId) {
        try {
            // Create and return Api url
            URL url = buildMovieDetailsApiUrl(movieId);
            // Use the URL to retrieve the JSON response from Movie API
            String jsonMovieDetailsResponse = NetworkUtils.getResponseFromHttpUrl(url);
            // Parse JSON response and return array of Movie objects
            return JsonUtils.parseMovieDetailsJson(jsonMovieDetailsResponse);
        } catch (Exception e) {
            Log.e(TAG, "Error accessing the Server:", e);
            // If something went wrong, we return an empty array of Movie objects
            return null;
        }
    }

    /* Perform a network request using a URL, parse the JSON from that request and return an array
     * list of Cast objects.
     * @param movieId is used to access buildMovieCastApiUrl utility method
     */
    public static ArrayList<Cast> fetchMovieCast(int movieId) {
        try {
            // Create and return the Api URL for movie cast, based on movie id
            URL url = buildMovieCastApiUrl(movieId);
            // Use the URL to retrieve the JSON response from Movie API
            String jsonMovieCastResponse = NetworkUtils.getResponseFromHttpUrl(url);
            // Parse the JSON into an array list of Cast objects
            return JsonUtils.parseMovieCastJson(jsonMovieCastResponse);
        } catch (Exception e) {
            Log.e(TAG, "Error accessing the Server:", e);
            // If something went wrong, we return null
            return null;
        }
    }

    /* Perform a network request using a URL, parse the JSON from that request and return an array
     * list of Review objects.
     * @param movieId is used to access buildMovieReviewsApiUrl utility method
     */
    public static ArrayList<Review> fetchMovieReviews(int movieId) {
        try {
            // Create and return the Api URL for movie reviews, based on movie id
            URL url = buildMovieReviewsApiUrl(movieId);
            // Use the URL to retrieve the JSON response from Movie API
            String jsonMovieReviewsResponse = NetworkUtils.getResponseFromHttpUrl(url);
            // Parse the JSON into an array list of Review objects
            return JsonUtils.parseMovieReviewJson(jsonMovieReviewsResponse);
        } catch (Exception e) {
            Log.e(TAG, "Error accessing the Server:", e);
            // If something went wrong, we return null
            return null;
        }
    }

    /* Perform a network request using a URL, parse the JSON from that request and return an array
     * list of Video objects.
     * @param movieId is used to access buildMovieVideosApiUrl utility method
     */
    public static ArrayList<Video> fetchMovieVideos(int movieId) {
        try {
            // Create and return the Api URL for movie videos, based on movie id
            URL url = buildMovieVideosApiUrl(movieId);
            // Use the URL to retrieve the JSON response from Movie API
            String jsonMovieVideosResponse = NetworkUtils.getResponseFromHttpUrl(url);
            // Parse the JSON into an array list of Video objects
            return JsonUtils.parseMovieVideosJson(jsonMovieVideosResponse);
        } catch (Exception e) {
            Log.e(TAG, "Error accessing the Server:", e);
            // If something went wrong, we return null
            return null;
        }
    }

    /* Perform a network request using a URL, parse the JSON from that request and return
     * an Actor object.
     * @param actorId is used to access buildActorApiUrl utility method
     */
    public static Actor fetchActorDetails(int actorId) {
        try {
            // Create and return the Api URL for an actor details, based on an actor id
            URL url = buildActorApiUrl(actorId);
            // Use the URL to retrieve the JSON response from Movie API
            String jsonActorDetailsResponse = NetworkUtils.getResponseFromHttpUrl(url);
            // Parse the JSON into an Actor object
            return JsonUtils.parseActorDetailsJson(jsonActorDetailsResponse);
        } catch (Exception e) {
            Log.e(TAG, "Error accessing the Server:", e);
            // If something went wrong, we return null
            return null;
        }
    }

    /* Perform a network request using a URL, parse the JSON from that request and return
     * an array list of Credits objects for a given actor.
     * @param actorId is used to access buildActorCreditsApiUrl utility method
     */
    public static ArrayList<Credits> fetchActorCredits(int actorId) {
        try {
            // Create and return the Api URL for an actor credits, based on an actor id
            URL url = buildActorCreditsApiUrl(actorId);
            // Use the URL to retrieve the JSON response from Movie API
            String jsonActorCreditsResponse = NetworkUtils.getResponseFromHttpUrl(url);
            // Parse the JSON into an array list of Credits objects
            return JsonUtils.parseActorCreditsJson(jsonActorCreditsResponse);
        } catch (Exception e) {
            Log.e(TAG, "Error accessing the Server:", e);
            // If something went wrong, we return null
            return null;
        }
    }

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

    /* Build and return the YOUTUBE URL for movie trailers and teasers
     * @param movieKey is used to build the url
     */
    public static URL buildVideoUrl (String movieKey) {
        Uri videoUri = Uri.parse(YOUTUBE_VIDEO_BASE_URL).buildUpon()
                .appendQueryParameter(YOUTUBE_PARAMETER, movieKey)
                .build();
        try {
            return new URL(videoUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
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
