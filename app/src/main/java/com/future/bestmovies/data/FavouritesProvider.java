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

import com.future.bestmovies.credits.Actor;
import com.future.bestmovies.credits.Credits;

import static com.future.bestmovies.data.FavouritesContract.*;

public class FavouritesProvider extends ContentProvider {
    // The instance of subclass FavouritesDbHelper of SQLiteOpenHelper, will be used to access our database.
    private FavouritesDbHelper mDbHelper;

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

    // URI matcher code for the content URI for the actors table
    private static final int ACTORS = 500;
    // URI matcher code for the content URI for a single actor in the actors table
    private static final int ACTOR_ID = 501;

    // URI matcher code for the content URI for the credits table
    private static final int CREDITS = 600;
    // URI matcher code for the content URI for a single credit in the credits table
    private static final int CREDIT_ID = 601;

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

        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_ACTORS, ACTORS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_ACTORS + "/#", ACTOR_ID);

        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_CREDITS, CREDITS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_CREDITS + "/#", CREDIT_ID);
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
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        switch (sUriMatcher.match(uri)) {
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
                // This will perform a query on the cast table where the movie_id equals (i.e. 157336) to return a
                // Cursor containing those rows of the table.
                cursor = database.query(CastEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case REVIEWS:
                cursor = database.query(ReviewsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case REVIEW_ID:
                selection = ReviewsEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ReviewsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case VIDEOS:
                cursor = database.query(VideosEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case VIDEO_ID:
                selection = VideosEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(VideosEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case ACTORS:
                cursor = database.query(ActorsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case ACTOR_ID:
                selection = ActorsEntry.COLUMN_ACTOR_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ActorsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case CREDITS:
                cursor = database.query(CreditsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case CREDIT_ID:
                selection = CreditsEntry.COLUMN_ACTOR_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(CreditsEntry.TABLE_NAME, projection, selection, selectionArgs,
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
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                return MovieDetailsEntry.CONTENT_LIST_TYPE;
            case MOVIE_ID:
                return MovieDetailsEntry.CONTENT_ITEM_TYPE;
            case CAST:
                return CastEntry.CONTENT_LIST_TYPE;
            case CAST_ID:
                return CastEntry.CONTENT_ITEM_TYPE;
            case REVIEWS:
                return ReviewsEntry.CONTENT_LIST_TYPE;
            case REVIEW_ID:
                return ReviewsEntry.CONTENT_ITEM_TYPE;
            case VIDEOS:
                return VideosEntry.CONTENT_LIST_TYPE;
            case VIDEO_ID:
                return VideosEntry.CONTENT_ITEM_TYPE;
            case ACTORS:
                return ActorsEntry.CONTENT_LIST_TYPE;
            case ACTOR_ID:
                return ActorsEntry.CONTENT_ITEM_TYPE;
            case CREDITS:
                return CreditsEntry.CONTENT_LIST_TYPE;
            case CREDIT_ID:
                return CreditsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + sUriMatcher.match(uri));
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                return insertFavourite(MovieDetailsEntry.TABLE_NAME, uri, contentValues);
            case ACTORS:
                return insertFavourite(ActorsEntry.TABLE_NAME, uri, contentValues);

            // In the present, the bellow cases are not used, but in the future they might be used,
            // so it's good to have all cases created
            case CAST:
                return insertFavourite(CastEntry.TABLE_NAME, uri, contentValues);
            case VIDEOS:
                return insertFavourite(VideosEntry.TABLE_NAME, uri, contentValues);
            case REVIEWS:
                return insertFavourite(ReviewsEntry.TABLE_NAME, uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // Insert a movie/cast member/review or video into the database with the given content values.
    // Return the new content URI for that specific row in the database.
    private Uri insertFavourite(String tableName, Uri uri, ContentValues contentValues) {
        // Get writable database and insert the given values
        long id = mDbHelper.getWritableDatabase().insert(tableName, null, contentValues);
        // Notify all listeners that the data has changed
        if (getContext() != null && id > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        // Return the id appended to the uri
        return ContentUris.withAppendedId(uri, id);
    }

    // Handles requests to insert a set of new rows in a selected table. In this app, we are going
    // to be inserting multiple rows of cast members, reviews or video data.
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        switch (sUriMatcher.match(uri)) {
            case CAST:
                return bulkInsertData(CastEntry.TABLE_NAME, uri, values);

            case REVIEWS:
                return bulkInsertData(ReviewsEntry.TABLE_NAME, uri, values);

            case VIDEOS:
                return bulkInsertData(VideosEntry.TABLE_NAME, uri, values);

            case CREDITS:
                return bulkInsertData(CreditsEntry.TABLE_NAME, uri, values);

            default:
                return super.bulkInsert(uri, values);
        }
    }

    private int bulkInsertData (String tableName, Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.beginTransaction();
        int rowsInserted = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    rowsInserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (getContext() != null && rowsInserted > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsInserted;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                // Delete all rows that match the selection and selection args
                return deleteFavourite(MovieDetailsEntry.TABLE_NAME, uri, selection, selectionArgs);

            case MOVIE_ID:
                // For the MOVIE_ID code, extract out the ID from the URI,
                // so we know which row to delete. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = MovieDetailsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // Delete a single row given by the ID in the URI
                return deleteFavourite(MovieDetailsEntry.TABLE_NAME, uri, selection, selectionArgs);

            case CAST:
                // Delete all rows that match the selection and selection args
                return deleteFavourite(CastEntry.TABLE_NAME, uri, selection, selectionArgs);

            case REVIEWS:
                // Delete all rows that match the selection and selection args
                return deleteFavourite(ReviewsEntry.TABLE_NAME, uri, selection, selectionArgs);

            case VIDEOS:
                // Delete all rows that match the selection and selection args
                return deleteFavourite(VideosEntry.TABLE_NAME, uri, selection, selectionArgs);

            case ACTORS:
                // Delete all rows that match the selection and selection args
                return deleteFavourite(ActorsEntry.TABLE_NAME, uri, selection, selectionArgs);

            case CREDITS:
                // Delete all rows that match the selection and selection args
                return deleteFavourite(CreditsEntry.TABLE_NAME, uri, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    private int deleteFavourite(String tableName, Uri uri, String selection, String[] selectionArgs) {
        // Get writable database, delete and return the number of database rows affected by the delete statement
        int rowsDeleted = mDbHelper.getWritableDatabase().delete(tableName, selection, selectionArgs);

        // If 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed
        if (getContext() != null && rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows affected by the delete statement
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case ACTORS:
                return updateActor(uri, contentValues, selection, selectionArgs);
            case ACTOR_ID:
                // For the ACTOR_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "COLUMN_ACTOR_ID=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ActorsEntry.COLUMN_ACTOR_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateActor(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateActor(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ActorsEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed (uri example: content://com.future.bestmovies/actors/#)
        if (getContext() != null && rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }
}
