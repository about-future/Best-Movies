package com.future.bestmovies;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
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
import android.text.TextUtils;
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
import com.future.bestmovies.data.MovieDetails;
import com.future.bestmovies.data.MovieDetailsLoader;
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


public class DetailsActivity extends AppCompatActivity implements
        VideoAdapter.ListItemClickListener, CastAdapter.ListItemClickListener {
    private static final int MOVIE_DETAILS_LOADER_ID = 805;
    private static final int CAST_LOADER_ID = 423;
    private static final int REVIEWS_LOADER_ID = 435;
    private static final int VIDEOS_LOADER_ID = 594;
    private static final int FAVOURITE_LOADER_ID = 516;
    private static final int CHECK_IF_FAVOURITE_MOVIE_LOADER_ID = 473;

    // Query projection used to check if the movie is a favourite or not
    public static final String[] MOVIE_CHECK_PROJECTION = {MovieDetailsEntry.COLUMN_MOVIE_ID};

    // Query projection used to retrieve movie details
    public static final String[] MOVIE_DETAILED_PROJECTION = {
            MovieDetailsEntry.COLUMN_MOVIE_ID,
            MovieDetailsEntry.COLUMN_BACKDROP_PATH,
            MovieDetailsEntry.COLUMN_GENRES,
            MovieDetailsEntry.COLUMN_LANGUAGE,
            MovieDetailsEntry.COLUMN_PLOT,
            MovieDetailsEntry.COLUMN_POSTER_PATH,
            MovieDetailsEntry.COLUMN_RATINGS,
            MovieDetailsEntry.COLUMN_RELEASE_DATE,
            MovieDetailsEntry.COLUMN_RUNTIME,
            MovieDetailsEntry.COLUMN_TITLE
    };

    // Instance Keys
    public static final String MOVIE_OBJECT_KEY = "movie";
    private static final String MOVIE_CAST_KEY = "movie_cast";
    public static final String MOVIE_REVIEWS_KEY = "movie_reviews";
    public static final String MOVIE_VIDEOS_KEY = "movie_videos";

    private static final String IS_FAVOURITE_KEY = "is_favourite";
    public static final String ACTOR_ID = "actor_id";
    private static final String CAST_POSITION_KEY = "cast_position";
    private static final String VIDEOS_POSITION_KEY = "videos_position";

    public static final String MOVIE_ID_KEY = "movie_id";
    public static final String MOVIE_TITLE_KEY = "movie_title";
    public static final String MOVIE_BACKDROP_KEY = "movie_backdrop";


    private Bundle mBundleState;
    private MovieDetails mSelectedMovie;
    private int mMovieId;
    private ImageView mMovieBackdropImageView;
    private ImageView mMoviePosterImageView;
    private TextView posterErrorTextView;
    private TextView mMovieGenreTextView;
    private TextView mMovieRatingTextView;
    private TextView mMovieReleaseDateTextView;
    private TextView mMoviePlotTextView;
    private TextView mMovieRuntimeTextView;

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
    private Toast mToast;


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

        // MOVIE DETAILS
        mMovieBackdropImageView = findViewById(R.id.details_backdrop_iv);
        mMovieGenreTextView = findViewById(R.id.details_genre_tv);
        mMoviePosterImageView = findViewById(R.id.details_poster_iv);
        posterErrorTextView = findViewById(R.id.poster_error_tv);
        mMovieRatingTextView = findViewById(R.id.details_rating_tv);
        mMovieReleaseDateTextView = findViewById(R.id.details_release_date_tv);
        mMoviePlotTextView = findViewById(R.id.details_plot_tv);
        mMovieRuntimeTextView = findViewById(R.id.details_runtime_tv);

        if (savedInstanceState == null) {
            // Otherwise, we check our intent and see if there is a Movie object or a movieId passed
            // from MainActivity, so we can populate our UI. If there isn't we close this activity
            // and display a toast message.
            Intent intent = getIntent();
            if (intent != null) {
                // If MainActivity passed a movie id
                if (intent.hasExtra(MOVIE_ID_KEY)) {
                    // Save the passed movieId
                    mMovieId = intent.getIntExtra(MOVIE_ID_KEY, 297762);
                    // MOVIE TITLE (set the title of our activity as the movie title)
                    setTitle(intent.getStringExtra(MOVIE_TITLE_KEY));
                    // Check if this movie is a favourite or not
                    getLoaderManager().restartLoader(CHECK_IF_FAVOURITE_MOVIE_LOADER_ID,null, favouriteMovieResultLoaderListener);
                } else {
                    closeOnError();
                }
            } else {
                closeOnError();
            }
        }

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
        mCastAdapter = new CastAdapter(this, this);
        mCastRecyclerView.setAdapter(mCastAdapter);
        mNoCastImageView = findViewById(R.id.no_cast_iv);
        mNoCastConnectionImageView = findViewById(R.id.no_cast_connection_iv);

        // Show the Cast progress bar and hide the Cast RecyclerView
        hideCast();
        // Check for saved data or fetch movie cast
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_CAST_KEY)) {
            mCast = savedInstanceState.getParcelableArrayList(MOVIE_CAST_KEY);
            // If cast is not null, data from server was previously fetched successfully
            if (mCast != null) {
                // If cast is not empty, use the saved cast and repopulate the cast section
                if (!mCast.isEmpty()) {
                    mCastAdapter = new CastAdapter(this, this);
                    mCastRecyclerView.setAdapter(mCastAdapter);
                    if (savedInstanceState.containsKey(CAST_POSITION_KEY)) {
                        mCastPosition = savedInstanceState.getInt(CAST_POSITION_KEY);
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
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_REVIEWS_KEY)) {
            mReviews = savedInstanceState.getParcelableArrayList(MOVIE_REVIEWS_KEY);
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
        mVideosAdapter = new VideoAdapter(this, this);
        mVideosRecyclerView.setAdapter(mVideosAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mVideosRecyclerView);
        mNoVideosImageView = findViewById(R.id.no_videos_iv);

        // Show the Videos progress bar and hide the Videos RecyclerView
        hideVideos();
        // Check for saved data or fetch movie videos
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_VIDEOS_KEY)) {
            mVideos = savedInstanceState.getParcelableArrayList(MOVIE_VIDEOS_KEY);
            // If mVideos is not null, data from server was previously fetched successfully
            if (mVideos != null) {
                // If mVideos is not empty, use the saved videos and repopulate the video section
                if (!mVideos.isEmpty()) {
                    mVideosAdapter = new VideoAdapter(this, this);
                    mVideosRecyclerView.setAdapter(mVideosAdapter);
                    //if (savedInstanceState.containsKey(VIDEOS_POSITION)) {
                    mVideosPosition = savedInstanceState.getInt(VIDEOS_POSITION_KEY);
                    //}
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

        MenuItem favouritesMenuItem = menu.findItem(R.id.action_favourite_movie);
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

        if (id == R.id.action_favourite_movie) {
            if (mIsFavourite) deleteFavourite(mSelectedMovie, item);
            else insertMovie(mSelectedMovie, item);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeOnError() {
        finish();
        toastThis(getString(R.string.details_error_message));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Movie Details
        outState.putParcelable(MOVIE_OBJECT_KEY, mSelectedMovie);
        // Cast
        outState.putParcelableArrayList(MOVIE_CAST_KEY, mCast);
        outState.putInt(CAST_POSITION_KEY, mCastLayoutManager.findFirstCompletelyVisibleItemPosition());
        // Reviews
        outState.putParcelableArrayList(MOVIE_REVIEWS_KEY, mReviews);
        // Videos
        outState.putParcelableArrayList(MOVIE_VIDEOS_KEY, mVideos);
        outState.putInt(VIDEOS_POSITION_KEY, mVideosLayoutManager.findFirstCompletelyVisibleItemPosition());
        // Favourite Movie
        outState.putBoolean(IS_FAVOURITE_KEY, mIsFavourite);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(MOVIE_OBJECT_KEY)) {
                mSelectedMovie = savedInstanceState.getParcelable(MOVIE_OBJECT_KEY);
                if (mSelectedMovie != null) populateMovieDetails(mSelectedMovie);
            }
            if (savedInstanceState.containsKey(MOVIE_CAST_KEY))
                mCast = savedInstanceState.getParcelableArrayList(MOVIE_CAST_KEY);
            if (savedInstanceState.containsKey(MOVIE_REVIEWS_KEY))
                mReviews = savedInstanceState.getParcelableArrayList(MOVIE_REVIEWS_KEY);
            if (savedInstanceState.containsKey(MOVIE_VIDEOS_KEY))
                mVideos = savedInstanceState.getParcelableArrayList(MOVIE_VIDEOS_KEY);
            if (savedInstanceState.containsKey(CAST_POSITION_KEY))
                mCastPosition = savedInstanceState.getInt(CAST_POSITION_KEY);
            if (savedInstanceState.containsKey(VIDEOS_POSITION_KEY))
                mVideosPosition = savedInstanceState.getInt(VIDEOS_POSITION_KEY);
            if (savedInstanceState.containsKey(IS_FAVOURITE_KEY))
                mIsFavourite = savedInstanceState.getBoolean(IS_FAVOURITE_KEY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBundleState = new Bundle();

        // Save Cast position
        mCastPosition = mCastLayoutManager.findFirstCompletelyVisibleItemPosition();
        mBundleState.putInt(CAST_POSITION_KEY, mCastPosition);

        // Save Video position
        mVideosPosition = mVideosLayoutManager.findFirstCompletelyVisibleItemPosition();
        mBundleState.putInt(VIDEOS_POSITION_KEY, mVideosPosition);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // restore RecyclerView state
        if (mBundleState != null) {
            // Restoring Cast position
            mCastPosition = mBundleState.getInt(CAST_POSITION_KEY);
            if (mCastPosition == RecyclerView.NO_POSITION) mCastPosition = 0;
            // Scroll the RecyclerView to mCastPosition
            mCastRecyclerView.smoothScrollToPosition(mCastPosition);

            // Restore Videos position
            mVideosPosition = mBundleState.getInt(VIDEOS_POSITION_KEY);
            if (mVideosPosition == RecyclerView.NO_POSITION) mVideosPosition = 0;
            // Scroll the RecyclerView to mVideosPosition
            mVideosRecyclerView.smoothScrollToPosition(mVideosPosition);
        }
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

    private LoaderManager.LoaderCallbacks<MovieDetails> movieDetailsResultLoaderListener =
            new LoaderManager.LoaderCallbacks<MovieDetails>() {
                @Override
                public Loader<MovieDetails> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case MOVIE_DETAILS_LOADER_ID:
                            // If the loaded id matches ours, return a new movie details loader
                            return new MovieDetailsLoader(getApplicationContext(), mMovieId);
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(Loader<MovieDetails> loader, MovieDetails movieDetails) {
                    mSelectedMovie = movieDetails;
                    // MOVIE TITLE (set the title of our activity as the movie title)
                    setTitle(movieDetails.getMovieTitle());
                    // Populate movie details section
                    populateMovieDetails(movieDetails);
                }

                @Override
                public void onLoaderReset(Loader<MovieDetails> loader) {
                    mSelectedMovie = null;
                    Log.v("SELECTED MOVIE", "BECAME NULL");
                }
            };

    private LoaderManager.LoaderCallbacks<ArrayList<Cast>> castResultLoaderListener =
            new LoaderManager.LoaderCallbacks<ArrayList<Cast>>() {
                @Override
                public Loader<ArrayList<Cast>> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case CAST_LOADER_ID:
                            // If the loaded id matches ours, return a new cast movie loader
                            return new CastLoader(getApplicationContext(), mMovieId);
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
                            return new ReviewLoader(getApplicationContext(), mMovieId);
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
                            return new VideoLoader(getApplicationContext(), mMovieId);
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
                    switch (loaderId) {
                        case FAVOURITE_LOADER_ID:
                            return new CursorLoader(getApplicationContext(),
                                    MovieDetailsEntry.buildMovieUriWithId(mMovieId),
                                    MOVIE_DETAILED_PROJECTION,
                                    null,
                                    null,
                                    null);
                        case CHECK_IF_FAVOURITE_MOVIE_LOADER_ID:
                            return new CursorLoader(getApplicationContext(),
                                    MovieDetailsEntry.buildMovieUriWithId(mMovieId),
                                    MOVIE_CHECK_PROJECTION,
                                    null,
                                    null,
                                    null);
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                    switch (loader.getId()) {
                        case FAVOURITE_LOADER_ID:
                            if (cursor != null && cursor.moveToFirst()) {
                                // Find the columns of movie attributes that we're interested in
                                int backdropColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_BACKDROP_PATH);
                                int genresColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_GENRES);
                                int movieIdColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_MOVIE_ID);
                                int languageColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_LANGUAGE);
                                int plotColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_PLOT);
                                int posterColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_POSTER_PATH);
                                int ratingsColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_RATINGS);
                                int releaseDateColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_RELEASE_DATE);
                                int runtimeColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_RUNTIME);
                                int titleColumnIndex = cursor.getColumnIndex(MovieDetailsEntry.COLUMN_TITLE);

                                // Set the extracted value from the Cursor for the given column index and use each
                                // value to create a Movie object
                                mSelectedMovie = new MovieDetails(
                                        cursor.getString(backdropColumnIndex),
                                        TextUtils.split(cursor.getString(genresColumnIndex), ", "),
                                        cursor.getInt(movieIdColumnIndex),
                                        cursor.getString(languageColumnIndex),
                                        cursor.getString(plotColumnIndex),
                                        cursor.getString(posterColumnIndex),
                                        cursor.getDouble(ratingsColumnIndex),
                                        cursor.getString(releaseDateColumnIndex),
                                        cursor.getInt(runtimeColumnIndex),
                                        cursor.getString(titleColumnIndex)
                                );
                                cursor.close();
                                // Populate movie details section
                                populateMovieDetails(mSelectedMovie);
                            }
                            break;

                        case CHECK_IF_FAVOURITE_MOVIE_LOADER_ID:
                            if (cursor != null && cursor.moveToFirst()) {
                                mIsFavourite = true;
                                cursor.close();
                                // If it's a favourite movie, load data using a cursor
                                getLoaderManager().restartLoader(FAVOURITE_LOADER_ID, null, favouriteMovieResultLoaderListener);
                            } else {
                                mIsFavourite = false;
                                // Otherwise, use a movie details loader and download the movie details
                                getLoaderManager().initLoader(MOVIE_DETAILS_LOADER_ID, null, movieDetailsResultLoaderListener);
                            }
                            break;
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    private void populateMovieDetails(MovieDetails movieDetails) {
        // BACKDROP
        Picasso.with(getApplicationContext())
                .load(ImageUtils.buildImageUrl(
                        getApplicationContext(),
                        movieDetails.getBackdropPath(),
                        ImageUtils.BACKDROP))
                .error(R.drawable.ic_landscape)
                .into(mMovieBackdropImageView);

        // MOVIE TITLE (set the title of our activity as the movie title)
        setTitle(movieDetails.getMovieTitle());

        // GENRE (generate and set movie genres)
        mMovieGenreTextView.setText(TextUtils.join(", ", movieDetails.getGenreIds()));
        // POSTER
        // Fetch the movie poster, if it's available. If no poster is available or if no internet
        // connection, poster error message will be used
        Picasso.with(this)
                .load(ImageUtils.buildImageUrl(
                        this,
                        movieDetails.getPosterPath(),
                        ImageUtils.POSTER))
                .placeholder(R.drawable.no_poster)
                .error(R.drawable.ic_local_movies)
                .into(mMoviePosterImageView, new Callback() {
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
                        mMoviePosterImageView.setContentDescription(getString(R.string.no_poster));
                    }
                });
        // RATINGS
        mMovieRatingTextView.setText(String.valueOf(movieDetails.getVoteAverage()).concat(getString(R.string.max_rating)));
        // RUNTIME
        mMovieRuntimeTextView.setText(String.valueOf(movieDetails.getRuntime()));
        // RELEASE DATE
        mMovieReleaseDateTextView.setText(movieDetails.getReleaseDate());
        // PLOT
        mMoviePlotTextView.setText(movieDetails.getOverview());
    }

    private void populateCast(ArrayList<Cast> movieCast) {
        if (movieCast != null) {
            mCastAdapter.swapCast(movieCast);

            // If the RecyclerView has no position, we assume the first position in the list
            if (mCastPosition == RecyclerView.NO_POSITION) mCastPosition = 0;
            // Scroll the RecyclerView to mCastPosition
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
                            reviewsIntent.putParcelableArrayListExtra(MOVIE_REVIEWS_KEY, movieReviews);
                            reviewsIntent.putExtra(MOVIE_TITLE_KEY, mSelectedMovie.getMovieTitle());
                            reviewsIntent.putExtra(MOVIE_BACKDROP_KEY, mSelectedMovie.getBackdropPath());
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

            // If the RecyclerView has no position, we assume the first position in the list
            if (mVideosPosition == RecyclerView.NO_POSITION) mVideosPosition = 0;
            // Scroll the RecyclerView to mVideoPosition
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

    private void insertMovie(MovieDetails selectedMovie, MenuItem item) {
        ContentValues values = new ContentValues();
        values.put(MovieDetailsEntry.COLUMN_MOVIE_ID, selectedMovie.getMovieId());
        values.put(MovieDetailsEntry.COLUMN_BACKDROP_PATH, selectedMovie.getBackdropPath());
        values.put(MovieDetailsEntry.COLUMN_GENRES, TextUtils.join(", ", selectedMovie.getGenreIds()));
        values.put(MovieDetailsEntry.COLUMN_LANGUAGE, selectedMovie.getLanguage());
        values.put(MovieDetailsEntry.COLUMN_PLOT, selectedMovie.getOverview());
        values.put(MovieDetailsEntry.COLUMN_POSTER_PATH, selectedMovie.getPosterPath());
        values.put(MovieDetailsEntry.COLUMN_RATINGS, selectedMovie.getVoteAverage());
        values.put(MovieDetailsEntry.COLUMN_RELEASE_DATE, selectedMovie.getReleaseDate());
        values.put(MovieDetailsEntry.COLUMN_RUNTIME, selectedMovie.getRuntime());
        values.put(MovieDetailsEntry.COLUMN_TITLE, selectedMovie.getMovieTitle());

        Uri responseUri = getContentResolver().insert(MovieDetailsEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (responseUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
            toastThis(getString(R.string.favourite_insert_failed));
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorHeart));
            toastThis(getString(R.string.favourite_insert_successful));
            mIsFavourite = true;
        }
    }

    private void deleteFavourite(MovieDetails mSelectedMovie, MenuItem item) {
        int rowsDeleted = getContentResolver().delete(MovieDetailsEntry.CONTENT_URI,
                MovieDetailsEntry.COLUMN_MOVIE_ID + " =?",
                new String[]{String.valueOf(mSelectedMovie.getMovieId())});

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were affected, then there was an error with the delete.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorHeart));
            toastThis(getString(R.string.favourite_delete_failed));
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
            toastThis(getString(R.string.favourite_delete_successful));
            mIsFavourite = false;
        }
    }

    public void toastThis(String toastMessage) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    public void onListItemClick(Video videoClicked) {
        startActivity(new Intent(
                Intent.ACTION_VIEW,
                NetworkUtils.buildVideoUri(videoClicked.getVideoKey())));
    }

    @Override
    public void onListItemClick(Cast castClicked) {
        Intent actorProfileIntent = new Intent(DetailsActivity.this, ProfileActivity.class);
        actorProfileIntent.putExtra(ACTOR_ID, castClicked.getActorId());
        startActivity(actorProfileIntent);
    }
}