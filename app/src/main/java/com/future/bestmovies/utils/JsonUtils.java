package com.future.bestmovies.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.data.MovieCast;

public class JsonUtils {
    //Movie labels
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
    private static final String RESULT_GENRE_IDS = "genre_ids";

    //Cast labels
    private static final String CAST = "cast";
    private static final String CHARACTER = "character";
    private static final String ACTOR_ID = "id";
    private static final String NAME = "name";
    private static final String PROFILE_PATH = "profile_path";

    public static Movie[] parseMoviesJson(String moviesJsonStr) throws JSONException {
        int id;
        double voteAverage;
        String title;
        String posterPath;
        String backdropPath;
        String overview;
        String releaseDate;
        int[] genreIds;

        // Instantiate a JSON object so we can get data.
        JSONObject allMoviesJson = new JSONObject(moviesJsonStr);

        JSONArray jsonResultsArray = allMoviesJson.getJSONArray(RESULTS);
        Movie[] movies = new Movie[jsonResultsArray.length()];
        for (int i = 0; i < jsonResultsArray.length(); i++) {
            JSONObject movieJson = jsonResultsArray.getJSONObject(i);

            id = movieJson.getInt(RESULT_ID);
            voteAverage = movieJson.getDouble(RESULT_VOTE_AVERAGE);
            title = movieJson.getString(RESULT_TITLE);
            posterPath = movieJson.getString(RESULT_POSTER_PATH);
            backdropPath = movieJson.getString(RESULT_BACKDROP_PATH);
            overview = movieJson.getString(RESULT_OVERVIEW);
            releaseDate = movieJson.getString(RESULT_RELESE_DATE);

            JSONArray genreArray = movieJson.getJSONArray(RESULT_GENRE_IDS);
            genreIds = new int[genreArray.length()];
            for (int j = 0; j < genreArray.length(); j++) {
                genreIds[j] = genreArray.getInt(j);
            }

            movies[i] = new Movie(id, voteAverage, title, posterPath, backdropPath, overview, releaseDate, genreIds);
        }

        return movies;
    }

    // Parses the JSON response for page number, total results and total pages
    public static int[] parsePagesJson(String moviesJsonStr) throws JSONException {
        // Instantiate a JSON object so we can get data.
        JSONObject allMoviesJson = new JSONObject(moviesJsonStr);

        // Get each value, store it in the array and return them
        return new int[]{allMoviesJson.getInt(PAGE),
                allMoviesJson.getInt(TOTAL_RESULTS),
                allMoviesJson.getInt(TOTAL_PAGES)};
    }

    // Parses the JSON response for entire list of actors from selected movie
    public static MovieCast[] parseMovieCastJson(String movieCastStr) throws JSONException {
        String character;
        int id;
        String name;
        String profilePath;

        // Instantiate a JSON object so we can get data.
        JSONObject fullMovieCastJson = new JSONObject(movieCastStr);

        JSONArray jsonResultsArray = fullMovieCastJson.getJSONArray(CAST);
        MovieCast[] movieCast = new MovieCast[jsonResultsArray.length()];
        for (int i = 0; i < jsonResultsArray.length(); i++) {
            JSONObject actorJson = jsonResultsArray.getJSONObject(i);

            character = actorJson.getString(CHARACTER);
            id = actorJson.getInt(ACTOR_ID);
            name = actorJson.getString(NAME);
            profilePath = actorJson.getString(PROFILE_PATH);

            movieCast[i] = new MovieCast(character, id, name, profilePath);
        }

        return movieCast;
    }
}