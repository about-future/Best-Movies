package com.future.bestmovies;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Parcel;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.future.bestmovies.data.Cast;
import com.future.bestmovies.data.CastAdapter;
import com.future.bestmovies.data.CastLoader;
import com.future.bestmovies.data.Movie;
import com.future.bestmovies.data.Review;
import com.future.bestmovies.data.ReviewAdapter;
import com.future.bestmovies.data.ReviewLoader;
import com.future.bestmovies.data.Video;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.MovieUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.future.bestmovies.utils.ScreenUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class DetailsActivity extends AppCompatActivity {
    private static final int CAST_LOADER_ID = 34;
    private static final int REVIEWS_LOADER_ID = 435;
    private static final int VIDEOS_LOADER_ID = 594;

    public static final String MOVIE_OBJECT = "movie";
    private static final String MOVIE_CAST = "movie_cast";
    private static final String CAST_POSITION = "cast_position";
    public static final String MOVIE_REVIEWS = "movie_reviews";
    public static final String MOVIE_VIDEOS = "movie_videos";
    public static final String MOVIE_ID = "movie_id";
    public static final String MOVIE_TITLE = "movie_title";
    public static final String MOVIE_BACKDROP = "movie_backdrop";

    private Movie mSelectedMovie;
    private ConstraintLayout mMovieDetailsLayout;
    private TextView mMessagesTextView;
    private TextView mCastMessagesTextView;
    private ImageView mCloudImageView;

    private int mCastPosition = RecyclerView.NO_POSITION;
    private CastAdapter mCastAdapter;
    private LinearLayoutManager mCastLayoutManager;
    private RecyclerView mCastRecyclerView;
    private ProgressBar mCastProgressBar;
    private ArrayList<Cast> mCast;

    private ConstraintLayout mFirstReviewLayout;
    private TextView mFirstReviewAuthorTextView;
    private TextView mFirstReviewContentTextView;
    private ProgressBar mReviewProgressBar;
    private TextView mReviewMessagesTextView;
    private TextView mSeeAllReviewsTextView;
    private ArrayList<Review> mReviews;

    private ArrayList<Video> mVideos;


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

        mMovieDetailsLayout = findViewById(R.id.movie_details_layout);
        mCloudImageView = findViewById(R.id.no_connection_cloud_iv);
        mMessagesTextView = findViewById(R.id.messages_tv);
        mCastMessagesTextView = findViewById(R.id.cast_messages_tv);
        mCastMessagesTextView.setText(R.string.loading);
        final ImageView movieBackdropImageView = findViewById(R.id.details_backdrop_iv);
        final ImageView moviePosterImageView = findViewById(R.id.details_poster_iv);
        TextView moviePlotTextView = findViewById(R.id.details_plot_tv);
        TextView movieGenreTextView = findViewById(R.id.details_genre_tv);
        mCastRecyclerView = findViewById(R.id.cast_rv);

        mCastProgressBar = findViewById(R.id.loading_cast_pb);
        // Set the progress bar color to colorAccent, if SDK is lower than API21
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mCastProgressBar.getIndeterminateDrawable().setColorFilter(getResources()
                    .getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        }

        // The layout manager for our Cast RecyclerView will be a LinerLayout, so we can display
        // our cast on a single line, horizontally
        mCastLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mCastRecyclerView.setLayoutManager(mCastLayoutManager);
        mCastRecyclerView.setHasFixedSize(true);
        mCastAdapter = new CastAdapter(this, new ArrayList<Cast>());
        mCastRecyclerView.setAdapter(mCastAdapter);

        // Reviews
        mFirstReviewLayout = findViewById(R.id.first_review_layout);
        mFirstReviewAuthorTextView = findViewById(R.id.first_review_author_tv);
        mFirstReviewContentTextView = findViewById(R.id.first_review_content_tv);
        mReviewProgressBar = findViewById(R.id.loading_first_review_pb);
        mReviewMessagesTextView = findViewById(R.id.first_review_messages_tv);
        mReviewMessagesTextView.setText(R.string.loading);
        mSeeAllReviewsTextView = findViewById(R.id.see_all_reviews_tv);

        // If we have an instance saved and contains our movie object, we use it to populate our UI
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_OBJECT)) {
            mSelectedMovie = savedInstanceState.getParcelable(MOVIE_OBJECT);
        } else {
            // Otherwise, we check our intent and see if there is a Movie object passed from
            // MainActivity, so we can populate our UI. If there isn't we close this activity and
            // display a toast message.
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(MOVIE_OBJECT)) {
                mSelectedMovie = intent.getParcelableExtra(MOVIE_OBJECT);
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

        // Populating UI with information
        // Set the title of our activity as the movie title
        setTitle(mSelectedMovie.getMovieTitle());
        // Generate and set movie genres
        String[] movieGenre = new String[mSelectedMovie.getGenreIds().length];
        for (int i = 0; i < mSelectedMovie.getGenreIds().length; i++) {
            movieGenre[i] = MovieUtils.getStringMovieGenre(this, mSelectedMovie.getGenreIds()[i]);
        }
        movieGenreTextView.setText(TextUtils.join(", ", movieGenre));
        // Set the movie plot
        moviePlotTextView.setText(mSelectedMovie.getOverview());

        // Set the ratings and release date
        ConstraintLayout ratingsLandscape = findViewById(R.id.ratings_landscape);
        ConstraintLayout ratingsPortrait = findViewById(R.id.ratings_portrait);
        TextView movieRatingTextView;
        TextView movieReleaseDateTextView;
        // If the user has a tablet or user uses the device in landscape mode, always show the
        // ratings and release date from layout ratings_landscape (placed inside the poster_and_plot layout)
        // AND hide the ratings_portrait
        if (ScreenUtils.getSmallestScreenWidthInDps(this) >= 600 || ScreenUtils.isLandscapeMode(this)) {
            movieRatingTextView = findViewById(R.id.details_ratings_landscape_tv);
            movieReleaseDateTextView = findViewById(R.id.details_release_date_landscape_tv);
            ratingsLandscape.setVisibility(View.VISIBLE);
            ratingsPortrait.setVisibility(View.GONE);
        } else {
            // Otherwise, the user has a phone (smallest width < 600DPs) and uses it in portrait mode.
            // In this case, show ratings_portrait and hide ratings_landscape
            movieRatingTextView = findViewById(R.id.details_ratings_portrait_tv);
            movieReleaseDateTextView = findViewById(R.id.details_release_date_portrait_tv);
            ratingsPortrait.setVisibility(View.VISIBLE);
            ratingsLandscape.setVisibility(View.GONE);
        }

        // Populate the allocated rating and release date TextViews
        movieRatingTextView.setText(String.valueOf(mSelectedMovie.getVoteAverage()));
        movieReleaseDateTextView.setText(mSelectedMovie.getReleaseDate());

        final TextView backdropErrorTextView = findViewById(R.id.backdrop_error_tv);
        final TextView posterErrorTextView = findViewById(R.id.poster_error_tv);

        // Fetch movie backdrop if it's available
        Picasso.with(this)
                .load(ImageUtils.buildImageUrl(
                        this,
                        mSelectedMovie.getBackdropPath(),
                        ImageUtils.BACKDROP))
                .error(R.drawable.ic_landscape)
                .into(movieBackdropImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        backdropErrorTextView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        backdropErrorTextView.setVisibility(View.VISIBLE);
                        // If there isn't a network connection, we show a "no connection" message
                        if (!NetworkUtils.isConnected(getApplicationContext())) {
                            backdropErrorTextView.setText(getString(R.string.no_connection));
                        } else {
                            // Otherwise, we show "no_backdrop" message
                            backdropErrorTextView.setText(getString(R.string.no_backdrop));
                        }
                        // Set backdrop content description for error case
                        movieBackdropImageView.setContentDescription(getString(R.string.no_backdrop));
                    }
                });

        // Fetch movie poster, if it's available
        if (ScreenUtils.isLandscapeMode(this)) {
            moviePosterImageView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.poster_width);
        }
        Picasso.with(this)
                .load(ImageUtils.buildImageUrl(
                        this,
                        mSelectedMovie.getPosterPath(),
                        ImageUtils.POSTER))
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

        // Show the Cast progress bar and hide the Cast RecyclerView
        hideCast();

        // Fetch movie cast
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_CAST)) {
            mCast = savedInstanceState.getParcelableArrayList(MOVIE_CAST);
            mCastAdapter = new CastAdapter(this, mCast);
            mCastRecyclerView.setAdapter(mCastAdapter);
            if (savedInstanceState.containsKey(CAST_POSITION)) {
                mCastPosition = savedInstanceState.getInt(CAST_POSITION);
            }
            populateCast(mCast);
        } else {
            getLoaderManager().initLoader(CAST_LOADER_ID, null, castResultLoaderListener);
        }

        // Fetch movie reviews
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_REVIEWS)) {
            mReviews = savedInstanceState.getParcelableArrayList(MOVIE_REVIEWS);
            populateReviews(mReviews);
        } else {
            getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, reviewsResultLoaderListener);
        }
//        } else {
//            // Otherwise, hide data and display connection error message
//            showError();
//            // Update message TextView with no connection error message
//            mMessagesTextView.setText(R.string.no_internet);
//            // And set collapsing toolbar as collapsed (not expanded)
//            AppBarLayout myAppBar = findViewById(R.id.app_bar);
//            myAppBar.setExpanded(false);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
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
        //outState.putParcelableArrayList(MOVIE_VIDEOS, mVideos);
        super.onSaveInstanceState(outState);
    }

    // Hide the progress bar and show cast
    private void showCast() {
        mCastRecyclerView.setVisibility(View.VISIBLE);
        mCastProgressBar.setVisibility(View.INVISIBLE);
        mCastMessagesTextView.setVisibility(View.INVISIBLE);
    }

    // Show progress bar and hide cast
    private void hideCast() {
        mCastRecyclerView.setVisibility(View.INVISIBLE);
        mCastProgressBar.setVisibility(View.VISIBLE);
        mCastMessagesTextView.setVisibility(View.VISIBLE);
    }

    // Hide the progress bar and show reviews
    private void showReview() {
        mFirstReviewLayout.setVisibility(View.VISIBLE);
        mReviewProgressBar.setVisibility(View.INVISIBLE);
        mReviewMessagesTextView.setVisibility(View.INVISIBLE);
    }

    // Show progress bar and hide reviews
    private void hideReview() {
        mFirstReviewLayout.setVisibility(View.INVISIBLE);
        mReviewProgressBar.setVisibility(View.VISIBLE);
        mReviewMessagesTextView.setVisibility(View.VISIBLE);
        mSeeAllReviewsTextView.setVisibility(View.INVISIBLE);
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
            }
        } else {
            Log.v("CAST", "IS NULL!");
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
                showReview();
                mFirstReviewAuthorTextView.setText(movieReviews.get(0).getReviewAuthor());
                mFirstReviewContentTextView.setText(movieReviews.get(0).getReviewContent());
            } else {
                // Otherwise, hide progress bar and show "No reviews available" message
                hideReview();
                mReviewMessagesTextView.setVisibility(View.VISIBLE);
                mReviewMessagesTextView.setText(R.string.no_reviews);
                mReviewProgressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            Log.v("REVIEWS", "ARE NULL!");
        }
    }
}