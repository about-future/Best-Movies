package com.future.bestmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.future.bestmovies.data.FavouritesContract.*;

public class FavouritesProvider extends ContentProvider {
    // The instance of subclass FavouritesDbHelper of SQLiteOpenHelper, will be used to access our database.
    private FavouritesDbHelper mDbHelper;

    //Tag for the log messages
    public static final String LOG_TAG = FavouritesProvider.class.getSimpleName();

    // URI matcher code for the content URI for the movie details table
    private static final int MOVIES = 100;
    // URI matcher code for the content URI for a single movie in the movies details table
    private static final int MOVIE_ID = 101;

    // URI matcher code for the content URI for the cast table
    private static final int CAST = 200;
    // URI matcher code for the content URI for a single actor in the cast table
    private static final int CAST_ID = 201;

    // URI matcher code for the content URI for the reviews table
    private static final int REVIEWS = 300;
    // URI matcher code for the content URI for a single review in the reviews table
    private static final int REVIEW_ID = 301;

    // URI matcher code for the content URI for the videos table
    private static final int VIDEOS = 400;
    // URI matcher code for the content URI for a single video in the videos table
    private static final int VIDEO_ID = 401;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_MOVIES, MOVIES);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_MOVIES + "/#", MOVIE_ID);

        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_CAST, CAST);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_CAST + "/#", CAST_ID);

        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_REVIEWS, REVIEWS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_REVIEWS + "/#", REVIEW_ID);

        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_VIDEOS, VIDEOS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_VIDEOS + "/#", VIDEO_ID);
    }

    // Initialize the provider and the database helper object.
    @Override
    public boolean onCreate() {
        // Instantiate our subclass of SQLiteOpenHelper and pass the context, which is the current activity.
        mDbHelper = new FavouritesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                // For the MOVIES code, query the movies table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the movies table.
                cursor = database.query(MovieDetailsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case MOVIE_ID:
                // For the MOVIE_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.future.bestmovies/movies/157336",
                // the selection will be "movie_id=?" and the selection argument will be a
                // String array containing the actual ID of 157336 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = MovieDetailsEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the movie_details table where the movie_id equals 157336 to return a
                // Cursor containing that row of the table.
                cursor = database.query(MovieDetailsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CAST:
                cursor = database.query(CastEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CAST_ID:
                selection = CastEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the cast table where the movie_id equals 157336 to return a
                // Cursor containing that row of the table.
                cursor = database.query(CastEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown Uri " + uri);
        }

        // Set notification URI on the Cursor, so we know what content URI the Cursor was created for.
        // If the data in this URI changes, than we know we need to update the Cursor.
        if (getContext() != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return MovieDetailsEntry.CONTENT_LIST_TYPE;
            case MOVIE_ID:
                return MovieDetailsEntry.CONTENT_ITEM_TYPE;
            case CAST:
                return CastEntry.CONTENT_LIST_TYPE;
            case CAST_ID:
                return CastEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return insertFavourite(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // Insert a movie into the database with the given content values. Return the new content URI
    // for that specific row in the database.
    private Uri insertFavourite(Uri uri, ContentValues contentValues) {

        // Get writable database and insert the new movie with the given values
        long id = mDbHelper.getWritableDatabase().insert(MovieDetailsEntry.TABLE_NAME, null, contentValues);

        // Notify all listeners that the data has changed
        if (id > 0) getContext().getContentResolver().notifyChange(uri, null);

        // Return the id appended to the uri
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                // Delete all rows that match the selection and selection args
                return deleteFavourite(uri, selection, selectionArgs);
            case MOVIE_ID:
                // For the MOVIE_ID code, extract out the ID from the URI,
                // so we know which row to delete. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = MovieDetailsEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // Delete a single row given by the ID in the URI
                return deleteFavourite(uri, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    private int deleteFavourite(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database, delete and return the number of database rows affected by the delete statement
        int rowsDeleted = mDbHelper.getWritableDatabase().delete(MovieDetailsEntry.TABLE_NAME, selection, selectionArgs);

        // If 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed
        if (rowsDeleted != 0) getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows affected by the delete statement
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return 0;
    }
}