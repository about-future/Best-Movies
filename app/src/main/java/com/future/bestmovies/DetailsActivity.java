package com.future.bestmovies;

import android.app.LoaderManager;
import android.content.ContentValues;
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
import com.future.bestmovies.data.FavouritesContract;
import com.future.bestmovies.data.MovieDetails;
import com.future.bestmovies.data.MovieDetailsLoader;
import com.future.bestmovies.data.Review;
import com.future.bestmovies.data.ReviewLoader;
import com.future.bestmovies.data.Video;
import com.future.bestmovies.data.VideoAdapter;
import com.future.bestmovies.data.VideoLoader;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.future.bestmovies.data.FavouritesContract.*;


public class DetailsActivity extends AppCompatActivity implements
        VideoAdapter.ListItemClickListener, CastAdapter.ListItemClickListener {
    private static final int MOVIE_DETAILS_LOADER_ID = 805;
    private static final int CAST_LOADER_ID = 423;
    private static final int REVIEWS_LOADER_ID = 435;
    private static final int VIDEOS_LOADER_ID = 594;
    private static final int FAVOURITE_LOADER_ID = 516;
    private static final int FAVOURITE_CAST_LOADER_ID = 516423;
    private static final int FAVOURITE_REVIEW_LOADER_ID = 516435;
    private static final int FAVOURITE_VIDEOS_LOADER_ID = 516594;
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

    // Query projection used to retrieve movie cast
    public static final String[] CAST_DETAILED_PROJECTION = {
            CastEntry.COLUMN_ACTOR_ID,
            CastEntry.COLUMN_ACTOR_NAME,
            CastEntry.COLUMN_CHARACTER_NAME,
            CastEntry.COLUMN_IMAGE_PROFILE_PATH,
            CastEntry.COLUMN_MOVIE_ID
    };

    // Query projection used to retrieve movie reviews
    public static final String[] REVIEW_DETAILED_PROJECTION = {
            ReviewsEntry.COLUMN_AUTHOR,
            ReviewsEntry.COLUMN_CONTENT,
            ReviewsEntry.COLUMN_MOVIE_ID
    };

    // Query projection used to retrieve movie videos
    public static final String[] VIDEOS_DETAILED_PROJECTION = {
            VideosEntry.COLUMN_MOVIE_ID,
            VideosEntry.COLUMN_VIDEO_KEY,
            VideosEntry.COLUMN_VIDEO_NAME,
            VideosEntry.COLUMN_VIDEO_TYPE
    };

    // Instance Keys
    public static final String MOVIE_OBJECT_KEY = "movie";
    private static final String MOVIE_CAST_KEY = "movie_cast";
    public static final String MOVIE_REVIEWS_KEY = "movie_reviews";
    public static final String MOVIE_VIDEOS_KEY = "movie_videos";

    private static final String IS_FAVOURITE_KEY = "is_favourite";
    public static final String ACTOR_ID_KEY = "actor_id";
    public static final String ACTOR_NAME_KEY = "actor_name";
    private static final String CAST_POSITION_KEY = "cast_position";
    private static final String VIDEOS_POSITION_KEY = "videos_position";

    public static final String MOVIE_ID_KEY = "movie_id";
    public static final String MOVIE_TITLE_KEY = "movie_title";
    public static final String MOVIE_BACKDROP_KEY = "movie_backdrop";

    private Bundle mBundleState;
    private int mMovieId;
    private MovieDetails mSelectedMovie;

    // Movie details variables
    private ImageView mMovieBackdropImageView;
    private ImageView mMoviePosterImageView;
    private TextView posterErrorTextView;
    private TextView mMovieGenreTextView;
    private TextView mMovieRatingTextView;
    private TextView mMovieReleaseDateTextView;
    private TextView mMoviePlotTextView;
    private TextView mMovieRuntimeTextView;

    // Cast variables
    private ArrayList<Cast> mCast;
    private RecyclerView mCastRecyclerView;
    private int mCastPosition = RecyclerView.NO_POSITION;
    private LinearLayoutManager mCastLayoutManager;
    private CastAdapter mCastAdapter;
    private ProgressBar mCastProgressBar;
    private ImageView mNoCastImageView;
    private TextView mCastMessagesTextView;

    // Reviews variables
    private ArrayList<Review> mReviews;
    private ConstraintLayout mFirstReviewLayout;
    private TextView mFirstReviewAuthorTextView;
    private TextView mFirstReviewContentTextView;
    private ProgressBar mFirstReviewProgressBar;
    private ImageView mNoReviewsImageView;
    private TextView mFirstReviewMessagesTextView;
    private TextView mSeeAllReviewsTextView;

    // Videos variables
    private ArrayList<Video> mVideos;
    private RecyclerView mVideosRecyclerView;
    private int mVideosPosition = RecyclerView.NO_POSITION;
    private LinearLayoutManager mVideosLayoutManager;
    private VideoAdapter mVideosAdapter;
    private ProgressBar mVideosProgressBar;
    private ImageView mNoVideosImageView;
    private TextView mVideosMessagesTextView;

    private boolean mIsFavourite;
    private MenuItem mFavouriteMovieMenuItem;
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
        mCastRecyclerView.setHasFixedSize(false);
        mCastAdapter = new CastAdapter(this, this);
        mCastRecyclerView.setAdapter(mCastAdapter);
        mNoCastImageView = findViewById(R.id.no_cast_iv);

        // REVIEWS
        mFirstReviewLayout = findViewById(R.id.first_review_layout);
        mFirstReviewAuthorTextView = findViewById(R.id.first_review_author_tv);
        mFirstReviewContentTextView = findViewById(R.id.first_review_content_tv);
        mFirstReviewProgressBar = findViewById(R.id.loading_first_review_pb);
        mFirstReviewMessagesTextView = findViewById(R.id.first_review_messages_tv);
        mFirstReviewMessagesTextView.setText(R.string.loading);
        mSeeAllReviewsTextView = findViewById(R.id.see_all_reviews_tv);
        mNoReviewsImageView = findViewById(R.id.no_reviews_iv);

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
        // Scrolling one item at the time is done with a SnapHelper
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mVideosRecyclerView);
        mNoVideosImageView = findViewById(R.id.no_videos_iv);

        if (savedInstanceState == null) {
            // Check intent and see if there is a movieId passed from MainActivity or
            // ProfileActivity, so we can populate our UI. If there isn't we close this activity
            // and display a toast message.
            Intent intent = getIntent();
            if (intent != null) {
                // If MainActivity or ProfileActivity passed a movie id
                if (intent.hasExtra(MOVIE_ID_KEY)) {
                    // Save the passed movieId
                    mMovieId = intent.getIntExtra(MOVIE_ID_KEY, 297762);
                    // Set the title of our activity as the movie title, passed from the other activity
                    setTitle(intent.getStringExtra(MOVIE_TITLE_KEY));
                    // Check if this movie is a favourite or not
                    getLoaderManager().restartLoader(CHECK_IF_FAVOURITE_MOVIE_LOADER_ID, null, favouriteMovieResultLoaderListener);
                } else {
                    closeOnError(getString(R.string.details_error_message));
                }
            } else {
                closeOnError(getString(R.string.details_error_message));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);

        MenuItem favouriteMovieMenuItem = menu.findItem(R.id.action_favourite_movie);
        mFavouriteMovieMenuItem = favouriteMovieMenuItem;
        if (mIsFavourite) {
            DrawableCompat.setTint(favouriteMovieMenuItem.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorHeart));
        } else {
            DrawableCompat.setTint(favouriteMovieMenuItem.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
        }

        return true;
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        return super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // This case is deactivate on purpose. The reason is when the user chooses a movie, then an
        // actor profile, and an other movie in which the chosen actor appeared in and an other actor
        // from the same movie, and an other movie and so on, (the user) will no be forced to click
        // back 20 times, just to get to the main movie list. Thank you for your understanding!
        /*if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }*/

        if (id == R.id.action_favourite_movie) {
            if (mIsFavourite)
                deleteFavourite(mSelectedMovie, item);
            else {
                if (NetworkUtils.isConnected(getApplicationContext()))
                    insertMovie(mSelectedMovie, item);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeOnError(String message) {
        finish();
        toastThis(message);
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
            // Movie Details
            if (savedInstanceState.containsKey(MOVIE_OBJECT_KEY)) {
                mSelectedMovie = savedInstanceState.getParcelable(MOVIE_OBJECT_KEY);
                if (mSelectedMovie != null) populateMovieDetails(mSelectedMovie);
            }

            // Cast
            if (savedInstanceState.containsKey(MOVIE_CAST_KEY)) {
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
                    noCast();
                }
            }

            // Reviews
            if (savedInstanceState.containsKey(MOVIE_REVIEWS_KEY)) {
                mReviews = savedInstanceState.getParcelableArrayList(MOVIE_REVIEWS_KEY);
                // If mReview is not null, data from server was previously fetched successfully
                if (mReviews != null) {
                    // If review is not empty, use the saved reviews and repopulate the reviews section
                    populateReviews(mReviews);
                } else {
                    noReviews();
                }
            }

            // Videos
            if (savedInstanceState.containsKey(MOVIE_VIDEOS_KEY)) {
                mVideos = savedInstanceState.getParcelableArrayList(MOVIE_VIDEOS_KEY);
                // If mVideos is not null, data from server was previously fetched successfully
                if (mVideos != null) {
                    // If mVideos is not empty, use the saved videos and repopulate the video section
                    if (!mVideos.isEmpty()) {
                        mVideosAdapter = new VideoAdapter(this, this);
                        mVideosRecyclerView.setAdapter(mVideosAdapter);
                        if (savedInstanceState.containsKey(VIDEOS_POSITION_KEY)) {
                            mVideosPosition = savedInstanceState.getInt(VIDEOS_POSITION_KEY);
                        }
                    }
                    populateVideos(mVideos);
                } else {
                    noVideos();
                }
            }

            // Favourite Movie
            if (savedInstanceState.containsKey(IS_FAVOURITE_KEY)) {
                mIsFavourite = savedInstanceState.getBoolean(IS_FAVOURITE_KEY);
            }
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

        mBundleState.putBoolean(IS_FAVOURITE_KEY, mIsFavourite);
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

            mIsFavourite = mBundleState.getBoolean(IS_FAVOURITE_KEY);
        }
    }

    // Hide the progress bar and show cast
    private void showCast() {
        mCastRecyclerView.setVisibility(View.VISIBLE);
        mCastProgressBar.setVisibility(View.INVISIBLE);
        mCastMessagesTextView.setVisibility(View.INVISIBLE);
        mNoCastImageView.setVisibility(View.INVISIBLE);
    }

    // Show progress bar and hide cast
    private void hideCast() {
        //mCastRecyclerView.setVisibility(View.GONE);
        mCastProgressBar.setVisibility(View.VISIBLE);
        mCastMessagesTextView.setVisibility(View.VISIBLE);
        mNoCastImageView.setVisibility(View.INVISIBLE);
    }

    // Hide progress bar and show no reviews and message
    private void noCast() {
        //mCastRecyclerView.setVisibility(View.GONE);
        mCastMessagesTextView.setVisibility(View.VISIBLE);
        mCastMessagesTextView.setText(R.string.no_cast);
        mCastProgressBar.setVisibility(View.INVISIBLE);
        mNoCastImageView.setVisibility(View.VISIBLE);
    }

    // Hide the progress bar and show reviews
    private void showReviews() {
        mFirstReviewLayout.setVisibility(View.VISIBLE);
        mFirstReviewProgressBar.setVisibility(View.INVISIBLE);
        mFirstReviewMessagesTextView.setVisibility(View.INVISIBLE);
        mNoReviewsImageView.setVisibility(View.INVISIBLE);
    }

    // Show progress bar and hide reviews
    private void hideReviews() {
        mFirstReviewLayout.setVisibility(View.INVISIBLE);
        mFirstReviewProgressBar.setVisibility(View.VISIBLE);
        mFirstReviewMessagesTextView.setVisibility(View.VISIBLE);
        mSeeAllReviewsTextView.setVisibility(View.INVISIBLE);
        mNoReviewsImageView.setVisibility(View.INVISIBLE);
    }

    // Hide progress bar and show no reviews icon and message
    private void noReviews() {
        mFirstReviewLayout.setVisibility(View.INVISIBLE);
        mSeeAllReviewsTextView.setVisibility(View.INVISIBLE);
        mFirstReviewMessagesTextView.setVisibility(View.VISIBLE);
        mFirstReviewMessagesTextView.setText(R.string.no_reviews);
        mFirstReviewProgressBar.setVisibility(View.INVISIBLE);
        mNoReviewsImageView.setVisibility(View.VISIBLE);
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

    // Hide progress bar and show no videos icon and message
    private void noVideos() {
        mVideosRecyclerView.setVisibility(View.GONE);
        mVideosMessagesTextView.setVisibility(View.VISIBLE);
        mVideosMessagesTextView.setText(R.string.no_videos);
        mVideosProgressBar.setVisibility(View.INVISIBLE);
        mNoVideosImageView.setVisibility(View.VISIBLE);
    }

    private void fetchMovieDetails() {
        if (NetworkUtils.isConnected(getApplicationContext())) {
            getLoaderManager().initLoader(MOVIE_DETAILS_LOADER_ID, null, movieDetailsResultLoaderListener);
        } else {
            toastThis(getString(R.string.no_connection));
        }
    }

    private void fetchCast() {
        if (NetworkUtils.isConnected(getApplicationContext())) {
            // Show the Cast progress bar and hide the Cast RecyclerView
            hideCast();
            getLoaderManager().initLoader(CAST_LOADER_ID, null, castResultLoaderListener);
        }
    }

    private void fetchReviews() {
        if (NetworkUtils.isConnected(getApplicationContext())) {
            // Show the Review progress bar and hide the firstReview layout
            hideReviews();
            getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, reviewsResultLoaderListener);
        }
    }

    private void fetchVideos() {
        if (NetworkUtils.isConnected(getApplicationContext())) {
            // Show the Videos progress bar and hide the Videos RecyclerView
            hideVideos();
            getLoaderManager().initLoader(VIDEOS_LOADER_ID, null, videoResultLoaderListener);
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
                    //mCastAdapter.swapCast(new ArrayList<Cast>() {});
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
                    //mFirstReviewAuthorTextView.setText("");
                    //mFirstReviewContentTextView.setText("");
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
                    //mVideosAdapter.swapVideos(new ArrayList<Video>() {});
                }
            };

    private LoaderManager.LoaderCallbacks<Cursor> favouriteMovieResultLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case FAVOURITE_LOADER_ID:
                            return new CursorLoader(getApplicationContext(),
                                    FavouritesContract.buildUriWithId(MovieDetailsEntry.CONTENT_URI, mMovieId),
                                    MOVIE_DETAILED_PROJECTION,
                                    null,
                                    null,
                                    null);
                        case CHECK_IF_FAVOURITE_MOVIE_LOADER_ID:
                            return new CursorLoader(getApplicationContext(),
                                    FavouritesContract.buildUriWithId(MovieDetailsEntry.CONTENT_URI, mMovieId),
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

                                // As soon as we know the movie is a favourite, color the heart, so the user will know it too
                                if (mFavouriteMovieMenuItem != null)
                                    DrawableCompat.setTint(mFavouriteMovieMenuItem.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorHeart));
                                cursor.close();
                                // If it's a favourite movie, load data using a cursor for each section
                                getLoaderManager().initLoader(FAVOURITE_LOADER_ID, null, favouriteMovieResultLoaderListener);
                                getLoaderManager().initLoader(FAVOURITE_CAST_LOADER_ID, null, favouriteCastResultLoaderListener);
                                getLoaderManager().initLoader(FAVOURITE_REVIEW_LOADER_ID, null, favouriteReviewsResultLoaderListener);
                                getLoaderManager().initLoader(FAVOURITE_VIDEOS_LOADER_ID, null, favouriteVideosResultLoaderListener);
                            } else {
                                mIsFavourite = false;
                                if (NetworkUtils.isConnected(getApplicationContext())) {
                                    // Otherwise, use a movie details loader and download the movie details
                                    getLoaderManager().initLoader(MOVIE_DETAILS_LOADER_ID, null, movieDetailsResultLoaderListener);
                                    //hideCast();
                                    getLoaderManager().initLoader(CAST_LOADER_ID, null, castResultLoaderListener);
                                    //hideReviews();
                                    getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, reviewsResultLoaderListener);
                                    //hideVideos();
                                    getLoaderManager().initLoader(VIDEOS_LOADER_ID, null, videoResultLoaderListener);
                                    //fetchMovieDetails();
                                    //fetchCast();
                                    //fetchReviews();
                                    //fetchVideos();
                                } else {
                                    closeOnError(getString(R.string.no_connection));
                                }
                            }
                            break;
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    private LoaderManager.LoaderCallbacks<Cursor> favouriteCastResultLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case FAVOURITE_CAST_LOADER_ID:
                            return new CursorLoader(getApplicationContext(),
                                    FavouritesContract.buildUriWithId(CastEntry.CONTENT_URI, mMovieId),
                                    CAST_DETAILED_PROJECTION,
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
                        // Find the columns of movie cast attributes that we're interested in
                        int movieIdColumnIndex = cursor.getColumnIndex(CastEntry.COLUMN_MOVIE_ID);
                        int castNameColumnIndex = cursor.getColumnIndex(CastEntry.COLUMN_ACTOR_NAME);
                        int castCharacterColumnIndex = cursor.getColumnIndex(CastEntry.COLUMN_CHARACTER_NAME);
                        int castIdColumnIndex = cursor.getColumnIndex(CastEntry.COLUMN_ACTOR_ID);
                        int castImagePathColumnIndex = cursor.getColumnIndex(CastEntry.COLUMN_IMAGE_PROFILE_PATH);

                        mCast = new ArrayList<>();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            cursor.moveToPosition(i);
                            // Set the extracted value from the Cursor for the given column index and use each
                            // value to create a Cast object
                            mCast.add(new Cast(
                                    cursor.getInt(movieIdColumnIndex),
                                    cursor.getString(castCharacterColumnIndex),
                                    cursor.getInt(castIdColumnIndex),
                                    cursor.getString(castNameColumnIndex),
                                    cursor.getString(castImagePathColumnIndex)));
                        }

                        cursor.close();
                        // Populate movie cast section
                        populateCast(mCast);
                    } else {
                        noCast();
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    private LoaderManager.LoaderCallbacks<Cursor> favouriteReviewsResultLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case FAVOURITE_REVIEW_LOADER_ID:
                            return new CursorLoader(getApplicationContext(),
                                    FavouritesContract.buildUriWithId(ReviewsEntry.CONTENT_URI, mMovieId),
                                    REVIEW_DETAILED_PROJECTION,
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
                        // Find the columns of movie review attributes that we're interested in
                        int movieIdColumnIndex = cursor.getColumnIndex(ReviewsEntry.COLUMN_MOVIE_ID);
                        int authorColumnIndex = cursor.getColumnIndex(ReviewsEntry.COLUMN_AUTHOR);
                        int contentColumnIndex = cursor.getColumnIndex(ReviewsEntry.COLUMN_CONTENT);

                        mReviews = new ArrayList<>();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            cursor.moveToPosition(i);
                            // Set the extracted value from the Cursor for the given column index and use each
                            // value to create a Review object
                            mReviews.add(new Review(
                                    cursor.getInt(movieIdColumnIndex),
                                    cursor.getString(authorColumnIndex),
                                    cursor.getString(contentColumnIndex)));
                        }

                        cursor.close();
                        // Populate movie reviews section
                        populateReviews(mReviews);
                    } else {
                        noReviews();
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    private LoaderManager.LoaderCallbacks<Cursor> favouriteVideosResultLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case FAVOURITE_VIDEOS_LOADER_ID:
                            return new CursorLoader(getApplicationContext(),
                                    FavouritesContract.buildUriWithId(VideosEntry.CONTENT_URI, mMovieId),
                                    VIDEOS_DETAILED_PROJECTION,
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
                        // Find the columns of movie videos attributes that we're interested in
                        int movieIdColumnIndex = cursor.getColumnIndex(VideosEntry.COLUMN_MOVIE_ID);
                        int videoKeyColumnIndex = cursor.getColumnIndex(VideosEntry.COLUMN_VIDEO_KEY);
                        int videoNameColumnIndex = cursor.getColumnIndex(VideosEntry.COLUMN_VIDEO_NAME);
                        int videoTypeColumnIndex = cursor.getColumnIndex(VideosEntry.COLUMN_VIDEO_TYPE);

                        mVideos = new ArrayList<>();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            cursor.moveToPosition(i);
                            // Set the extracted value from the Cursor for the given column index and use each
                            // value to create a Video object
                            mVideos.add(new Video(
                                    cursor.getInt(movieIdColumnIndex),
                                    cursor.getString(videoKeyColumnIndex),
                                    cursor.getString(videoNameColumnIndex),
                                    cursor.getString(videoTypeColumnIndex)));
                        }

                        cursor.close();
                        // Populate movie videos section
                        populateVideos(mVideos);
                    } else {
                        noVideos();
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
        int runtime = movieDetails.getRuntime();
        long hours = TimeUnit.MINUTES.toHours(runtime);
        long minutes = runtime - TimeUnit.HOURS.toMinutes(hours);

        if (hours > 0) {
            mMovieRuntimeTextView.setText(String.format(getString(R.string.format_runtime), (float) hours, (float) minutes));
        } else {
            mMovieRuntimeTextView.setText(String.format(getString(R.string.format_minutes), (float) minutes));
        }

        // RELEASE DATE
        mMovieReleaseDateTextView.setText(movieDetails.getReleaseDate());
        // PLOT
        mMoviePlotTextView.setText(movieDetails.getOverview());
    }

    private void populateCast(ArrayList<Cast> movieCast) {
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
            noCast();
        }
    }

    private void populateReviews(final ArrayList<Review> movieReviews) {
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
            noReviews();
        }
    }

    private void populateVideos(ArrayList<Video> movieVideos) {
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
            noVideos();
        }
    }

    private void insertMovie(MovieDetails selectedMovie, MenuItem item) {
        int INITIAL_VALUE = -1;

        //Movie details insertion
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieDetailsEntry.COLUMN_MOVIE_ID, selectedMovie.getMovieId());
        movieValues.put(MovieDetailsEntry.COLUMN_BACKDROP_PATH, selectedMovie.getBackdropPath());
        movieValues.put(MovieDetailsEntry.COLUMN_GENRES, TextUtils.join(", ", selectedMovie.getGenreIds()));
        movieValues.put(MovieDetailsEntry.COLUMN_LANGUAGE, selectedMovie.getLanguage());
        movieValues.put(MovieDetailsEntry.COLUMN_PLOT, selectedMovie.getOverview());
        movieValues.put(MovieDetailsEntry.COLUMN_POSTER_PATH, selectedMovie.getPosterPath());
        movieValues.put(MovieDetailsEntry.COLUMN_RATINGS, selectedMovie.getVoteAverage());
        movieValues.put(MovieDetailsEntry.COLUMN_RELEASE_DATE, selectedMovie.getReleaseDate());
        movieValues.put(MovieDetailsEntry.COLUMN_RUNTIME, selectedMovie.getRuntime());
        movieValues.put(MovieDetailsEntry.COLUMN_TITLE, selectedMovie.getMovieTitle());
        Uri movieResponseUri = getContentResolver().insert(MovieDetailsEntry.CONTENT_URI, movieValues);

        // Cast insertion
        ContentValues[] allCastValues = new ContentValues[mCast.size()];
        // For each cast member, get the data and put it in castValue
        for (int i = 0; i < mCast.size(); i++) {
            ContentValues castValues = new ContentValues();
            castValues.put(CastEntry.COLUMN_MOVIE_ID, selectedMovie.getMovieId());
            castValues.put(CastEntry.COLUMN_ACTOR_ID, mCast.get(i).getActorId());
            castValues.put(CastEntry.COLUMN_ACTOR_NAME, mCast.get(i).getActorName());
            castValues.put(CastEntry.COLUMN_CHARACTER_NAME, mCast.get(i).getCharacter());
            castValues.put(CastEntry.COLUMN_IMAGE_PROFILE_PATH, mCast.get(i).getProfilePath());

            // Add each castValues to the array
            allCastValues[i] = castValues;
        }

        // Initialize the value of castResponse
        int castResponse = INITIAL_VALUE;
        // If we have cast values to insert, insert them and update the value of castResponse
        if (allCastValues.length != 0) {
            castResponse = getContentResolver().bulkInsert(CastEntry.CONTENT_URI, allCastValues);
        }

        // Reviews insertion
        ContentValues[] allReviewsValues = new ContentValues[mReviews.size()];
        // For each review, get it's data and put it in reviewValues
        for (int i = 0; i < mReviews.size(); i++) {
            ContentValues reviewValues = new ContentValues();
            reviewValues.put(ReviewsEntry.COLUMN_MOVIE_ID, selectedMovie.getMovieId());
            reviewValues.put(ReviewsEntry.COLUMN_AUTHOR, mReviews.get(i).getReviewAuthor());
            reviewValues.put(ReviewsEntry.COLUMN_CONTENT, mReviews.get(i).getReviewContent());

            // Add each reviewValues to the array
            allReviewsValues[i] = reviewValues;
        }

        // Initialize the value of reviewsResponse
        int reviewsResponse = INITIAL_VALUE;
        // If we have review values to insert, insert them and update the value of reviewsResponse
        if (allReviewsValues.length != 0) {
            reviewsResponse = getContentResolver().bulkInsert(ReviewsEntry.CONTENT_URI, allReviewsValues);
        }

        // Videos insertion
        ContentValues[] allVideoValues = new ContentValues[mVideos.size()];
        // For each video, get it's data and put it in videoValues
        for (int i = 0; i < mVideos.size(); i++) {
            ContentValues videoValue = new ContentValues();
            videoValue.put(VideosEntry.COLUMN_MOVIE_ID, selectedMovie.getMovieId());
            videoValue.put(VideosEntry.COLUMN_VIDEO_KEY, mVideos.get(i).getVideoKey());
            videoValue.put(VideosEntry.COLUMN_VIDEO_NAME, mVideos.get(i).getVideoName());
            videoValue.put(VideosEntry.COLUMN_VIDEO_TYPE, mVideos.get(i).getVideoType());

            // Add each videoValues to the array
            allVideoValues[i] = videoValue;
        }

        // Initialize the value of videosResponse
        int videosResponse = INITIAL_VALUE;
        // If we have video values to insert, insert them and update the value of videosResponse
        if (allVideoValues.length != 0) {
            videosResponse = getContentResolver().bulkInsert(VideosEntry.CONTENT_URI, allVideoValues);
        }

        // Show a toast message depending on whether or not the insertion was successful
        if (movieResponseUri != null &&
                (castResponse == INITIAL_VALUE || castResponse > 0) &&
                (reviewsResponse == INITIAL_VALUE || reviewsResponse > 0) &&
                (videosResponse == INITIAL_VALUE || videosResponse > 0)) {
            // The insertion was successful and we can display a toast.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorHeart));
            toastThis(getString(R.string.favourite_insert_successful));
            mIsFavourite = true;
        } else {
            // Otherwise, if the new content URI is null, then there was an error with insertion.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
            toastThis(getString(R.string.favourite_insert_failed));
        }
    }

    private void deleteFavourite(MovieDetails mSelectedMovie, MenuItem item) {
        int rowsDeleted = getContentResolver().delete(MovieDetailsEntry.CONTENT_URI,
                MovieDetailsEntry.COLUMN_MOVIE_ID + " =?",
                new String[]{String.valueOf(mSelectedMovie.getMovieId())});

        getContentResolver().delete(CastEntry.CONTENT_URI,
                CastEntry.COLUMN_MOVIE_ID + " =?",
                new String[]{String.valueOf(mSelectedMovie.getMovieId())});

        getContentResolver().delete(ReviewsEntry.CONTENT_URI,
                ReviewsEntry.COLUMN_MOVIE_ID + " =?",
                new String[]{String.valueOf(mSelectedMovie.getMovieId())});

        getContentResolver().delete(VideosEntry.CONTENT_URI,
                VideosEntry.COLUMN_MOVIE_ID + " =?",
                new String[]{String.valueOf(mSelectedMovie.getMovieId())});

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted != 0) {
            // Otherwise, the delete was successful and we can display a toast.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
            toastThis(getString(R.string.favourite_delete_successful));
            mIsFavourite = false;
        } else {
            // Otherwise, if no rows were affected, then there was an error with the delete.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorHeart));
            toastThis(getString(R.string.favourite_delete_failed));
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
        actorProfileIntent.putExtra(ACTOR_ID_KEY, castClicked.getActorId());
        actorProfileIntent.putExtra(ACTOR_NAME_KEY, castClicked.getActorName());
        actorProfileIntent.putExtra(MOVIE_BACKDROP_KEY, mSelectedMovie.getBackdropPath());
        startActivity(actorProfileIntent);
    }
}