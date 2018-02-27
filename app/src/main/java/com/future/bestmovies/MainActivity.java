package com.future.bestmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.data.MovieLoader;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        MovieAdapter.GridItemClickListener,
        LoaderManager.LoaderCallbacks<Movie[]> {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int MOVIES_LOADER_ID = 24;
    private MovieAdapter mAdapter;
    private RecyclerView mMoviesRecyclerView;
    private TextView mEmptyStateTextView;
    private ProgressBar mLoading;
    private int mPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if preference screen width was create, if not proceed with the measurement
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp.contains(getString(R.string.pref_screen_width_key))) {
            ImageUtils.createScreenSizePreference(this);
        }

        mMoviesRecyclerView = findViewById(R.id.movies_rv);
        mLoading = findViewById(R.id.loading_pb);
        mEmptyStateTextView = findViewById(R.id.emptyView);
        mEmptyStateTextView.setText(R.string.loading);

        // The number of columns in our RecyclerView is determined by the orientation of the device
        int numberOfColumns;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // If in portrait mode we have two columns
            numberOfColumns = 2;
        } else {
            // Otherwise assuming that we are in landscape mode, we'll have three columns
            numberOfColumns = 3;
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfColumns);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(true);
        mAdapter = new MovieAdapter(this, new Movie[]{}, this);
        mMoviesRecyclerView.setAdapter(mAdapter);

        showLoading();

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            //Loader init
            showMovies();
            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);

        } else {
            // Otherwise, hide loading indicator, hide data and display connection error message
            showError();
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet);
        }
    }

    // TODO: Make preferences to influence the results
    // TODO: Explain everything for others

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

    @Override
    public void onGridItemClick(Movie movieClicked) {
        Intent movieDetailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
        movieDetailsIntent.putExtra(DetailsActivity.MOVIE_OBJECT, movieClicked);
        startActivity(movieDetailsIntent);
    }

    private void showMovies() {
        // Hide the text and loading indicator and show movie data
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
        mEmptyStateTextView.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
    }

    private void showLoading() {
        // Hide the movie data and show loading indicator and text
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mEmptyStateTextView.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }

    private void showError() {
        // Hide the movie data and loading indicator and show error message
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
        mEmptyStateTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<Movie[]> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case MOVIES_LOADER_ID:
                // This method returns the API URL used to get our JSON results
                return new MovieLoader(this, NetworkUtils.getUrl(this));
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Movie[]> loader, Movie[] data) {
        mAdapter.swapMovies(data);

        if (mPosition == RecyclerView.NO_POSITION) {
            mPosition = 0;
            mMoviesRecyclerView.smoothScrollToPosition(mPosition);
        }

        if (data != null && data.length != 0)  {
//            if (mPosition == 0) {
//                if (!TextUtils.isEmpty(data[0].getBackdropPath())) {
//                    String mainBackdropUrl = ImageUtils.buildImageUrlWithImageType(
//                            this,
//                            data[0].getBackdropPath(),
//                            ImageUtils.BACKDROP);
//                    Picasso.with(this)
//                            .load(mainBackdropUrl)
//                            .into(mainBackdropImageView);
//                }
//            }

            showMovies();
        }
    }

    @Override
    public void onLoaderReset(Loader<Movie[]> loader) {mAdapter.swapMovies(null);
    }
}