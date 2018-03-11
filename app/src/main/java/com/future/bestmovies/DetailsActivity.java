package com.future.bestmovies;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.future.bestmovies.data.Cast;
import com.future.bestmovies.data.CastLoader;
import com.future.bestmovies.data.Movie;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.MovieUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.future.bestmovies.utils.ScreenUtils;
import com.squareup.picasso.Picasso;


public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cast[]> {
    public static final int CAST_LOADER_ID = 34;
    public static final String MOVIE_OBJECT = "movie";
    private Movie mSelectedMovie;
    private ConstraintLayout mMovieDetailsLayout;
    private TextView mMessagesTextView;
    private TextView mCastMessagesTextView;
    private ImageView mCloudImageView;

    private int mPosition = RecyclerView.NO_POSITION;
    private CastAdapter mAdapter;
    private RecyclerView mCastRecyclerView;
    private ProgressBar mCastProgressBar;

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
        ImageView movieBackdropImageView = findViewById(R.id.details_backdrop_iv);
        ImageView moviePosterImageView = findViewById(R.id.details_poster_iv);
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
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mCastRecyclerView.setLayoutManager(layoutManager);
        mCastRecyclerView.setHasFixedSize(true);
        mAdapter = new CastAdapter(this, new Cast[]{});
        mCastRecyclerView.setAdapter(mAdapter);

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
            // Otherwise, the user has a phone (width < 600DPs) and uses it in portrait mode.
            // In this case, show ratings_portrait and hide ratings_landscape
            movieRatingTextView = findViewById(R.id.details_ratings_portrait_tv);
            movieReleaseDateTextView = findViewById(R.id.details_release_date_portrait_tv);
            ratingsPortrait.setVisibility(View.VISIBLE);
            ratingsLandscape.setVisibility(View.GONE);
        }

        // Populate the allocated rating and release date TextViews
        movieRatingTextView.setText(String.valueOf(mSelectedMovie.getVoteAverage()));
        movieReleaseDateTextView.setText(mSelectedMovie.getReleaseDate());

        // Because all the information so far was provided by the Movie object and no internet
        // connection was necessary, the next 3 areas of our UI will be populated if and only if we
        // have an internet connection.
        // The movie backdrop, poster and cast depend on it

        // First we show the progress bar and hide the Cast RecyclerView
        hideCast();

        // If there is a network connection, we show all the movie details and fetch cast data
        if (NetworkUtils.isConnected(this)) {
            // Show movie details
            showMovieDetails();

            // Fetch movie backdrop if it's available
            if (!mSelectedMovie.getBackdropPath().equals("null")) {
                Picasso.with(this)
                        .load(ImageUtils.buildImageUrlWithImageType(
                                this,
                                mSelectedMovie.getBackdropPath(),
                                ImageUtils.BACKDROP))
                        .into(movieBackdropImageView);
            } else {
                // Otherwise, set the default backdrop and chance the content description
                movieBackdropImageView.setImageResource(R.drawable.ic_landscape);
                movieBackdropImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                movieBackdropImageView.setContentDescription(getString(R.string.no_backdrop));
            }

            // Fetch movie poster, if it's available
            if (!mSelectedMovie.getPosterPath().equals("null")) {
                if (ScreenUtils.isLandscapeMode(this)) {
                    moviePosterImageView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.poster_width);
                }
                Picasso.with(this)
                        .load(ImageUtils.buildImageUrlWithImageType(
                                this,
                                mSelectedMovie.getPosterPath(),
                                ImageUtils.POSTER))
                        .into(moviePosterImageView);
            } else {
                // Otherwise, set the default poster and chance the content description
                moviePosterImageView.setImageResource(R.drawable.ic_local_movies);
                moviePosterImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                moviePosterImageView.setContentDescription(getString(R.string.no_poster));
            }
            // Fetch movie cast
            getLoaderManager().initLoader(CAST_LOADER_ID, null, this);
        } else {
            hideCast();
            // Otherwise, hide data and display connection error message
            showError();
            // Update message TextView with no connection error message
            mMessagesTextView.setText(R.string.no_internet);
            // And set collapsing toolbar as collapsed (not expanded)
            AppBarLayout myAppBar = findViewById(R.id.app_bar);
            myAppBar.setExpanded(false);
        }
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
        outState.putParcelable(MOVIE_OBJECT, mSelectedMovie);
        super.onSaveInstanceState(outState);
    }

    // Hide the error text and show movie data
    private void showMovieDetails() {
        mMovieDetailsLayout.setVisibility(View.VISIBLE);
        mCloudImageView.setVisibility(View.INVISIBLE);
        mMessagesTextView.setVisibility(View.INVISIBLE);
    }

    // Hide the movie data and show error message
    private void showError() {
        mMovieDetailsLayout.setVisibility(View.INVISIBLE);
        mCloudImageView.setVisibility(View.VISIBLE);
        mMessagesTextView.setVisibility(View.VISIBLE);
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

    @Override
    public Loader<Cast[]> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case CAST_LOADER_ID:
                // If the loaded id matches ours, return a new cast movie loader
                return new CastLoader(this, String.valueOf(mSelectedMovie.getMovieId()));
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cast[]> loader, Cast[] movieCast) {
        mAdapter.swapCast(movieCast);

        // If our RecyclerView has is not position, we assume the first position in the list
        // and set the RecyclerView a the beginning of our results
        if (mPosition == RecyclerView.NO_POSITION) {
            mPosition = 0;
            mCastRecyclerView.smoothScrollToPosition(mPosition);
        }

        // If the movieCast has data
        if (movieCast.length != 0) {
            // Show movie cast
            showCast();
        } else {
            // Otherwise, hide progress bar and show "No cast available" message
            mCastMessagesTextView.setVisibility(View.VISIBLE);
            mCastMessagesTextView.setText(R.string.no_cast);
            mCastProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cast[]> loader) {
        mAdapter.swapCast(new Cast[]{});
    }
}