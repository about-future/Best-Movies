package com.future.bestmovies.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class FavouritesContract {
    private FavouritesContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.future.bestmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.future.bestmovies/movie/ is a valid path for
     * looking at movie data. content://com.future.bestmovies/ratings/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "ratings".
     */
    public static final String PATH_MOVIES = "movies";

    // Inner class that defines constant values for the movie details database table.
    // Each entry in the table represents a single movie.
    public static abstract class MovieDetailsEntry implements BaseColumns {
        // The content URI to access the movie data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MOVIES);

        // The MIME type of the {@link #CONTENT_URI} for a list of movies.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        // The MIME type of the {@link #CONTENT_URI} for a single movie.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        // Name of database table for movie details
        public final static String TABLE_NAME = "movie_details";

        // Unique ID number for the movie (only for use in the database table). Type: INTEGER */
        public final static String _ID = BaseColumns._ID;

        // Id of the movie. Type: INTEGER
        public final static String COLUMN_MOVIE_ID ="movie_id";

        // Title of the movie. Type: TEXT
        public final static String COLUMN_TITLE ="title";

        // Poster path of the movie. Type: TEXT
        public final static String COLUMN_POSTER_PATH = "poster_path";

        // Backdrop path of the movie. Type: TEXT
        public final static String COLUMN_BACKDROP_PATH = "backdrop_path";

        // Plot of the movie. Type: TEXT
        public final static String COLUMN_PLOT = "overview";

        // Average ratings of the movie. Type: DOUBLE
        public final static String COLUMN_RATINGS ="ratings";

        // Release date of the movie. Type: TEXT
        public final static String COLUMN_RELEASE_DATE ="release_date";

        // Genres of the movie. Type: INTEGER
        public final static String COLUMN_GENRES ="genres";
    }


    public static final String PATH_CAST = "cast";
    // Inner class that defines constant values for the cast database table.
    // Each entry in the table represents a single cast member.
    public static abstract class CastEntry implements BaseColumns {
        // The content URI to access the section data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CAST);

        // The MIME type of the {@link #CONTENT_URI} for a cast list.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CAST;

        // The MIME type of the {@link #CONTENT_URI} for a single cast member.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CAST;

        // Name of database table for cast members
        public final static String TABLE_NAME = "cast";

        // Unique ID number for the section (only for use in the database table). Type: INTEGER
        public final static String _ID = BaseColumns._ID;

        // Id of the movie in witch the cast member appears. Type: INTEGER
        public final static String COLUMN_MOVIE_ID ="movie_id";

        // Name of the cast member. Type: TEXT
        public final static String COLUMN_ACTOR_NAME ="actor_name";

        // Name of the character. Type: TEXT
        public final static String COLUMN_CHARACTER_NAME ="character_name";

        // Id of the actor. Type: INTEGER
        public final static String COLUMN_ACTOR_ID = "actor_id";

        // Image profile path of the cast member. Type: TEXT
        public final static String COLUMN_IMAGE_PROFILE_PATH = "profile_path";
    }


    public static final String PATH_REVIEWS = "reviews";
    // Inner class that defines constant values for the reviews database table.
    // Each entry in the table represents a single review.
    public static abstract class ReviewsEntry implements BaseColumns {
        // The content URI to access the section data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_REVIEWS);

        // The MIME type of the {@link #CONTENT_URI} for a list of reviews.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        // The MIME type of the {@link #CONTENT_URI} for a single review.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        // Name of database table for movie reviews
        public final static String TABLE_NAME = "reviews";

        // Unique ID number for the section (only for use in the database table). Type: INTEGER
        public final static String _ID = BaseColumns._ID;

        // Id of the movie for witch the review was written. Type: INTEGER
        public final static String COLUMN_MOVIE_ID ="movie_id";

        // Author of the review. Type: TEXT
        public final static String COLUMN_AUTHOR ="author";

        // Content of the review. Type: TEXT
        public final static String COLUMN_CONTENT ="content";
    }


    public static final String PATH_VIDEOS = "videos";
    // Inner class that defines constant values for the videos database table.
    // Each entry in the table represents a single video.
    public static abstract class VideosEntry implements BaseColumns {
        // The content URI to access the section data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VIDEOS);

        // The MIME type of the {@link #CONTENT_URI} for a list of videos.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;

        // The MIME type of the {@link #CONTENT_URI} for a single video.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;

        // Name of database table for movie videos
        public final static String TABLE_NAME = "videos";

        // Unique ID number for the section (only for use in the database table). Type: INTEGER
        public final static String _ID = BaseColumns._ID;

        // Id of the movie for witch the video was made. Type: INTEGER
        public final static String COLUMN_MOVIE_ID ="movie_id";

        // Key of the video. Type: TEXT
        public final static String COLUMN_VIDEO_KEY ="video_key";

        // Name of the video. Type: TEXT
        public final static String COLUMN_VIDEO_NAME ="video_name";

        // Type of the video. Type: TEXT
        public final static String COLUMN_VIDEO_TYPE ="video_type";
    }
}
