package com.future.bestmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
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
    private MovieAdapter mAdapter;
    private RecyclerView mMoviesRecyclerView;
    private TextView mMessagesTextView;
    private ImageView mCloudImageView;
    private ProgressBar mLoading;
    private TextView mMovieCategory;
    private int mPosition = RecyclerView.NO_POSITION;
    private boolean isScrolling = false;
    private int visibleItems;
    private int totalItems;
    private int scrolledUpItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This TextView will be used to display the movie category we are in, but we use it
        // only in portrait mode, in landscape mode we always keep it hidden
        mMovieCategory = findViewById(R.id.movie_category_tv);

        mLoading = findViewById(R.id.loading_pb);
        mCloudImageView = findViewById(R.id.no_connection_cloud_iv);
        mMessagesTextView = findViewById(R.id.messages_tv);
        mMessagesTextView.setText(R.string.loading);

        mMoviesRecyclerView = findViewById(R.id.movies_rv);
        // The layout manager for our RecyclerView will be a GridLayout, so we can display our movies
        // on columns. The number of columns is dictated by the orientation and size of the device
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(
                this,
                ScreenUtils.getNumberOfColumns(this));

        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(true);
        mAdapter = new MovieAdapter(this, new Movie[]{}, this);
        mMoviesRecyclerView.setAdapter(mAdapter);

        // To create an infinite scrolling effect, we add an OnScrollListener to our RecyclerView
        mMoviesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // To be able to load data in advance, before the user gets to the bottom of our
                // present results, we have to know how many items are visible on the screen, how
                // many items are in total and how many items are already scrolled out of the screen
                visibleItems = gridLayoutManager.getChildCount();
                totalItems = gridLayoutManager.getItemCount();
                scrolledUpItems = gridLayoutManager.findFirstVisibleItemPosition();

                // We set a threshold, to help us know that the use is about to get to the end of
                // the list.
                int threshold = 5;

                // If the user is still scrolling and the the Threshold is bigger or equal with the
                // totalItems - visibleItems - scrolledUpItems, we know we have to load new Movies
                if (isScrolling && (threshold >= totalItems - visibleItems - scrolledUpItems)) {
                    isScrolling = false;
                    Log.v(TAG, "Load new movies!");
                    loadNewMovies();
                }
            }
        });

        // Check if preference "image_width" was create before, if not, proceed.
        if (!MoviePreferences.isImageWidthAvailable(this)) {
            // Create an image width preference for our RecyclerView
            // This preference is very useful to our RecyclerView, so we can download all the images
            // for the RecyclerView heaving the same width, perfect for the device we are using.
            // We measure once and use it as many times we want.
            MoviePreferences.setImageWidthForRecyclerView(
                    this,
                    ImageUtils.getImageWidth(this, ImageUtils.POSTER));
        }

        // Every time we create this activity we set the page number of our results to be 0
        MoviePreferences.setLastPageNumber(this, 0);

        // Show progress bar
        mLoading.setVisibility(View.VISIBLE);

        // If there is a network connection, fetch data
        fetchDataIfConnected(this);
    }

    private void loadNewMovies() {
        mLoading.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchDataIfConnected(getApplicationContext());
                mLoading.setVisibility(View.GONE);
            }
        }, 1000);
    }

    // Fetch data if connection is available
    private void fetchDataIfConnected(Context context) {
        // If there is a network connection, fetch data
        if (NetworkUtils.isConnected(context)) {
            // Before we fetch data, we need the last page number that was loaded in our RecyclerView,
            // increment it by 1 and save it in a preference for next data fetching
            int nextPage = MoviePreferences.getLastPageNumber(context) + 1;
            MoviePreferences.setLastPageNumber(getApplicationContext(), nextPage);
            //Init or restart loader
            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        } else {
            // Otherwise, hide loading indicator, hide data and display connection error message
            showError();
            // Update message TextView with no connection error message
            mMessagesTextView.setText(R.string.no_internet);

            // Every time we have a connection error, we set the page number of our results to be 0
            MoviePreferences.setLastPageNumber(this, 0);
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

    // Every time we click a movie poster, we create an intent and pass a Movie object along with it,
    // so we can display all the information that we received about it, without heaving to fetch
    // more data from movie API server
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
        // Every time we get new results we have 2 possibilities
        int currentPage = MoviePreferences.getLastPageNumber(getApplicationContext());
        // If currentPage is "1", we know that the user has changed the movie category or uses the
        // app for the first time. In this situation we swap the Movie array with the new data
        if (currentPage == 1) {
            mAdapter.swapMovies(data);
        } else {
            // Otherwise, we add the new data to the old data, creating an infinite scrolling effect
            mAdapter.mergeMovies(data);
        }

        // If our RecyclerView has is not position, we assume the first position in the list
        // and set the RecyclerView a the beginning of our results
        if (mPosition == RecyclerView.NO_POSITION) {
            mPosition = 0;
            mMoviesRecyclerView.smoothScrollToPosition(mPosition);
        }

        // If new data is available
        if (data.length != 0) {
            // Hide progress bar
            mLoading.setVisibility(View.GONE);
            // Set the text for mMovieCaterogory as the selected category
            mMovieCategory.setText(ScreenUtils.createCategoryTitle(this));
        }
    }

    @Override
    public void onLoaderReset(Loader<Movie[]> loader) {
        // If the loader is reset, swap old data with null ones
        mAdapter.swapMovies(new Movie[]{});
    }

    // Hide the movie data and loading indicator and show error message
    private void showError() {
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mCloudImageView.setVisibility(View.VISIBLE);
        mMessagesTextView.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
    }
}