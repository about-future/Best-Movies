package com.future.bestmovies;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.content.CursorLoader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.future.bestmovies.data.Cast;
import com.future.bestmovies.data.CastAdapter;
import com.future.bestmovies.data.CastLoader;
import com.future.bestmovies.data.Movie;
import com.future.bestmovies.data.Review;
import com.future.bestmovies.data.ReviewLoader;
import com.future.bestmovies.data.Video;
import com.future.bestmovies.data.VideoAdapter;
import com.future.bestmovies.data.VideoLoader;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.future.bestmovies.utils.StringUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.future.bestmovies.data.FavouritesContract.*;


public class DetailsActivity extends AppCompatActivity implements VideoAdapter.ListItemClickListener {
    private static final int CAST_LOADER_ID = 423;
    private static final int REVIEWS_LOADER_ID = 435;
    private static final int VIDEOS_LOADER_ID = 594;
    private static final int FAVOURITE_LOADER_ID = 516;

    public static final String[] MOVIE_CHECK_PROJECTION = {MovieDetailsEntry.COLUMN_MOVIE_ID};

    public static final String[] MOVIE_DETAILED_PROJECTION = {
            MovieDetailsEntry.COLUMN_MOVIE_ID,
            MovieDetailsEntry.COLUMN_BACKDROP_PATH,
            MovieDetailsEntry.COLUMN_GENRES,
            MovieDetailsEntry.COLUMN_PLOT,
            MovieDetailsEntry.COLUMN_POSTER_PATH,
            MovieDetailsEntry.COLUMN_RATINGS,
            MovieDetailsEntry.COLUMN_RELEASE_DATE,
            MovieDetailsEntry.COLUMN_TITLE
    };

    public static final String MOVIE_OBJECT = "movie";
    private static final String MOVIE_CAST = "movie_cast";
    private static final String CAST_POSITION = "cast_position";
    private static final String VIDEOS_POSITION = "videos_position";
    public static final String MOVIE_REVIEWS = "movie_reviews";
    public static final String MOVIE_VIDEOS = "movie_videos";
    public static final String MOVIE_ID = "movie_id";
    public static final String MOVIE_TITLE = "movie_title";
    public static final String MOVIE_BACKDROP = "movie_backdrop";

    private Movie mSelectedMovie;
    private int mMovieId;
    private ImageView mMovieBackdropImageView;
    private TextView posterErrorTextView;
    private TextView movieGenreTextView;

    private TextView mCastMessagesTextView;
    private int mCastPosition = RecyclerView.NO_POSITION;
    private CastAdapter mCastAdapter;
    private LinearLayoutManager mCastLayoutManager;
    private RecyclerView mCastRecyclerView;
    private ProgressBar mCastProgressBar;
    private ArrayList<Cast> mCast;
    private ImageView mNoCastImageView;
    private ImageView mNoCastConnectionImageView;

    private ConstraintLayout mFirstReviewLayout;
    private TextView mFirstReviewAuthorTextView;
    private TextView mFirstReviewContentTextView;
    private ProgressBar mFirstReviewProgressBar;
    private TextView mFirstReviewMessagesTextView;
    private TextView mSeeAllReviewsTextView;
    private ArrayList<Review> mReviews;
    private ImageView mNoReviewsImageView;
    private ImageView mNoReviewsConnectionImageView;

    private ArrayList<Video> mVideos;
    private RecyclerView mVideosRecyclerView;
    private LinearLayoutManager mVideosLayoutManager;
    private ProgressBar mVideosProgressBar;
    private VideoAdapter mVideosAdapter;
    private TextView mVideosMessagesTextView;
    private int mVideosPosition = RecyclerView.NO_POSITION;
    private ImageView mNoVideosImageView;

    private boolean mIsFavourite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // We initialize and set the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // If we have an instance saved and contains our movie object, we use it to populate our UI
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_OBJECT)) {
            mSelectedMovie = savedInstanceState.getParcelable(MOVIE_OBJECT);
        } else {
            // Otherwise, we check our intent and see if there is a Movie object or a movieId passed
            // from MainActivity, so we can populate our UI. If there isn't we close this activity
            // and display a toast message.
            Intent intent = getIntent();
            if (intent != null) {
                // If MainActivity passed a movie object
                if (intent.hasExtra(MOVIE_OBJECT)) {
                    mSelectedMovie = intent.getParcelableExtra(MOVIE_OBJECT);
                } else if (intent.hasExtra(MOVIE_ID)) {
                    // If MainActivity passed a movieId, get the ID and create a Movie object,
                    // based on that ID.
                    mMovieId = intent.getIntExtra(MOVIE_ID, 297762);

                    Log.v("ID", String.valueOf(mMovieId));

                    Cursor cursor = getContentResolver().query(MovieDetailsEntry.buildMovieUriWithId(mMovieId),
                            MOVIE_DETAILED_PROJECTION,
                            null,
                            null,
                            null);

                    if (cursor != null && cursor.moveToFirst()) {
                        // Find the columns of movie attributes that we're interested in
                        int movieIdColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_MOVIE_ID);
                        int backdropColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_BACKDROP_PATH);
                        int genresColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_GENRES);
                        int plotColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_PLOT);
                        int posterColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_POSTER_PATH);
                        int ratingsColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_RATINGS);
                        int releaseDateColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_RELEASE_DATE);
                        int titleColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_TITLE);

                        mSelectedMovie = new Movie(
                                cursor.getInt(movieIdColumnIndex),
                                cursor.getDouble(ratingsColumnIndex),
                                cursor.getString(titleColumnIndex),
                                cursor.getString(posterColumnIndex),
                                cursor.getString(backdropColumnIndex),
                                cursor.getString(plotColumnIndex),
                                cursor.getString(releaseDateColumnIndex),
                                StringUtils.stringToIntArray(cursor.getString(genresColumnIndex))
                        );

                        cursor.close();
                    }

                    //getLoaderManager().restartLoader(FAVOURITE_LOADER_ID, null, favouriteMovieResultLoaderListener);
                } else {
                    closeOnError();
                }
            } else {
                closeOnError();
            }
        }

        // If the Movie object contains no data, we close this activity and display a toast message
        if (mSelectedMovie == null) {
            // Movies data unavailable
            closeOnError();
            return;
        }

        // Check if this movie is a favourite
        Cursor movieCheck = getContentResolver().query(MovieDetailsEntry.buildMovieUriWithId(mSelectedMovie.getMovieId()),
                MOVIE_CHECK_PROJECTION,
                null,
                null,
                null);

        if (movieCheck != null && movieCheck.moveToFirst()) {
            mIsFavourite = true;
            movieCheck.close();
        } else {
            mIsFavourite = false;
        }
        Log.v("MOVIE IS FAVOURITE", String.valueOf(mIsFavourite));

        // BACKDROP
        mMovieBackdropImageView = findViewById(R.id.details_backdrop_iv);
        Picasso.with(this)
                .load(ImageUtils.buildImageUrl(
                        this,
                        mSelectedMovie.getBackdropPath(),
                        ImageUtils.BACKDROP))
                .error(R.drawable.ic_landscape)
                .into(mMovieBackdropImageView);

        // MOVIE TITLE
        // Set the title of our activity as the movie title
        setTitle(mSelectedMovie.getMovieTitle());

        // GENRE
        // Generate and set movie genres
        movieGenreTextView = findViewById(R.id.details_genre_tv);
        movieGenreTextView.setText(StringUtils.movieGenresAsString(this, mSelectedMovie.getGenreIds()));

        // POSTER
        // Poster error message will be used if no poster is available or if no internet connection
        final ImageView moviePosterImageView = findViewById(R.id.details_poster_iv);
        posterErrorTextView = findViewById(R.id.poster_error_tv);
        // Fetch movie poster, if it's available
        Picasso.with(this)
                .load(ImageUtils.buildImageUrl(
                        this,
                        mSelectedMovie.getPosterPath(),
                        ImageUtils.POSTER))
                .placeholder(R.drawable.no_poster)
                .error(R.drawable.ic_local_movies)
                .into(moviePosterImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        posterErrorTextView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        posterErrorTextView.setVisibility(View.VISIBLE);
                        // If there isn't a network connection, we show a "no connection" message
                        if (!NetworkUtils.isConnected(getApplicationContext())) {
                            posterErrorTextView.setText(getString(R.string.no_connection));
                        } else {
                            // Otherwise, we show "no_poster" message
                            posterErrorTextView.setText(getString(R.string.no_poster));
                        }
                        // Set poster content description for error case
                        moviePosterImageView.setContentDescription(getString(R.string.no_poster));
                    }
                });

        // RATINGS
        TextView movieRatingTextView = findViewById(R.id.details_rating_tv);
        movieRatingTextView.setText(String.valueOf(mSelectedMovie.getVoteAverage()));

        // RELEASE DATE
        TextView movieReleaseDateTextView = findViewById(R.id.details_release_date_tv);
        movieReleaseDateTextView.setText(mSelectedMovie.getReleaseDate());

        // PLOT
        TextView moviePlotTextView = findViewById(R.id.details_plot_tv);
        moviePlotTextView.setText(mSelectedMovie.getOverview());

        // CAST
        mCastMessagesTextView = findViewById(R.id.cast_messages_tv);
        mCastMessagesTextView.setText(R.string.loading);
        mCastRecyclerView = findViewById(R.id.cast_rv);
        mCastProgressBar = findViewById(R.id.loading_cast_pb);
        // The layout manager for our Cast RecyclerView will be a LinerLayout, so we can display
        // our cast on a single line, horizontally
        mCastLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mCastRecyclerView.setLayoutManager(mCastLayoutManager);
        mCastRecyclerView.setHasFixedSize(true);
        mCastAdapter = new CastAdapter(this, new ArrayList<Cast>());
        mCastRecyclerView.setAdapter(mCastAdapter);
        mNoCastImageView = findViewById(R.id.no_cast_iv);
        mNoCastConnectionImageView = findViewById(R.id.no_cast_connection_iv);

        // Show the Cast progress bar and hide the Cast RecyclerView
        hideCast();
        // Check for saved data or fetch movie cast
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_CAST)) {
            mCast = savedInstanceState.getParcelableArrayList(MOVIE_CAST);
            // If cast is not null, data from server was previously fetched successfully
            if (mCast != null) {
                // If cast is not empty, use the saved cast and repopulate the cast section
                if (!mCast.isEmpty()) {
                    mCastAdapter = new CastAdapter(this, mCast);
                    mCastRecyclerView.setAdapter(mCastAdapter);
                    if (savedInstanceState.containsKey(CAST_POSITION)) {
                        mCastPosition = savedInstanceState.getInt(CAST_POSITION);
                    }
                }
                populateCast(mCast);
            } else {
                // Otherwise, there might be an error while accessing the server
                // Check the connection and if connected try fetching cast again
                fetchCast();
            }
        } else {
            // Otherwise, no previous data was saved before, so loader has to be initialised
            fetchCast();
        }

        // REVIEWS
        mFirstReviewLayout = findViewById(R.id.first_review_layout);
        mFirstReviewAuthorTextView = findViewById(R.id.first_review_author_tv);
        mFirstReviewContentTextView = findViewById(R.id.first_review_content_tv);
        mFirstReviewProgressBar = findViewById(R.id.loading_first_review_pb);
        mFirstReviewMessagesTextView = findViewById(R.id.first_review_messages_tv);
        mFirstReviewMessagesTextView.setText(R.string.loading);
        mSeeAllReviewsTextView = findViewById(R.id.see_all_reviews_tv);
        mNoReviewsImageView = findViewById(R.id.no_reviews_iv);
        mNoReviewsConnectionImageView = findViewById(R.id.no_review_connection_iv);

        // Show the Review progress bar and hide the firstReview layout
        hideReviews();
        // Check for saved data or fetch movie reviews
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_REVIEWS)) {
            mReviews = savedInstanceState.getParcelableArrayList(MOVIE_REVIEWS);
            // If mReview is not null, data from server was previously fetched successfully
            if (mReviews != null) {
                // If review is not empty, use the saved reviews and repopulate the reviews section
                populateReviews(mReviews);
            } else {
                // Otherwise, there might be an error while accessing the server
                // Check the connection and if connected try fetching reviews again
                fetchReviews();
            }
        } else {
            // Otherwise, no previous data was saved before, so loader has to be initialised
            fetchReviews();
        }

        // VIDEOS
        mVideosMessagesTextView = findViewById(R.id.videos_messages_tv);
        mVideosMessagesTextView.setText(R.string.loading);
        mVideosRecyclerView = findViewById(R.id.videos_rv);
        mVideosProgressBar = findViewById(R.id.loading_videos_pb);
        // The layout manager for our Videos RecyclerView will be a LinerLayout, so we can display
        // our videos on a single line, horizontally
        mVideosLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mVideosRecyclerView.setLayoutManager(mVideosLayoutManager);
        mVideosRecyclerView.setHasFixedSize(true);
        mVideosAdapter = new VideoAdapter(this, new ArrayList<Video>(), this);
        mVideosRecyclerView.setAdapter(mVideosAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mVideosRecyclerView);
        mNoVideosImageView = findViewById(R.id.no_videos_iv);

        // Show the Videos progress bar and hide the Videos RecyclerView
        hideVideos();
        // Check for saved data or fetch movie videos
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_VIDEOS)) {
            mVideos = savedInstanceState.getParcelableArrayList(MOVIE_VIDEOS);
            // If mVideos is not null, data from server was previously fetched successfully
            if (mVideos != null) {
                // If mVideos is not empty, use the saved videos and repopulate the video section
                if (!mVideos.isEmpty()) {
                    mVideosAdapter = new VideoAdapter(this, mVideos, this);
                    mVideosRecyclerView.setAdapter(mVideosAdapter);
                    if (savedInstanceState.containsKey(VIDEOS_POSITION)) {
                        mVideosPosition = savedInstanceState.getInt(VIDEOS_POSITION);
                    }
                }
                populateVideos(mVideos);
            } else {
                // Otherwise, there might be an error while accessing the server
                // Check the connection and if connected try fetching videos again
                fetchVideos();
            }
        } else {
            // Otherwise, no previous data was saved before, so loader has to be initialised
            fetchVideos();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);

        MenuItem favouritesMenuItem = menu.findItem(R.id.action_favourites);
        if (mIsFavourite) {
            DrawableCompat.setTint(favouritesMenuItem.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorHeart));
        } else {
            DrawableCompat.setTint(favouritesMenuItem.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.action_favourites) {
            if (mIsFavourite) {
                deleteFavourite(mSelectedMovie, item);
            } else {
                insertMovie(mSelectedMovie, item);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.details_error_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Movie Details
        outState.putParcelable(MOVIE_OBJECT, mSelectedMovie);

        // Cast
        outState.putParcelableArrayList(MOVIE_CAST, mCast);
        outState.putInt(CAST_POSITION, mCastLayoutManager.findFirstCompletelyVisibleItemPosition());

        // Reviews
        outState.putParcelableArrayList(MOVIE_REVIEWS, mReviews);

        // Videos
        outState.putParcelableArrayList(MOVIE_VIDEOS, mVideos);
        if (mVideosLayoutManager.findFirstCompletelyVisibleItemPosition() != RecyclerView.NO_POSITION) {
            outState.putInt(VIDEOS_POSITION, mVideosLayoutManager.findFirstCompletelyVisibleItemPosition());
        } else {
            outState.putInt(VIDEOS_POSITION, mVideosLayoutManager.findFirstVisibleItemPosition());
        }

        super.onSaveInstanceState(outState);
    }

    // Hide the progress bar and show cast
    private void showCast() {
        mCastRecyclerView.setVisibility(View.VISIBLE);
        mCastProgressBar.setVisibility(View.INVISIBLE);
        mCastMessagesTextView.setVisibility(View.INVISIBLE);
        mNoCastImageView.setVisibility(View.INVISIBLE);
        mNoCastConnectionImageView.setVisibility(View.INVISIBLE);
    }

    // Show progress bar and hide cast
    private void hideCast() {
        mCastRecyclerView.setVisibility(View.GONE);
        mCastProgressBar.setVisibility(View.VISIBLE);
        mCastMessagesTextView.setVisibility(View.VISIBLE);
        mNoCastImageView.setVisibility(View.INVISIBLE);
        mNoCastConnectionImageView.setVisibility(View.INVISIBLE);
    }

    // Hide the progress bar and show reviews
    private void showReviews() {
        mFirstReviewLayout.setVisibility(View.VISIBLE);
        mFirstReviewProgressBar.setVisibility(View.INVISIBLE);
        mFirstReviewMessagesTextView.setVisibility(View.INVISIBLE);
        mNoReviewsImageView.setVisibility(View.INVISIBLE);
        mNoReviewsConnectionImageView.setVisibility(View.INVISIBLE);
    }

    // Show progress bar and hide reviews
    private void hideReviews() {
        mFirstReviewLayout.setVisibility(View.INVISIBLE);
        mFirstReviewProgressBar.setVisibility(View.VISIBLE);
        mFirstReviewMessagesTextView.setVisibility(View.VISIBLE);
        mSeeAllReviewsTextView.setVisibility(View.INVISIBLE);
        mNoReviewsImageView.setVisibility(View.INVISIBLE);
        mNoReviewsConnectionImageView.setVisibility(View.INVISIBLE);
    }

    // Hide the progress bar and show videos
    private void showVideos() {
        mVideosRecyclerView.setVisibility(View.VISIBLE);
        mVideosProgressBar.setVisibility(View.INVISIBLE);
        mVideosMessagesTextView.setVisibility(View.INVISIBLE);
        mNoVideosImageView.setVisibility(View.INVISIBLE);
    }

    // Show progress bar and hide videos
    private void hideVideos() {
        mVideosRecyclerView.setVisibility(View.GONE);
        mVideosProgressBar.setVisibility(View.VISIBLE);
        mVideosMessagesTextView.setVisibility(View.VISIBLE);
        mNoVideosImageView.setVisibility(View.INVISIBLE);
    }

    private void fetchCast() {
        if (NetworkUtils.isConnected(getApplicationContext())) {
            getLoaderManager().restartLoader(CAST_LOADER_ID, null, castResultLoaderListener);
        } else {
            // Otherwise, hide progress bar and show "No connection available" message
            mCastMessagesTextView.setVisibility(View.VISIBLE);
            mCastMessagesTextView.setText(R.string.no_connection);
            mCastProgressBar.setVisibility(View.INVISIBLE);
            mNoCastImageView.setVisibility(View.INVISIBLE);
            mNoCastConnectionImageView.setVisibility(View.VISIBLE);
        }
    }

    private void fetchReviews() {
        if (NetworkUtils.isConnected(getApplicationContext())) {
            getLoaderManager().restartLoader(REVIEWS_LOADER_ID, null, reviewsResultLoaderListener);
        } else {
            // Otherwise, hide progress bar and show "No connection available" message
            mFirstReviewLayout.setVisibility(View.INVISIBLE);
            mSeeAllReviewsTextView.setVisibility(View.INVISIBLE);
            mFirstReviewMessagesTextView.setVisibility(View.VISIBLE);
            mFirstReviewMessagesTextView.setText(R.string.no_connection);
            mFirstReviewProgressBar.setVisibility(View.INVISIBLE);
            mNoReviewsImageView.setVisibility(View.INVISIBLE);
            mNoReviewsConnectionImageView.setVisibility(View.VISIBLE);
        }
    }

    private void fetchVideos() {
        if (NetworkUtils.isConnected(getApplicationContext())) {
            getLoaderManager().restartLoader(VIDEOS_LOADER_ID, null, videoResultLoaderListener);
        } else {
            // Otherwise, hide progress bar and show "No connection available" message
            mVideosMessagesTextView.setVisibility(View.VISIBLE);
            mVideosMessagesTextView.setText(R.string.no_connection);
            mVideosProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private LoaderManager.LoaderCallbacks<ArrayList<Cast>> castResultLoaderListener =
            new LoaderManager.LoaderCallbacks<ArrayList<Cast>>() {
                @Override
                public Loader<ArrayList<Cast>> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case CAST_LOADER_ID:
                            // If the loaded id matches ours, return a new cast movie loader
                            return new CastLoader(getApplicationContext(), String.valueOf(mSelectedMovie.getMovieId()));
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(Loader<ArrayList<Cast>> loader, ArrayList<Cast> movieCast) {
                    mCast = movieCast;
                    // Populate cast section
                    populateCast(movieCast);
                }

                @Override
                public void onLoaderReset(Loader<ArrayList<Cast>> loader) {
                    mCastAdapter.swapCast(new ArrayList<Cast>() {
                    });
                }
            };

    private LoaderManager.LoaderCallbacks<ArrayList<Review>> reviewsResultLoaderListener =
            new LoaderManager.LoaderCallbacks<ArrayList<Review>>() {
                @Override
                public Loader<ArrayList<Review>> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case REVIEWS_LOADER_ID:
                            // If the loaded id matches ours, return a new movie review loader
                            return new ReviewLoader(getApplicationContext(), String.valueOf(mSelectedMovie.getMovieId()));
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(Loader<ArrayList<Review>> loader, ArrayList<Review> movieReviews) {
                    mReviews = movieReviews;
                    // Populate reviews section
                    populateReviews(movieReviews);
                }

                @Override
                public void onLoaderReset(Loader<ArrayList<Review>> loader) {
                    // Clear TextViews
                    mFirstReviewAuthorTextView.setText(null);
                    mFirstReviewContentTextView.setText(null);
                }
            };

    private LoaderManager.LoaderCallbacks<ArrayList<Video>> videoResultLoaderListener =
            new LoaderManager.LoaderCallbacks<ArrayList<Video>>() {
                @Override
                public Loader<ArrayList<Video>> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case VIDEOS_LOADER_ID:
                            // If the loaded id matches ours, return a new movie review loader
                            return new VideoLoader(getApplicationContext(), String.valueOf(mSelectedMovie.getMovieId()));
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(Loader<ArrayList<Video>> loader, final ArrayList<Video> movieVideos) {
                    mVideos = movieVideos;
                    // Populate videos section
                    populateVideos(movieVideos);
                }

                @Override
                public void onLoaderReset(Loader<ArrayList<Video>> loader) {
                    mVideosAdapter.swapVideos(new ArrayList<Video>() {
                    });
                }
            };

    private LoaderManager.LoaderCallbacks<Cursor> favouriteMovieResultLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
                    Uri uri = MovieDetailsEntry.CONTENT_URI.buildUpon()
                            .appendPath(Integer.toString(mMovieId))
                            .build();

                    Log.v("URI", uri.toString());

                    switch (loaderId) {
                        case FAVOURITE_LOADER_ID:
                            return new CursorLoader(getApplicationContext(),
                                    uri,
                                    MOVIE_DETAILED_PROJECTION,
                                    null,
                                    null,
                                    null);

                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                    if (cursor != null && cursor.moveToFirst()) {
                        //if (cursor == null) return;

                        Log.v("LOAD FINISHED", "CURSOR HAS DATA");

                        //cursor.moveToFirst();

                        // Find the columns of movie attributes that we're interested in
                        int movieIdColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_MOVIE_ID);
                        int backdropColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_BACKDROP_PATH);
                        int genresColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_GENRES);
                        int plotColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_PLOT);
                        int posterColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_POSTER_PATH);
                        int ratingsColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_RATINGS);
                        int releaseDateColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_RELEASE_DATE);
                        int titleColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_TITLE);

                        // Set the extracted value from the Cursor for the given column index and use each
                        // value to create a Movie object
                        mSelectedMovie = new Movie(
                                cursor.getInt(movieIdColumnIndex),
                                cursor.getDouble(ratingsColumnIndex),
                                cursor.getString(titleColumnIndex),
                                cursor.getString(posterColumnIndex),
                                cursor.getString(backdropColumnIndex),
                                cursor.getString(plotColumnIndex),
                                cursor.getString(releaseDateColumnIndex),
                                StringUtils.stringToIntArray(cursor.getString(genresColumnIndex))
                        );
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    private void populateCast(ArrayList<Cast> movieCast) {
        if (movieCast != null) {
            mCastAdapter.swapCast(movieCast);

            // If our RecyclerView has is not position, we assume the first position in the list
            // and set the RecyclerView a the beginning of our results
            if (mCastPosition == RecyclerView.NO_POSITION) {
                mCastPosition = 0;
            }
            mCastRecyclerView.smoothScrollToPosition(mCastPosition);

            // If the movieCast has data
            if (movieCast.size() != 0) {
                // Show movie cast
                showCast();
            } else {
                // Otherwise, hide progress bar and show "No cast available" message
                mCastMessagesTextView.setVisibility(View.VISIBLE);
                mCastMessagesTextView.setText(R.string.no_cast);
                mCastProgressBar.setVisibility(View.INVISIBLE);
                mNoCastImageView.setVisibility(View.VISIBLE);
                mNoCastConnectionImageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void populateReviews(final ArrayList<Review> movieReviews) {
        if (movieReviews != null) {
            if (movieReviews.size() != 0) {
                if (movieReviews.size() > 1) {
                    mSeeAllReviewsTextView.setVisibility(View.VISIBLE);
                    mSeeAllReviewsTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent reviewsIntent = new Intent(getApplicationContext(), ReviewsActivity.class);
                            reviewsIntent.putParcelableArrayListExtra(MOVIE_REVIEWS, movieReviews);
                            reviewsIntent.putExtra(MOVIE_TITLE, mSelectedMovie.getMovieTitle());
                            reviewsIntent.putExtra(MOVIE_BACKDROP, mSelectedMovie.getBackdropPath());
                            startActivity(reviewsIntent);
                        }
                    });
                } else {
                    mSeeAllReviewsTextView.setVisibility(View.GONE);
                }
                // Show movie reviews
                showReviews();
                mFirstReviewAuthorTextView.setText(movieReviews.get(0).getReviewAuthor());
                mFirstReviewContentTextView.setText(movieReviews.get(0).getReviewContent());
            } else {
                // Otherwise, hide progress bar and show "No reviews available" message
                hideReviews();
                mFirstReviewMessagesTextView.setVisibility(View.VISIBLE);
                mFirstReviewMessagesTextView.setText(R.string.no_reviews);
                mFirstReviewProgressBar.setVisibility(View.INVISIBLE);
                mNoReviewsImageView.setVisibility(View.VISIBLE);
                mNoReviewsConnectionImageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void populateVideos(ArrayList<Video> movieVideos) {
        if (movieVideos != null) {
            mVideosAdapter.swapVideos(movieVideos);

            // If our RecyclerView has is not position, we assume the first position in the list
            // and set the RecyclerView a the beginning of our results
            if (mVideosPosition == RecyclerView.NO_POSITION) {
                mVideosPosition = 0;
            }
            mVideosRecyclerView.smoothScrollToPosition(mVideosPosition);


            // If the movieVideo has data
            if (movieVideos.size() != 0) {
                // Show movie videos
                showVideos();
            } else {
                // Otherwise, hide progress bar and show "No videos available" message
                mVideosRecyclerView.setVisibility(View.GONE);
                mVideosMessagesTextView.setVisibility(View.VISIBLE);
                mVideosMessagesTextView.setText(R.string.no_videos);
                mVideosProgressBar.setVisibility(View.INVISIBLE);
                mNoVideosImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onListItemClick(Video videoClicked) {
        startActivity(new Intent(
                Intent.ACTION_VIEW,
                NetworkUtils.buildVideoUri(videoClicked.getVideoKey())));
    }

    private void insertMovie(Movie selectedMovie, MenuItem item) {
        ContentValues values = new ContentValues();
        values.put(MovieDetailsEntry.COLUMN_MOVIE_ID, selectedMovie.getMovieId());
        values.put(MovieDetailsEntry.COLUMN_BACKDROP_PATH, selectedMovie.getBackdropPath());
        values.put(MovieDetailsEntry.COLUMN_GENRES, StringUtils.intArrayToString(selectedMovie.getGenreIds()));
        values.put(MovieDetailsEntry.COLUMN_PLOT, selectedMovie.getOverview());
        values.put(MovieDetailsEntry.COLUMN_POSTER_PATH, selectedMovie.getPosterPath());
        values.put(MovieDetailsEntry.COLUMN_RATINGS, selectedMovie.getVoteAverage());
        values.put(MovieDetailsEntry.COLUMN_RELEASE_DATE, selectedMovie.getReleaseDate());
        values.put(MovieDetailsEntry.COLUMN_TITLE, selectedMovie.getMovieTitle());

        Uri responseUri = getContentResolver().insert(MovieDetailsEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (responseUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this,
                    getString(R.string.favourite_insert_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Log.v("INSERT MOVIE", "MOVIE INSERTED IN " + MovieDetailsEntry.TABLE_NAME + " WITH URI: " + responseUri);
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this,
                    getString(R.string.favourite_insert_successful),
                    Toast.LENGTH_SHORT).show();
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorHeart));
        }
    }

    private void deleteFavourite(Movie mSelectedMovie, MenuItem item) {
        int rowsDeleted = getContentResolver().delete(MovieDetailsEntry.CONTENT_URI,
                MovieDetailsEntry.COLUMN_MOVIE_ID + " =?",
                new String[]{String.valueOf(mSelectedMovie.getMovieId())});

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were affected, then there was an error with the delete.
            //DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorHeart));
            Toast.makeText(this, getString(R.string.favourite_delete_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
            Toast.makeText(this, getString(R.string.favourite_delete_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }
}