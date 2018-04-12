package com.future.bestmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import com.future.bestmovies.movie.Movie;
import com.future.bestmovies.movie.MovieCategoryAdapter;
import com.future.bestmovies.movie.MovieCategoryLoader;
import com.future.bestmovies.movie.MoviePreferences;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.future.bestmovies.utils.ScreenUtils;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.future.bestmovies.data.FavouritesContract.*;


public class MainActivity extends AppCompatActivity implements
        MovieCategoryAdapter.GridItemClickListener, LoaderManager.LoaderCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TITLE_KEY = "title";

    // Movie categories
    private static final String CATEGORY_POPULAR = "popular";
    private static final String CATEGORY_TOP_RATED = "top_rated";
    private static final String CATEGORY_UPCOMING = "upcoming";
    private static final String CATEGORY_NOW_PLAYING = "now_playing";
    private static final String CATEGORY_FAVOURITES = "favourites";

    // Loaders
    private static final int MOVIES_LOADER_ID = 24;
    private static final int FAVOURITES_LOADER_ID = 516;
    private static final String CURRENT_LOADED_ID = "loader_id";
    private int mCurrentLoaderId;

    // Cursor projection
    public static final String[] FAVOURITES_MOVIE_PROJECTION = {
            MovieDetailsEntry.COLUMN_MOVIE_ID,
            MovieDetailsEntry.COLUMN_POSTER_PATH
    };

    @BindView(R.id.movies_rv)
    RecyclerView mMoviesRecyclerView;
    @BindView(R.id.messages_tv)
    TextView mMessagesTextView;
    @BindView(R.id.no_connection_cloud_iv)
    ImageView mNoConnectionImageView;
    @BindView(R.id.loading_pb)
    ProgressBar mLoading;
    private MovieCategoryAdapter mAdapter;
    private GridLayoutManager mGridLayoutManager;
    private int mPosition = RecyclerView.NO_POSITION;

    // Infinite scrolling variables
    private boolean isScrolling = false;
    private int visibleItems;
    private int totalItems;
    private int scrolledUpItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        Picasso.Builder builder = new Picasso.Builder(this);
//        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
//        Picasso picasso = builder.build();
//        picasso.setIndicatorsEnabled(true);
//        Picasso.setSingletonInstance(picasso);

        mLoading.setVisibility(View.VISIBLE);
        // The layout manager for our RecyclerView will be a GridLayout, so we can display our movies
        // on columns. The number of columns is dictated by the orientation and size of the device
        mGridLayoutManager = new GridLayoutManager(
                this,
                ScreenUtils.getNumberOfColumns(this, 200, 2));
        mMoviesRecyclerView.setLayoutManager(mGridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(true);
        mAdapter = new MovieCategoryAdapter(this, this);
        mMoviesRecyclerView.setAdapter(mAdapter);

        // Check if preference "image_width" was create before, if not, proceed.
        if (!MoviePreferences.isImageWidthAvailable(this)) {
            // Create an image width preference for our RecyclerView
            // This preference is very useful to our RecyclerView, so we can load all the images
            // into it heaving the same width. The image width will be perfect(or almost perfect)
            // for the device we are using. We measure once and use it as many times we want.
            MoviePreferences.setImageWidthForRecyclerView(
                    this,
                    ImageUtils.getImageWidth(this, ImageUtils.POSTER));
        }

        // Every time we create this activity we set the page number of our results to be 0
        MoviePreferences.setLastPageNumber(this, 0);


        if (savedInstanceState == null) {
//            if (MoviePreferences.getPreferredQueryType(this) == null) {
//            }

            // Set current loader id and title, depending on preferred "query type" (aka: "movie category" or "sort order")
            switch (MoviePreferences.getPreferredQueryType(this)) {
                case CATEGORY_TOP_RATED:
                    mCurrentLoaderId = MOVIES_LOADER_ID;
                    setTitle(R.string.menu_top_rated);
                    break;
                case CATEGORY_UPCOMING:
                    mCurrentLoaderId = MOVIES_LOADER_ID;
                    setTitle(R.string.menu_upcoming);
                    break;
                case CATEGORY_NOW_PLAYING:
                    mCurrentLoaderId = MOVIES_LOADER_ID;
                    setTitle(R.string.menu_now_playing);
                    break;
                case CATEGORY_FAVOURITES:
                    mCurrentLoaderId = FAVOURITES_LOADER_ID;
                    setTitle(R.string.menu_favourites);
                    break;
                default:
                    // If we run the app for the first time, no movie category was set as query
                    // preference and no loader was initialized
                    mCurrentLoaderId = MOVIES_LOADER_ID;
                    setTitle(R.string.menu_popular);
            }

            // Fetch movie data, using the selected loader id
            fetchMovies(this);
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
                if (!TextUtils.equals(MoviePreferences.getPreferredQueryType(getApplicationContext()), CATEGORY_FAVOURITES)) {
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
                        loadNewMovies();
                    }
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_LOADED_ID, mCurrentLoaderId);
        outState.putString(TITLE_KEY, getTitle().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CURRENT_LOADED_ID)) {
                mCurrentLoaderId = savedInstanceState.getInt(CURRENT_LOADED_ID);
                fetchMovies(this);
            }
            if (savedInstanceState.containsKey(TITLE_KEY))
                setTitle(savedInstanceState.getString(TITLE_KEY));
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

    // Fetch data or show connection error
    private void fetchMovies(Context context) {
        // If there is a network connection, fetch data
        if (NetworkUtils.isConnected(context) && mCurrentLoaderId != FAVOURITES_LOADER_ID) {
            showMovies();

            // Before we fetch data, we need the last page number that was loaded in our RecyclerView,
            // increment it by 1 and save it in a preference for next data fetching
            int nextPage = MoviePreferences.getLastPageNumber(context) + 1;
            MoviePreferences.setLastPageNumber(context, nextPage);
            //Init or restart loader
            getSupportLoaderManager().restartLoader(mCurrentLoaderId, null, this);
        }
        // If selected loader id is FAVOURITES_LOADER_ID, we don't need internet connection, so we
        // showMovies and start favourites loader
        else if (mCurrentLoaderId == FAVOURITES_LOADER_ID) {
            showMovies();

            //Init or restart loader
            getSupportLoaderManager().restartLoader(mCurrentLoaderId, null, this);
        }
        // If no connection and the loader id is not FAVOURITES_LOADER_ID
        else {
            // Hide loading indicator, hide data and display connection error message
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem selectedItem;
        switch (MoviePreferences.getPreferredQueryType(this)) {
            case CATEGORY_POPULAR:
                selectedItem = menu.findItem(R.id.action_popular);
                selectedItem.setIcon(R.drawable.ic_check);
                break;
            case CATEGORY_TOP_RATED:
                selectedItem = menu.findItem(R.id.action_top_rated);
                selectedItem.setIcon(R.drawable.ic_check);
                break;
            case CATEGORY_UPCOMING:
                selectedItem = menu.findItem(R.id.action_upcoming);
                selectedItem.setIcon(R.drawable.ic_check);
                break;
            case CATEGORY_NOW_PLAYING:
                selectedItem = menu.findItem(R.id.action_now_playing);
                selectedItem.setIcon(R.drawable.ic_check);
                break;
            case CATEGORY_FAVOURITES:
                selectedItem = menu.findItem(R.id.action_favourites);
                selectedItem.setIcon(R.drawable.ic_check);
                break;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_popular:
                MoviePreferences.setPreferredQueryType(this, getString(R.string.category_popular));
                MoviePreferences.setLastPageNumber(this, 0);
                getSupportLoaderManager().destroyLoader(FAVOURITES_LOADER_ID);
                mCurrentLoaderId = MOVIES_LOADER_ID;
                fetchMovies(getApplicationContext());
                setTitle(item.getTitle());
                invalidateOptionsMenu();
                break;

            case R.id.action_top_rated:
                MoviePreferences.setPreferredQueryType(this, getString(R.string.category_top_rated));
                MoviePreferences.setLastPageNumber(this, 0);
                getSupportLoaderManager().destroyLoader(FAVOURITES_LOADER_ID);
                mCurrentLoaderId = MOVIES_LOADER_ID;
                fetchMovies(getApplicationContext());
                setTitle(item.getTitle());
                invalidateOptionsMenu();
                break;

            case R.id.action_upcoming:
                MoviePreferences.setPreferredQueryType(this, getString(R.string.category_upcoming));
                MoviePreferences.setLastPageNumber(this, 0);
                getSupportLoaderManager().destroyLoader(FAVOURITES_LOADER_ID);
                mCurrentLoaderId = MOVIES_LOADER_ID;
                fetchMovies(getApplicationContext());
                setTitle(item.getTitle());
                invalidateOptionsMenu();
                break;

            case R.id.action_now_playing:
                MoviePreferences.setPreferredQueryType(this, getString(R.string.category_now_playing));
                MoviePreferences.setLastPageNumber(this, 0);
                getSupportLoaderManager().destroyLoader(FAVOURITES_LOADER_ID);
                mCurrentLoaderId = MOVIES_LOADER_ID;
                fetchMovies(getApplicationContext());
                setTitle(item.getTitle());
                invalidateOptionsMenu();
                break;

            case R.id.action_favourites:
                MoviePreferences.setPreferredQueryType(this, getString(R.string.category_favourites));
                getSupportLoaderManager().destroyLoader(MOVIES_LOADER_ID);
                mCurrentLoaderId = FAVOURITES_LOADER_ID;
                //getSupportLoaderManager().restartLoader(mCurrentLoaderId, null, this);
                fetchMovies(getApplicationContext());
                setTitle(item.getTitle());
                invalidateOptionsMenu();
                break;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
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
            movieDetailsIntent.putExtra(DetailsActivity.MOVIE_ID_KEY, movieClicked.getMovieId());
            movieDetailsIntent.putExtra(DetailsActivity.MOVIE_TITLE_KEY, movieClicked.getTitle());
            startActivity(movieDetailsIntent);
        } else {
            Intent movieDetailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
            movieDetailsIntent.putExtra(DetailsActivity.MOVIE_ID_KEY, movieId);
            startActivity(movieDetailsIntent);
        }
    }

    // Show movie data and hide no connection icon and message
    private void showMovies() {
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
        mNoConnectionImageView.setVisibility(View.INVISIBLE);
        mNoConnectionImageView.setImageResource(R.drawable.ic_cloud_off);
        mMessagesTextView.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }

    // Hide the movie data and loading indicator and show error message
    private void showError() {
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mNoConnectionImageView.setVisibility(View.VISIBLE);
        mNoConnectionImageView.setImageResource(R.drawable.ic_cloud_off);
        mMessagesTextView.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int loaderId, @Nullable Bundle args) {
        switch (loaderId) {
            case MOVIES_LOADER_ID:
                // If the loaded id matches movies loader, return a new movie category loader
                return new MovieCategoryLoader(getApplicationContext());

            case FAVOURITES_LOADER_ID:
                // If the loader id matches favourites loader, return a cursor loader
                return new CursorLoader(
                        getApplicationContext(),
                        MovieDetailsEntry.CONTENT_URI,
                        FAVOURITES_MOVIE_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        if (data != null) {
            mLoading.setVisibility(View.GONE);
        }

        switch (loader.getId()) {
            case MOVIES_LOADER_ID:
                mNoConnectionImageView.setVisibility(View.INVISIBLE);
                mMessagesTextView.setVisibility(View.INVISIBLE);

                // Every time we get new results we have 2 possibilities
                int currentPage = MoviePreferences.getLastPageNumber(getApplicationContext());
                // If currentPage is "1", we know that the user has changed the movie category or uses the
                // app for the first time. In this situation we swap the Movie array with the new data
                if (currentPage == 1) {
                    mAdapter.swapMovies((ArrayList<Movie>) data);
                } else {
                    // Otherwise, we add the new data to the old data, creating an infinite scrolling effect
                    if (currentPage > 1)
                        mAdapter.addMovies((ArrayList<Movie>) data);
                }

                // If the RecyclerView has no position, we assume the first position in the list
                // and set the RecyclerView at the beginning of results
                if (mPosition == RecyclerView.NO_POSITION) {
                    mPosition = 0;
                    mMoviesRecyclerView.smoothScrollToPosition(mPosition);
                }

                break;

            case FAVOURITES_LOADER_ID:
                mAdapter.swapCursor((Cursor) data);

                if (data != null && ((Cursor) data).getCount() == 0) {
                    mNoConnectionImageView.setVisibility(View.VISIBLE);
                    mNoConnectionImageView.setImageResource(R.drawable.ic_favorite);
                    mMessagesTextView.setVisibility(View.VISIBLE);
                    mMessagesTextView.setText(R.string.no_favourites);
                } else {
                    mNoConnectionImageView.setVisibility(View.INVISIBLE);
                    mMessagesTextView.setVisibility(View.INVISIBLE);
                    // If the RecyclerView has no position, we assume the first position in the list
                    if (mPosition == RecyclerView.NO_POSITION) {
                        mPosition = 0;
                        // Scroll the RecyclerView to mPosition
                        mMoviesRecyclerView.smoothScrollToPosition(mPosition);
                    }
                }

                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        switch (loader.getId()) {
            case FAVOURITES_LOADER_ID:
                mAdapter.swapCursor(null);
                break;
            default:
                break;
        }
    }
}