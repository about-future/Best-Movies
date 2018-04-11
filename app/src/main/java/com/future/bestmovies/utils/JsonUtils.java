package com.future.bestmovies.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.future.bestmovies.credits.Actor;
import com.future.bestmovies.credits.Credits;
import com.future.bestmovies.movie.Movie;
import com.future.bestmovies.cast.Cast;
import com.future.bestmovies.movie.MovieDetails;
import com.future.bestmovies.reviews.Review;
import com.future.bestmovies.videos.Video;

import java.util.ArrayList;


public class JsonUtils {
    // Main label
    private static final String MOVIE_ID = "id";

    //Movie and MovieDetails labels
    private static final String RESULTS = "results";
    private static final String RESULT_BACKDROP_PATH = "backdrop_path";
    private static final String RESULT_GENRE_IDS = "genre_ids";
    private static final String RESULT_GENRES = "genres";
    private static final String RESULT_GENRE_NAME = "name";
    private static final String RESULT_ID = "id";
    private static final String RESULT_TITLE = "original_title";
    private static final String RESULT_LANGUAGE = "original_language";
    private static final String RESULT_OVERVIEW = "overview";
    private static final String RESULT_POSTER_PATH = "poster_path";
    private static final String RESULT_RELEASE_DATE = "release_date";
    private static final String RESULT_RUNTIME = "runtime";
    private static final String RESULT_VOTE_AVERAGE = "vote_average";

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

    // Actor labels
    private static final String BIRTHDAY = "birthday";
    private static final String DEATH_DAY = "deathday";
    private static final String GENDER = "gender";
    //private static final String ACTOR_ID = "id";
    //private static final String NAME = "name";
    private static final String BIOGRAPHY = "biography";
    private static final String PLACE_OF_BIRTH = "place_of_birth";
    //private static final String PROFILE_PATH = "profile_path";

    // Actor credits labels
    private static final String CREDIT_BACKDROP_PATH = "backdrop_path";
    private static final String CREDIT_CHARACTER = "character";
    private static final String CREDIT_ID = "id";
    private static final String CREDIT_TITLE = "original_title";
    private static final String CREDIT_POSTER_PATH = "poster_path";
    private static final String CREDIT_RELEASE_DATE = "release_date";

    // Parses the JSON response for the list of movies and their details
    public static ArrayList<Movie> parseMovieCategoryJson(String moviesJsonStr) throws JSONException {
        int id;
        String posterPath;
        String title;

        // Instantiate a JSON object so we can get data.
        JSONObject allMoviesJson = new JSONObject(moviesJsonStr);
        JSONArray jsonResultsArray = allMoviesJson.getJSONArray(RESULTS);

        ArrayList<Movie> movies = new ArrayList<>(jsonResultsArray.length());
        for (int i = 0; i < jsonResultsArray.length(); i++) {
            JSONObject movieJson = jsonResultsArray.getJSONObject(i);

            id = movieJson.getInt(RESULT_ID);
            posterPath = movieJson.getString(RESULT_POSTER_PATH);
            title = movieJson.getString(RESULT_TITLE);

            movies.add(i, new Movie(id, posterPath, title));
        }

        return movies;
    }

    // Parses the JSON response for the list of movies and their details
    public static MovieDetails parseMovieDetailsJson(String movieDetailsJsonStr) throws JSONException {
        String backdropPath;
        String[] genres;
        int id;
        String language;
        String title;
        String overview;
        String posterPath;
        String releaseDate;
        int runtime;
        double voteAverage;

        // Instantiate a JSON object so we can get data.
        JSONObject movieDetailsJson = new JSONObject(movieDetailsJsonStr);

        backdropPath = movieDetailsJson.getString(RESULT_BACKDROP_PATH);
        JSONArray genreArray = movieDetailsJson.getJSONArray(RESULT_GENRES);
        genres = new String[genreArray.length()];
        for (int i = 0; i < genreArray.length(); i++) {
            JSONObject genreObject = genreArray.getJSONObject(i);
            genres[i] = genreObject.getString(RESULT_GENRE_NAME);
        }
        id = movieDetailsJson.getInt(RESULT_ID);
        language = movieDetailsJson.getString(RESULT_LANGUAGE);
        title = movieDetailsJson.getString(RESULT_TITLE);
        overview = movieDetailsJson.getString(RESULT_OVERVIEW);
        posterPath = movieDetailsJson.getString(RESULT_POSTER_PATH);
        releaseDate = movieDetailsJson.getString(RESULT_RELEASE_DATE);
        try {
            runtime = movieDetailsJson.getInt(RESULT_RUNTIME);
        } catch (JSONException e) {
            runtime = 0;
        }
        voteAverage = movieDetailsJson.getDouble(RESULT_VOTE_AVERAGE);

        return new MovieDetails(
                backdropPath, genres, id, language, overview,
                posterPath, voteAverage, releaseDate, runtime, title);
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

    // Parses the JSON response for the actor details
    public static Actor parseActorDetailsJson(String actorDetailsJsonStr) throws JSONException {
        int id;
        String birthday;
        String deathday;
        String gender;
        String name;
        String biography;
        String placeOfBirth;
        String profilePath;

        // Instantiate a JSON object so we can get data.
        JSONObject actorDetailsJson = new JSONObject(actorDetailsJsonStr);

        id = actorDetailsJson.getInt(ACTOR_ID);
        try {
            if (!actorDetailsJson.getString(BIRTHDAY).equals("null"))
                birthday = actorDetailsJson.getString(BIRTHDAY);
            else
                birthday = "Unknown";
        } catch (JSONException e) {
            birthday = "Unknown";
        }
        try {
            deathday = actorDetailsJson.getString(DEATH_DAY);
        } catch (JSONException e) {
            deathday = "Unknown";
        }

        if (actorDetailsJson.getInt(GENDER) == 1)
            gender = "Female";
        else if (actorDetailsJson.getInt(GENDER) == 2)
            gender = "Male";
        else gender = "Unknown";

        name = actorDetailsJson.getString(NAME);
        biography = actorDetailsJson.getString(BIOGRAPHY);

        try {
            if (!actorDetailsJson.getString(PLACE_OF_BIRTH).equals("null"))
                placeOfBirth = actorDetailsJson.getString(PLACE_OF_BIRTH);
            else
                placeOfBirth = "Unknown";
        } catch (JSONException e) {
            placeOfBirth = "Unknown";
        }

        try {
            if (actorDetailsJson.getString(PROFILE_PATH) != null)
                profilePath = actorDetailsJson.getString(PROFILE_PATH);
            else profilePath = "none";
        } catch (JSONException e) {
            profilePath = "none";
        }

        return new Actor(id, birthday, deathday, gender, name, biography, placeOfBirth, profilePath);
    }

    // Parses the JSON response for the list of Credits of an actor
    public static ArrayList<Credits> parseActorCreditsJson(String actorCreditsJsonStr) throws JSONException {
        String backdropPath;
        String character;
        int movieId;
        String posterPath;
        String releaseDate;
        int releaseYear;
        String title;

        // Instantiate a JSON object so we can get data.
        JSONObject actorDetailsJson = new JSONObject(actorCreditsJsonStr);

        JSONArray jsonResultsArray = actorDetailsJson.getJSONArray(CAST);

        ArrayList<Credits> credits = new ArrayList<>(jsonResultsArray.length());
        for (int i = 0; i < jsonResultsArray.length(); i++) {
            JSONObject creditJson = jsonResultsArray.getJSONObject(i);

            backdropPath = creditJson.getString(CREDIT_BACKDROP_PATH);
            try {
                character = creditJson.getString(CREDIT_CHARACTER);
            } catch (JSONException e) {
                character = "Unknown";
            }

            movieId = creditJson.getInt(CREDIT_ID);
            posterPath = creditJson.getString(CREDIT_POSTER_PATH);
            try {
                releaseDate = creditJson.getString(CREDIT_RELEASE_DATE);
                if (releaseDate != null && releaseDate.length() > 4) {
                    releaseDate = releaseDate.substring(0, 4);
                } else {
                    releaseDate = "9999";
                }
            } catch (JSONException e) {
                releaseDate = "9999";
            }

            title = creditJson.getString(CREDIT_TITLE);

            credits.add(i, new Credits(character, movieId, posterPath, releaseDate, title));
        }

        return credits;
    }
}