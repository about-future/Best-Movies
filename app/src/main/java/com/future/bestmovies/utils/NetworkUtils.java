package com.future.bestmovies.utils;

import android.content.Context;
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
    private static final String POPULAR_MOVIES = "movie/popular";
    private static final String TOP_RATED_MOVIES = "movie/top_rated";
    private static final String UPCOMING_MOVIES = "movie/upcoming";
    private static final String NOW_PLAYING_MOVIES = "movie/now_playing";
    private static final String MOVIE = "movie";
    private static final String PAGE_NUMBER = "page";
    private static final String API_KEY = "api_key";
    private static final String DEFAULT_ID = "0d2882c5bcce9ff1ceadd2f23236b077";

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

    public static String getResponseFromHttpUrl(URL url) throws IOException {
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

    public static Movie[] fetchMovieData(URL url) {
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
//    OkHttpClient client = new OkHttpClient();
//
//    MediaType mediaType = MediaType.parse("application/octet-stream");
//    RequestBody body = RequestBody.create(mediaType, "{}");
//    Request request = new Request.Builder()
//            .url("https://api.themoviedb.org/3/movie/top_rated?page=1&language=en-US&api_key=%3C%3Capi_key%3E%3E")
//            .get()
//            .build();
//
//    Response response = client.newCall(request).execute();


}
