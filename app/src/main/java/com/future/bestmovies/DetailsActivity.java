package com.future.bestmovies;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ShareCompat;
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

import com.future.bestmovies.cast.Cast;
import com.future.bestmovies.cast.CastAdapter;
import com.future.bestmovies.cast.CastLoader;
import com.future.bestmovies.data.FavouritesContract;
import com.future.bestmovies.movie_details.Details;
import com.future.bestmovies.movie_details.DetailsLoader;
import com.future.bestmovies.movie_details.Genre;
import com.future.bestmovies.reviews.Review;
import com.future.bestmovies.reviews.ReviewLoader;
import com.future.bestmovies.videos.Video;
import com.future.bestmovies.videos.VideoAdapter;
import com.future.bestmovies.videos.VideoLoader;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

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

    private static final String MOVIES_SHARE_HASHTAG = " #BestMoviesApp";

    // Query projection used to check if the movie is a favourite or not
    private static final String[] MOVIE_CHECK_PROJECTION = {MovieDetailsEntry.COLUMN_MOVIE_ID};

    // Query projection used to retrieve movie details
    private static final String[] MOVIE_DETAILED_PROJECTION = {
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
    private static final String[] CAST_DETAILED_PROJECTION = {
            CastEntry.COLUMN_ACTOR_ID,
            CastEntry.COLUMN_ACTOR_NAME,
            CastEntry.COLUMN_CHARACTER_NAME,
            CastEntry.COLUMN_IMAGE_PROFILE_PATH,
            CastEntry.COLUMN_MOVIE_ID
    };

    // Query projection used to retrieve movie reviews
    private static final String[] REVIEW_DETAILED_PROJECTION = {
            ReviewsEntry.COLUMN_AUTHOR,
            ReviewsEntry.COLUMN_CONTENT,
            ReviewsEntry.COLUMN_MOVIE_ID
    };

    // Query projection used to retrieve movie videos
    private static final String[] VIDEOS_DETAILED_PROJECTION = {
            VideosEntry.COLUMN_MOVIE_ID,
            VideosEntry.COLUMN_VIDEO_KEY,
            VideosEntry.COLUMN_VIDEO_NAME,
            VideosEntry.COLUMN_VIDEO_TYPE
    };

    // Instance Keys
    private static final String MOVIE_OBJECT_KEY = "movie";
    private static final String MOVIE_CAST_KEY = "movie_cast";
    public static final String MOVIE_REVIEWS_KEY = "movie_reviews";
    private static final String MOVIE_VIDEOS_KEY = "movie_videos";

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
    private Details mSelectedMovie;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    // Movie details variables
    @BindView(R.id.details_backdrop_iv)
    ImageView mMovieBackdropImageView;
    @BindView(R.id.details_poster_iv)
    ImageView mMoviePosterImageView;
    @BindView(R.id.poster_error_tv)
    TextView posterErrorTextView;
    @BindView(R.id.details_genre_tv)
    TextView mMovieGenreTextView;
    @BindView(R.id.details_rating_tv)
    TextView mMovieRatingTextView;
    @BindView(R.id.details_release_date_tv)
    TextView mMovieReleaseDateTextView;
    @BindView(R.id.details_plot_tv)
    TextView mMoviePlotTextView;
    @BindView(R.id.details_runtime_tv)
    TextView mMovieRuntimeTextView;

    // Cast variables
    @BindView(R.id.cast_rv)
    RecyclerView mCastRecyclerView;
    @BindView(R.id.loading_cast_pb)
    ProgressBar mCastProgressBar;
    @BindView(R.id.no_cast_iv)
    ImageView mNoCastImageView;
    @BindView(R.id.cast_messages_tv)
    TextView mCastMessagesTextView;
    private ArrayList<Cast> mCast;
    private int mCastPosition = RecyclerView.NO_POSITION;
    private LinearLayoutManager mCastLayoutManager;
    private CastAdapter mCastAdapter;

    // Reviews variables
    @BindView(R.id.first_review_layout)
    ConstraintLayout mFirstReviewLayout;
    @BindView(R.id.first_review_author_tv)
    TextView mFirstReviewAuthorTextView;
    @BindView(R.id.first_review_content_tv)
    TextView mFirstReviewContentTextView;
    @BindView(R.id.loading_first_review_pb)
    ProgressBar mFirstReviewProgressBar;
    @BindView(R.id.no_reviews_iv)
    ImageView mNoReviewsImageView;
    @BindView(R.id.first_review_messages_tv)
    TextView mFirstReviewMessagesTextView;
    @BindView(R.id.see_all_reviews_tv)
    TextView mSeeAllReviewsTextView;
    private ArrayList<Review> mReviews;

    // Videos variables
    @BindView(R.id.videos_rv)
    RecyclerView mVideosRecyclerView;
    @BindView(R.id.loading_videos_pb)
    ProgressBar mVideosProgressBar;
    @BindView(R.id.no_videos_iv)
    ImageView mNoVideosImageView;
    @BindView(R.id.videos_messages_tv)
    TextView mVideosMessagesTextView;
    @BindView(R.id.share_video_iv)
    ImageView mVideoShare;
    private ArrayList<Video> mVideos;
    private int mVideosPosition = RecyclerView.NO_POSITION;
    private LinearLayoutManager mVideosLayoutManager;
    private VideoAdapter mVideosAdapter;

    private boolean mIsFavourite;
    private MenuItem mFavouriteMovieMenuItem;
    private Toast mToast;

    // Resources
    @BindString(R.string.no_connection)
    String noConnection;
    @BindString(R.string.no_poster)
    String noPoster;
    @BindString(R.string.loading)
    String loadingMsg;
    @BindString(R.string.details_error_message)
    String errorMsg;
    @BindString(R.string.favourite_movie_insert_successful)
    String insertSuccessfulMsg;
    @BindString(R.string.favourite_movie_insert_failed)
    String insertFailedMsg;
    @BindString(R.string.favourite_movie_delete_successful)
    String deleteSuccessfulMsg;
    @BindString(R.string.favourite_movie_delete_failed)
    String deleteFailedMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        // We initialize and set the toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // CAST
        mCastMessagesTextView.setText(loadingMsg);
        // The layout manager for our Cast RecyclerView will be a LinerLayout, so we can display
        // our cast on a single line, horizontally
        mCastLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mCastRecyclerView.setLayoutManager(mCastLayoutManager);
        mCastRecyclerView.setHasFixedSize(false);
        mCastAdapter = new CastAdapter(this, this);
        mCastRecyclerView.setAdapter(mCastAdapter);

        // REVIEWS
        mFirstReviewMessagesTextView.setText(loadingMsg);

        // VIDEOS
        mVideosMessagesTextView.setText(loadingMsg);
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
                    closeOnError(errorMsg);
                }
            } else {
                closeOnError(errorMsg);
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

    // Hide progress bar and show no reviews and message
    private void noCast() {
        mCastRecyclerView.setVisibility(View.GONE);
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
        mVideoShare.setVisibility(View.VISIBLE);
        mVideoShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(createShareVideoIntent());
            }
        });
    }

    // Hide progress bar and show no videos icon and message
    private void noVideos() {
        mVideosRecyclerView.setVisibility(View.GONE);
        mVideosMessagesTextView.setVisibility(View.VISIBLE);
        mVideosMessagesTextView.setText(R.string.no_videos);
        mVideosProgressBar.setVisibility(View.INVISIBLE);
        mNoVideosImageView.setVisibility(View.VISIBLE);
        mVideoShare.setVisibility(View.INVISIBLE);
    }

    private final LoaderManager.LoaderCallbacks<Details> movieDetailsResultLoaderListener =
            new LoaderManager.LoaderCallbacks<Details>() {
                @Override
                public Loader<Details> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case MOVIE_DETAILS_LOADER_ID:
                            // If the loaded id matches ours, return a new movie details loader
                            return new DetailsLoader(getApplicationContext(), mMovieId);
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(Loader<Details> loader, Details movieDetails) {
                    mSelectedMovie = movieDetails;
                    // MOVIE TITLE (set the title of our activity as the movie title)
                    setTitle(movieDetails.getMovieTitle());
                    // Populate movie details section
                    populateMovieDetails(movieDetails);
                }

                @Override
                public void onLoaderReset(Loader<Details> loader) {
                    mSelectedMovie = null;
                    Log.v("SELECTED MOVIE", "BECAME NULL");
                }
            };

    private final LoaderManager.LoaderCallbacks<ArrayList<Cast>> castResultLoaderListener =
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
                    populateCast(mCast);
                }

                @Override
                public void onLoaderReset(Loader<ArrayList<Cast>> loader) {
                    mCastAdapter.swapCast(new ArrayList<Cast>() {
                    });
                }
            };

    private final LoaderManager.LoaderCallbacks<ArrayList<Review>> reviewsResultLoaderListener =
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
                    mFirstReviewAuthorTextView.setText("");
                    mFirstReviewContentTextView.setText("");
                }
            };

    private final LoaderManager.LoaderCallbacks<ArrayList<Video>> videoResultLoaderListener =
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

    private final LoaderManager.LoaderCallbacks<Cursor> favouriteMovieResultLoaderListener =
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

                                // Recreate movie genres
                                // Split the string into a string array
                                String[] genresString = TextUtils.split(cursor.getString(genresColumnIndex), ", ");
                                // Create and populate an Genre ArrayList with each genre
                                ArrayList<Genre> genres = new ArrayList<>();
                                for (int i = 0; i < genresString.length; i++) {
                                    genres.add(i, new Genre(genresString[i]));
                                }

                                // Set the extracted value from the Cursor for the given column index and use each
                                // value to create a Movie object
                                mSelectedMovie = new Details(
                                        cursor.getString(backdropColumnIndex),
                                        genres,
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
                                    getLoaderManager().initLoader(CAST_LOADER_ID, null, castResultLoaderListener);
                                    getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, reviewsResultLoaderListener);
                                    getLoaderManager().initLoader(VIDEOS_LOADER_ID, null, videoResultLoaderListener);
                                } else {
                                    closeOnError(noConnection);
                                }
                            }
                            break;
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    private final LoaderManager.LoaderCallbacks<Cursor> favouriteCastResultLoaderListener =
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

    private final LoaderManager.LoaderCallbacks<Cursor> favouriteReviewsResultLoaderListener =
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
                        int authorColumnIndex = cursor.getColumnIndex(ReviewsEntry.COLUMN_AUTHOR);
                        int contentColumnIndex = cursor.getColumnIndex(ReviewsEntry.COLUMN_CONTENT);

                        mReviews = new ArrayList<>();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            cursor.moveToPosition(i);
                            // Set the extracted value from the Cursor for the given column index and use each
                            // value to create a Review object
                            mReviews.add(new Review(
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

    private final LoaderManager.LoaderCallbacks<Cursor> favouriteVideosResultLoaderListener =
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
                        int videoKeyColumnIndex = cursor.getColumnIndex(VideosEntry.COLUMN_VIDEO_KEY);
                        int videoNameColumnIndex = cursor.getColumnIndex(VideosEntry.COLUMN_VIDEO_NAME);
                        int videoTypeColumnIndex = cursor.getColumnIndex(VideosEntry.COLUMN_VIDEO_TYPE);

                        mVideos = new ArrayList<>();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            cursor.moveToPosition(i);
                            // Set the extracted value from the Cursor for the given column index and use each
                            // value to create a Video object
                            mVideos.add(new Video(
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

    private void populateMovieDetails(final Details movieDetails) {
        // BACKDROP
        String backdropPath = movieDetails.getBackdropPath();

        // If we have a valid image path, try loading it from cache or from web with Picasso
        if (!TextUtils.isEmpty(backdropPath)) {
            final String backdropUrl = ImageUtils.buildImageUrl(getApplicationContext(), backdropPath, ImageUtils.BACKDROP);

            Picasso.get()
                    .load(backdropUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(mMovieBackdropImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            // Try again online, if loading from device memory or cache failed
                            Picasso.get()
                                    .load(backdropUrl)
                                    .error(R.drawable.ic_landscape)
                                    .into(mMovieBackdropImageView);
                        }
                    });
        } else {
            // Otherwise, don't bother using Picasso and set no_poster for mMovieBackdropImageView
            mMovieBackdropImageView.setImageResource(R.drawable.ic_landscape);
        }

        // MOVIE TITLE (set the title of our activity as the movie title)
        setTitle(movieDetails.getMovieTitle());

        // GENRE (set movie genres)
        mMovieGenreTextView.setText(movieDetails.getGenres());

        // POSTER
        // Fetch the movie poster, if it's available
        String posterPath = movieDetails.getPosterPath();

        // If we have a valid image path, try loading it from cache or from web with Picasso
        if (!TextUtils.isEmpty(posterPath)) {
            final String posterUrl = ImageUtils.buildImageUrl(getApplicationContext(), posterPath, ImageUtils.POSTER);

            Picasso.get()
                    .load(posterUrl)
                    .placeholder(R.drawable.no_poster)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(mMoviePosterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Yay!
                        }

                        @Override
                        public void onError(Exception e) {
                            // Try again online, if loading from device memory or cache failed. If no poster
                            // is available or no internet connection, use "no_poster" drawable
                            Picasso.get()
                                    .load(posterUrl)
                                    .placeholder(R.drawable.no_poster)
                                    .error(R.drawable.no_poster)
                                    .into(mMoviePosterImageView);
                        }
                    });
        } else {
            // Otherwise, don't bother using Picasso and set no_poster for mMoviePosterImageView
            mMoviePosterImageView.setImageResource(R.drawable.no_poster);
        }
        // RATINGS
        mMovieRatingTextView.setText(String.valueOf(movieDetails.getVoteAverage()).concat(getString(R.string.max_rating)));

        // RUNTIME
        int runtime = movieDetails.getRuntime();
        long hours = TimeUnit.MINUTES.toHours(runtime);
        long minutes = runtime - TimeUnit.HOURS.toMinutes(hours);

        if (hours > 0) {
            // TODO 1: Tried to use a string formatter, but kept getting an error and never understood how to do it right.
            // This is the line with error
            // mMovieRuntimeTextView.setText(String.format(getString(R.string.format_runtime), (float) hours, (float) minutes));
            mMovieRuntimeTextView.setText(TextUtils.concat(String.valueOf(hours), "h ", String.valueOf(minutes), "m"));
        } else {
            mMovieRuntimeTextView.setText(TextUtils.concat(String.valueOf(minutes), " min"));
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

    private void insertMovie(Details selectedMovie, MenuItem item) {
        int INITIAL_VALUE = -1;

        //Movie details insertion
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieDetailsEntry.COLUMN_MOVIE_ID, selectedMovie.getMovieId());
        movieValues.put(MovieDetailsEntry.COLUMN_BACKDROP_PATH, selectedMovie.getBackdropPath());
        movieValues.put(MovieDetailsEntry.COLUMN_GENRES, selectedMovie.getGenres());
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
            toastThis(insertSuccessfulMsg);
            mIsFavourite = true;
        } else {
            // Otherwise, if the new content URI is null, then there was an error with insertion.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
            toastThis(insertFailedMsg);
        }
    }

    private void deleteFavourite(Details mSelectedMovie, MenuItem item) {
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
            toastThis(deleteSuccessfulMsg);
            mIsFavourite = false;
        } else {
            // Otherwise, if no rows were affected, then there was an error with the delete.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorHeart));
            toastThis(deleteFailedMsg);
        }
    }

    private void toastThis(String toastMessage) {
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

    private Intent createShareVideoIntent() {
        // Get the video object from current position
        Video selectedVideo = mVideos.get(mVideosLayoutManager.findFirstCompletelyVisibleItemPosition());

        // Create a video summary, using the video name and type of the selected video
        String videoSummary = TextUtils.concat(
                selectedVideo.getVideoName(),
                " (", selectedVideo.getVideoType(), ") ").toString();

        // Create a videoUri for the selected video
        String videoUri = NetworkUtils.buildVideoUri(selectedVideo.getVideoKey()).toString();

        // Build the intent
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(videoSummary + MOVIES_SHARE_HASHTAG + "\n" + videoUri)
                .getIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }

        // Return the intent
        return shareIntent;
    }
}