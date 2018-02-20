package com.future.bestmovies.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.future.bestmovies.data.Movie;

public class JsonUtils {
    private static final String PAGE = "page";
    private static final String TOTAL_RESULTS = "total_results";
    private static final String TOTAL_PAGES = "total_pages";
    private static final String RESULTS = "results";
    private static final String RESULT_ID = "id";
    private static final String RESULT_TITLE = "original_title";
    private static final String RESULT_POSTER_PATH = "poster_path";
    private static final String RESULT_BACKDROP_PATH = "backdrop_path";
    private static final String RESULT_OVERVIEW = "overview";
    private static final String RESULT_VOTE_AVERAGE = "vote_average";
    private static final String RESULT_RELESE_DATE = "release_date";

    public static Movie[] parseMoviesJson(String moviesJsonStr) throws JSONException {
        int id;
        double voteAverage;
        String title;
        String posterPath;
        String backdropPath;
        String overview;
        String releseDate;

        // Instantiate a JSON object so we can get data.
        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        //TODO: get page, total results, total pages from moviesJson object

        JSONArray jsonResultsArray = moviesJson.getJSONArray(RESULTS);
        Movie[] movies = new Movie[jsonResultsArray.length()];
        for (int i = 0; i < jsonResultsArray.length(); i++) {
            JSONObject movieJson = jsonResultsArray.getJSONObject(i);

            id = movieJson.getInt(RESULT_ID);
            voteAverage = movieJson.getDouble(RESULT_VOTE_AVERAGE);
            title = movieJson.getString(RESULT_TITLE);
            posterPath = movieJson.getString(RESULT_POSTER_PATH);
            backdropPath = movieJson.getString(RESULT_BACKDROP_PATH);
            overview = movieJson.getString(RESULT_OVERVIEW);
            releseDate = movieJson.getString(RESULT_RELESE_DATE);

            movies[i] = new Movie(id, voteAverage, title, posterPath, backdropPath, overview, releseDate);
        }

        return movies;
    }
}