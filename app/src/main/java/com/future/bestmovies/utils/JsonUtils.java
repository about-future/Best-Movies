package com.future.bestmovies.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.data.Cast;
import com.future.bestmovies.data.Review;
import com.future.bestmovies.data.Video;

import java.util.ArrayList;


public class JsonUtils {
    // Main label
    private static final String MOVIE_ID = "id";

    //Movie labels
    private static final String RESULTS = "results";
    private static final String RESULT_ID = "id";
    private static final String RESULT_TITLE = "original_title";
    private static final String RESULT_POSTER_PATH = "poster_path";
    private static final String RESULT_BACKDROP_PATH = "backdrop_path";
    private static final String RESULT_OVERVIEW = "overview";
    private static final String RESULT_VOTE_AVERAGE = "vote_average";
    private static final String RESULT_RELEASE_DATE = "release_date";
    private static final String RESULT_GENRE_IDS = "genre_ids";

    //Cast labels
    private static final String CAST = "cast";
    private static final String CHARACTER = "character";
    private static final String ACTOR_ID = "id";
    private static final String NAME = "name";
    private static final String PROFILE_PATH = "profile_path";

    // Reviews labels
    private static final String TOTAL_PAGES = "total_pages";
    private static final String TOTAL_RESULTS = "total_results";
    private static final String REVIEW_RESULTS = "results";
    private static final String REVIEW_AUTHOR = "author";
    private static final String REVIEW_CONTENT = "content";
    private static final String REVIEW_ID = "id";
    private static final String REVIEW_URL = "url";

    // Video labels
    private static final String VIDEO_RESULTS = "results";
    private static final String VIDEO_ID = "id";
    private static final String VIDEO_KEY = "key";
    private static final String VIDEO_NAME = "name";
    private static final String VIDEO_SITE = "site";
    private static final String VIDEO_SIZE = "size";
    private static final String VIDEO_TYPE = "type";

    // Parses the JSON response for the list of movies and their details
    public static ArrayList<Movie> parseMoviesJson(String moviesJsonStr) throws JSONException {
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

        ArrayList<Movie> movies = new ArrayList<>(jsonResultsArray.length());
        for (int i = 0; i < jsonResultsArray.length(); i++) {
            JSONObject movieJson = jsonResultsArray.getJSONObject(i);

            id = movieJson.getInt(RESULT_ID);
            voteAverage = movieJson.getDouble(RESULT_VOTE_AVERAGE);
            title = movieJson.getString(RESULT_TITLE);
            posterPath = movieJson.getString(RESULT_POSTER_PATH);
            backdropPath = movieJson.getString(RESULT_BACKDROP_PATH);
            overview = movieJson.getString(RESULT_OVERVIEW);
            releaseDate = movieJson.getString(RESULT_RELEASE_DATE);

            JSONArray genreArray = movieJson.getJSONArray(RESULT_GENRE_IDS);
            genreIds = new int[genreArray.length()];
            for (int j = 0; j < genreArray.length(); j++) {
                genreIds[j] = genreArray.getInt(j);
            }

            movies.add(i, new Movie(id, voteAverage, title, posterPath, backdropPath, overview, releaseDate, genreIds));
        }

        return movies;
    }

    // Parses the JSON response for entire list of actors from selected movie
    public static ArrayList<Cast> parseMovieCastJson(String movieCastStr) throws JSONException {
        int movieId;
        String character;
        int id;
        String name;
        String profilePath;

        // Instantiate a JSON object so we can get data.
        JSONObject fullMovieCastJson = new JSONObject(movieCastStr);

        movieId = fullMovieCastJson.getInt(MOVIE_ID);
        JSONArray jsonResultsArray = fullMovieCastJson.getJSONArray(CAST);

        ArrayList<Cast> movieCast = new ArrayList<>(jsonResultsArray.length());
        for (int i = 0; i < jsonResultsArray.length(); i++) {
            JSONObject actorJson = jsonResultsArray.getJSONObject(i);

            character = actorJson.getString(CHARACTER);
            id = actorJson.getInt(ACTOR_ID);
            name = actorJson.getString(NAME);
            profilePath = actorJson.getString(PROFILE_PATH);

            movieCast.add(i, new Cast(movieId, character, id, name, profilePath));
        }

        return movieCast;
    }

    // Parses the JSON response for the list of reviews for a movie
    public static ArrayList<Review> parseMovieReviewJson(String movieReviewsJsonStr) throws JSONException {
        int movieId;
        String author;
        String content;

        // Instantiate a JSON object so we can get data.
        JSONObject allReviewsJson = new JSONObject(movieReviewsJsonStr);

        movieId = allReviewsJson.getInt(MOVIE_ID);
        JSONArray jsonResultsArray = allReviewsJson.getJSONArray(REVIEW_RESULTS);

        ArrayList<Review> reviews = new ArrayList<>(jsonResultsArray.length());
        for (int i = 0; i < jsonResultsArray.length(); i++) {
            JSONObject reviewJson = jsonResultsArray.getJSONObject(i);

            author = reviewJson.getString(REVIEW_AUTHOR);
            content = reviewJson.getString(REVIEW_CONTENT);

            reviews.add(i, new Review(movieId, author, content));
        }

        return reviews;
    }

    // Parses the JSON response for the list of videos for a movie
    public static ArrayList<Video> parseMovieVideosJson(String movieVideosJsonStr) throws JSONException {
        int movieId;
        String videoKey;
        String videoName;
        String videoType;

        // Instantiate a JSON object so we can get data.
        JSONObject allVideosJson = new JSONObject(movieVideosJsonStr);

        movieId = allVideosJson.getInt(MOVIE_ID);
        JSONArray jsonResultsArray = allVideosJson.getJSONArray(VIDEO_RESULTS);

        ArrayList<Video> videos = new ArrayList<>(jsonResultsArray.length());
        for (int i = 0; i < jsonResultsArray.length(); i++) {
            JSONObject videoJson = jsonResultsArray.getJSONObject(i);

            videoKey = videoJson.getString(VIDEO_KEY);
            videoName = videoJson.getString(VIDEO_NAME);
            videoType = videoJson.getString(VIDEO_TYPE);

            videos.add(i, new Video(movieId, videoKey, videoName, videoType));
        }

        return videos;
    }
}