package com.future.bestmovies;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.data.MovieLoader;
import com.future.bestmovies.data.MoviePreferences;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.future.bestmovies.utils.ScreenUtils;

public class MainActivity extends AppCompatActivity implements
        MovieAdapter.GridItemClickListener,
        LoaderManager.LoaderCallbacks<Movie[]> {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int MOVIES_LOADER_ID = 24;
    private static final String ACTION = "Action";
    private MovieAdapter mAdapter;
    private RecyclerView mMoviesRecyclerView;
    private TextView mMessagesTextView;
    private ImageView mCloudImageView;
    private ProgressBar mLoading;
    private TextView mMovieCategory;
    private LinearLayout mNavigationLayout;
    private TextView mPageNumber;
    private int mPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Number of columns for our RecyclerView
        int numberOfColumns;

        // The number of columns in our RecyclerView is determined by the orientation of the device
        if (ScreenUtils.isLandscapeMode(this)) {
            // Set a different theme for our layout, if the device's orientation is in landscape mode
            setTheme(R.style.AppTheme);

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // In landscape mode, we'll have three columns and no toolbar as the ActionBar
            // our ActionBar will be the default one, provided by AppTheme
            numberOfColumns = 3;
        } else {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // If in portrait mode we have two columns
            numberOfColumns = 2;

            // We initialize and set the toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            // This FAB will be used later on, to allow users to see the trailer of the best movie
            // in the selected category... or maybe something else
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, getString(R.string.fab_message), Snackbar.LENGTH_LONG)
                            .setAction(ACTION, null).show();
                }
            });
        }

        // This TextView will be used to display the movie category we are in, but we use it
        // only in portrait mode, in landscape mode we always keep it hidden
        mMovieCategory = findViewById(R.id.movie_category_tv);

        mMoviesRecyclerView = findViewById(R.id.movies_rv);
        mLoading = findViewById(R.id.loading_pb);
        mCloudImageView = findViewById(R.id.no_connection_cloud_iv);
        mMessagesTextView = findViewById(R.id.messages_tv);
        mMessagesTextView.setText(R.string.loading);
        mNavigationLayout = findViewById(R.id.navigation);
        mPageNumber = findViewById(R.id.page_number_tv);


        // The layout manager for our RecyclerView will be a GridLayout, so we can display
        // our movies on columns. The number of columns is dictated by the orientation of the device
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfColumns);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(true);

        // Check if preference "screen width" was create before, if not proceed with the measurement
        // This preference is very useful to our RecyclerView, so we can download all the images for
        // the RecyclerView heaving the same width. We measure once and use it as many times we want.
        if (!MoviePreferences.isImageSizeAvailable(this)) {
            ImageUtils.createScreenSizePreference(this);
        }

        mAdapter = new MovieAdapter(this, new Movie[]{}, this);
        mMoviesRecyclerView.setAdapter(mAdapter);

        showLoading();

        // If there is a network connection, fetch data
        fetchDataIfConnected(this);
    }
    // TODO: Explain everything for others
    // TODO: See if you can monitor the connection and load the movies when connection is available again
    // TODO: Create layout for Tablets, create Settings layout for landscape mode

    private void fetchDataIfConnected(Context context){
        // If there is a network connection, fetch data
        if(NetworkUtils.isConnected(context)){
            showMovies();
            //Loader init
            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        } else {
            // Otherwise, if there is no network connection, in portrait mode, we will get a
            // reference for our collapsing bar and set it collapsed
            if (!ScreenUtils.isLandscapeMode(context)) {
                AppBarLayout myAppBar = findViewById(R.id.app_bar);
                myAppBar.setExpanded(false);
            }

            // Otherwise, hide loading indicator, hide data and display connection error message
            showError();
            // Update message TextView with no connection error message
            mMessagesTextView.setText(R.string.no_internet);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Every time we click a poster, we create an intent and pass a Movie object along with it,
    // so we can display all the information that we received about it, without heaving to fetch
    // data from movie Api server
    @Override
    public void onGridItemClick(Movie movieClicked) {
        Intent movieDetailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
        movieDetailsIntent.putExtra(DetailsActivity.MOVIE_OBJECT, movieClicked);
        startActivity(movieDetailsIntent);
    }

    @Override
    public Loader<Movie[]> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case MOVIES_LOADER_ID:
                // If the loaded id matches ours, return a new movie loader
                return new MovieLoader(this);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Movie[]> loader, Movie[] data) {
        // Every time we get new results we swap the old ones with the new ones
        mAdapter.swapMovies(data);

        // If our RecyclerView has is not position, we assume the first position in the list
        // and set the RecyclerView a the beginning of our results
        if (mPosition == RecyclerView.NO_POSITION) {
            mPosition = 0;
            mMoviesRecyclerView.smoothScrollToPosition(mPosition);
        }

        if (data.length != 0) {
            // Show results
            showMovies();
            // If new data is available, create a subtitle with our new movie category
            mMovieCategory.setText(ScreenUtils.createCategoryTitle(this));
            mPageNumber.setText(MoviePreferences.getLastPageNumber(this));
        }
    }

    @Override
    public void onLoaderReset(Loader<Movie[]> loader) {
        // If the loader is reset, swap old data with null ones
        mAdapter.swapMovies(null);
    }

    // Hide the text and loading indicator and show movie data
    private void showMovies() {
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
        mNavigationLayout.setVisibility(View.VISIBLE);
        mCloudImageView.setVisibility(View.INVISIBLE);
        mMessagesTextView.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
    }

    // Hide the movie data and show loading indicator and text
    private void showLoading() {
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mNavigationLayout.setVisibility(View.INVISIBLE);
        mCloudImageView.setVisibility(View.INVISIBLE);
        mMessagesTextView.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }

    // Hide the movie data and loading indicator and show error message
    private void showError() {
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mNavigationLayout.setVisibility(View.INVISIBLE);
        mCloudImageView.setVisibility(View.VISIBLE);
        mMessagesTextView.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
    }

    public void nextPage(View v) {
        Context context = getApplicationContext();
        String currentPage = MoviePreferences.getLastPageNumber(context);
        int nextPage = Integer.parseInt(currentPage) + 1;
        MoviePreferences.setLastPageNumber(getApplicationContext(), String.valueOf(nextPage));
        fetchDataIfConnected(context);
    }

    public void previousPage(View v) {
        Context context = getApplicationContext();
        String currentPage = MoviePreferences.getLastPageNumber(context);
        int nextPage;
        if (TextUtils.equals(currentPage, "1")) {
            nextPage = 1;
        } else {
            nextPage = Integer.parseInt(currentPage) - 1;
        }
        MoviePreferences.setLastPageNumber(getApplicationContext(), String.valueOf(nextPage));
        fetchDataIfConnected(context);
    }
}