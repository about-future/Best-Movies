package com.future.bestmovies;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.data.MovieAdapter;
import com.future.bestmovies.data.MovieLoader;
import com.future.bestmovies.data.MoviePreferences;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.future.bestmovies.utils.ScreenUtils;

import java.util.ArrayList;

import static com.future.bestmovies.data.FavouritesContract.*;


public class MainActivity extends AppCompatActivity implements
        MovieAdapter.GridItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String MOVIES_CURSOR = "movies_cursor";
    private static final String MOVIES_LIST = "movies_list";
    private static final String FAVOURITES = "favourites";
    private static final String MOVIE_POSITION = "movie_position";
    private static final String CURRENT_LOADED_ID = "loader_id";
    private static final int MOVIES_LOADER_ID = 24;
    private static final int FAVOURITES_LOADER_ID = 516;
    private int mCurrentLoaderId;

    public static final String[] FAVOURITES_MOVIE_PROJECTION = {
            MovieDetailsEntry.COLUMN_MOVIE_ID,
            MovieDetailsEntry.COLUMN_POSTER_PATH
    };

    private MovieAdapter mAdapter;
    private RecyclerView mMoviesRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private TextView mMessagesTextView;
    private ImageView mCloudImageView;
    private ProgressBar mLoading;

    private int mPosition = RecyclerView.NO_POSITION;
    private boolean isScrolling = false;
    private int visibleItems;
    private int totalItems;
    private int scrolledUpItems;

    private Cursor mMoviesCursor;
    private ArrayList<Movie> mMovies;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoading = findViewById(R.id.loading_pb);
        mLoading.setVisibility(View.VISIBLE);
        mCloudImageView = findViewById(R.id.no_connection_cloud_iv);
        mMessagesTextView = findViewById(R.id.messages_tv);
        mMoviesRecyclerView = findViewById(R.id.movies_rv);
        // The layout manager for our RecyclerView will be a GridLayout, so we can display our movies
        // on columns. The number of columns is dictated by the orientation and size of the device
        mGridLayoutManager = new GridLayoutManager(this, ScreenUtils.getNumberOfColumns(this));
        mMoviesRecyclerView.setLayoutManager(mGridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(true);
        mAdapter = new MovieAdapter(this, new ArrayList<Movie>() {
        }, this);
        mMoviesRecyclerView.setAdapter(mAdapter);

        // Check if preference "image_width" was create before, if not, proceed.
        if (!MoviePreferences.isImageWidthAvailable(this)) {
            // Create an image width preference for our RecyclerView
            // This preference is very useful to our RecyclerView, so we can load all the images
            // for the RecyclerView heaving the same width, perfect for the device we are using.
            // We measure once and use it as many times we want.
            MoviePreferences.setImageWidthForRecyclerView(
                    this,
                    ImageUtils.getImageWidth(this, ImageUtils.POSTER));
        }

        // Every time we create this activity we set the page number of our results to be 0
        MoviePreferences.setLastPageNumber(this, 0);

        // Check chosen category
        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_LOADED_ID)) {
            mCurrentLoaderId = savedInstanceState.getInt(CURRENT_LOADED_ID);
            Log.v ("SAVED INSTANCE", "CURRENT LOADER: " + String.valueOf(mCurrentLoaderId));
            if (mCurrentLoaderId == FAVOURITES_LOADER_ID) {
                getLoaderManager().restartLoader(FAVOURITES_LOADER_ID, null, favouriteMoviesLoaderListener);
            } else {
                fetchMovies(this);
                //getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
            }
        } else {
            Log.v ("NO INSTANCE SAVE", "CURRENT LOADER: " + String.valueOf(mCurrentLoaderId));
            MoviePreferences.setPreferredQueryType(this, getString(R.string.pref_category_popular));
            fetchMovies(this);
            //MoviePreferences.setLastPageNumber(this, 1);
            //getLoaderManager().initLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
            //currentLoaderId = MOVIES_LOADER_ID;
        }

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

                // This scroll listener will be used only if the selected category is not favourites
                if (!TextUtils.equals(MoviePreferences.getPreferredQueryType(getApplicationContext()), FAVOURITES)) {
                    // To be able to load data in advance, before the user gets to the bottom of our
                    // present results, we have to know how many items are visible on the screen, how
                    // many items are in total and how many items are already scrolled out of the screen
                    visibleItems = mGridLayoutManager.getChildCount();
                    totalItems = mGridLayoutManager.getItemCount();
                    scrolledUpItems = mGridLayoutManager.findFirstVisibleItemPosition();

                    // We set a threshold, to help us know that the user is about to get to the end of
                    // the list.
                    int threshold = 5;

                    // If the user is still scrolling and the the Threshold is bigger or equal with the
                    // totalItems - visibleItems - scrolledUpItems, we know we have to load new Movies
                    if (isScrolling && (threshold >= totalItems - visibleItems - scrolledUpItems)) {
                        isScrolling = false;
                        Log.v(TAG, "Load new movies!");
                        //loadNewMovies();
                    }
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_LOADED_ID, mCurrentLoaderId);
        Log.v ("ON INSTANCE SAVE", "CURRENT LOADER: " + String.valueOf(mCurrentLoaderId));
        //outState.putInt(MOVIE_POSITION, mGridLayoutManager.findFirstCompletelyVisibleItemPosition());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentLoaderId = savedInstanceState.getInt(CURRENT_LOADED_ID);
            if (mCurrentLoaderId == FAVOURITES_LOADER_ID)
                getLoaderManager().initLoader(mCurrentLoaderId, null, favouriteMoviesLoaderListener);
            else
                getLoaderManager().initLoader(mCurrentLoaderId, null, moviesLoaderListener);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void loadNewMovies() {
        mLoading.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchMovies(getApplicationContext());
                mLoading.setVisibility(View.GONE);
            }
        }, 1000);
    }

    // Fetch data if connection is available
    private void fetchMovies(Context context) {
        // If there is a network connection, fetch data
        if (NetworkUtils.isConnected(context)) {
            // Before we fetch data, we need the last page number that was loaded in our RecyclerView,
            // increment it by 1 and save it in a preference for next data fetching
            int nextPage = MoviePreferences.getLastPageNumber(context) + 1;
            MoviePreferences.setLastPageNumber(context, nextPage);
            //Init or restart loader
            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
            mCurrentLoaderId = MOVIES_LOADER_ID;
            Log.v ("FETCH MOVIES", "CURRENT LOADER: " + String.valueOf(mCurrentLoaderId));
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
        switch (item.getItemId()) {
            case R.id.action_popular:
                MoviePreferences.setPreferredQueryType(this, getString(R.string.pref_category_popular));
                MoviePreferences.setLastPageNumber(this, 1);
                getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
                mCurrentLoaderId = MOVIES_LOADER_ID;
                break;
            case R.id.action_top_rated:
                MoviePreferences.setPreferredQueryType(this, getString(R.string.pref_category_top_rated));
                MoviePreferences.setLastPageNumber(this, 1);
                getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
                mCurrentLoaderId = MOVIES_LOADER_ID;
                break;
            case R.id.action_upcoming:
                MoviePreferences.setPreferredQueryType(this, getString(R.string.pref_category_upcoming));
                MoviePreferences.setLastPageNumber(this, 1);
                getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
                mCurrentLoaderId = MOVIES_LOADER_ID;
                break;
            case R.id.action_now_playing:
                MoviePreferences.setPreferredQueryType(this, getString(R.string.pref_category_now_playing));
                MoviePreferences.setLastPageNumber(this, 1);
                getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
                mCurrentLoaderId = MOVIES_LOADER_ID;
                break;
            default:
                MoviePreferences.setPreferredQueryType(this, getString(R.string.pref_category_favourites));
                getLoaderManager().restartLoader(FAVOURITES_LOADER_ID, null, favouriteMoviesLoaderListener);
                mCurrentLoaderId = FAVOURITES_LOADER_ID;
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Every time we click a movie poster, we create an intent and pass a Movie object along with it,
    // so we can display all the information that we received about it, without heaving to fetch
    // more data from movie API server
    @Override
    public void onGridItemClick(Movie movieClicked, int movieId) {
        if (movieClicked != null && movieId == 0) {
            Intent movieDetailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
            movieDetailsIntent.putExtra(DetailsActivity.MOVIE_OBJECT, movieClicked);
            startActivity(movieDetailsIntent);
        } else {
            Intent movieDetailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
            movieDetailsIntent.putExtra(DetailsActivity.MOVIE_ID, movieId);
            startActivity(movieDetailsIntent);
        }
    }

    // TODO: savedInstanteState, favourites and share info

    // Hide the movie data and loading indicator and show error message
    private void showError() {
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mCloudImageView.setVisibility(View.VISIBLE);
        mMessagesTextView.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
    }

    private LoaderManager.LoaderCallbacks<ArrayList<Movie>> moviesLoaderListener =
            new LoaderManager.LoaderCallbacks<ArrayList<Movie>>() {
                @Override
                public Loader<ArrayList<Movie>> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case MOVIES_LOADER_ID:
                            // If the loaded id matches ours, return a new movie loader
                            Log.v ("MOVIE LOADER", "CURRENT LOADER: " + String.valueOf(mCurrentLoaderId));
                            return new MovieLoader(getApplicationContext());
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> movies) {
                    // Every time we get new results we have 2 possibilities
                    int currentPage = MoviePreferences.getLastPageNumber(getApplicationContext());
                    // If currentPage is "0" or "1", we know that the user has changed the movie category or uses the
                    // app for the first time. In this situation we swap the Movie array with the new data
                    if (currentPage == 0 || currentPage == 1) {
                        mAdapter.swapMovies(movies);
                    } else {
                        // Otherwise, we add the new data to the old data, creating an infinite scrolling effect
                        mAdapter.addMovies(movies);
                    }

                    // If the RecyclerView has no position, we assume the first position in the list
                    // and set the RecyclerView at the beginning of results
                    if (mPosition == RecyclerView.NO_POSITION) {
                        mPosition = 0;
                        mMoviesRecyclerView.smoothScrollToPosition(mPosition);
                    }

                    // If new data is available, hide progress bar
                    if (movies != null && movies.size() != 0) mLoading.setVisibility(View.GONE);
                }

                @Override
                public void onLoaderReset(Loader<ArrayList<Movie>> loader) {
                    // If the loader is reset, swap old data with null ones
                    mAdapter.swapMovies(new ArrayList<Movie>() {
                    });
                }
            };

    private LoaderManager.LoaderCallbacks<Cursor> favouriteMoviesLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

                    Uri uri = MovieDetailsEntry.CONTENT_URI;

                    Log.v("URI", uri.toString());

                    switch (loaderId) {
                        case FAVOURITES_LOADER_ID:
                            return new CursorLoader(
                                    getApplicationContext(),
                                    uri,
                                    FAVOURITES_MOVIE_PROJECTION,
                                    null,
                                    null,
                                    null);

                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor moviesCursor) {
                    //mMoviesCursor = moviesCursor;
                    // mMovies = null;
                    mAdapter.swapCursor(moviesCursor);

                    // If the RecyclerView has no position, we assume the first position in the list
                    //if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                    // Scroll the RecyclerView to mPosition
                    //mMoviesRecyclerView.smoothScrollToPosition(mPosition);

                    // If new data is available, hide progress bar
                    if (moviesCursor.getCount() != 0) mLoading.setVisibility(View.GONE);
                }

                @Override
                public void onLoaderReset(@NonNull Loader<Cursor> loader) {
                    mAdapter.swapCursor(null);
                }
            };

    private void populateMovies(ArrayList<Movie> movies) {
        // Every time we get new results we have 2 possibilities
        int currentPage = MoviePreferences.getLastPageNumber(getApplicationContext());
        // If currentPage is "0" or "1", we know that the user has changed the movie category or uses the
        // app for the first time. In this situation we swap the Movie array with the new data
        if (currentPage == 0 || currentPage == 1) {
            mAdapter = new MovieAdapter(this, new ArrayList<Movie>() {
            }, this);
            mMoviesRecyclerView.setAdapter(mAdapter);
            mAdapter.swapMovies(movies);
        } else {
            // Otherwise, we add the new data to the old data, creating an infinite scrolling effect
            mAdapter.addMovies(movies);
        }

        // If the RecyclerView has no position, we assume the first position in the list
        // and set the RecyclerView at the beginning of results
        if (mPosition == RecyclerView.NO_POSITION) {
            mPosition = 0;
            mMoviesRecyclerView.smoothScrollToPosition(mPosition);
        }

        // If new data is available, hide progress bar
        if (movies.size() != 0) mLoading.setVisibility(View.GONE);
    }
}