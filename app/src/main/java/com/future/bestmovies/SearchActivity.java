package com.future.bestmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.future.bestmovies.movie.MoviePreferences;
import com.future.bestmovies.search.SearchLoader;
import com.future.bestmovies.search.SearchResultsAdapter;
import com.future.bestmovies.search.SearchResult;
import com.future.bestmovies.utils.NetworkUtils;

import java.util.ArrayList;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<SearchResult>>, SearchResultsAdapter.ListItemClickListener {

    private static final int SEARCH_LOADER_ID = 231;
    public static final String SEARCH_QUERY_KEY = "search_query";
    private static final String RESULT_TYPE_MOVIE = "movie";
    private static final String RESULT_TYPE_PERSON = "person";

    @BindView(R.id.search_results_rv)
    RecyclerView mResultsRecyclerView;
    @BindView(R.id.results_messages_tv)
    TextView mResultsMessagesTextView;
    @BindView(R.id.no_result_iv)
    ImageView mNoResultsImageView;      // No results and no connection
    @BindView(R.id.loading_results_pb)
    ProgressBar mResultsProgressBar;
    private SearchResultsAdapter mSearchResultsAdapter;
    private LinearLayoutManager mResultsLayoutManager;
    private int mResultsPosition = RecyclerView.NO_POSITION;

    // Resources
    @BindString(R.string.no_connection)
    String noConnection;
    @BindString(R.string.no_poster)
    String noPoster;
    @BindString(R.string.loading)
    String loadingMsg;
    @BindString(R.string.no_search_results)
    String errorMsg;

    private String mSearchQuery;
    private Toast mToast;

    // Infinite scrolling variables
    private boolean isScrolling = false;
    private int visibleItems;
    private int totalItems;
    private int scrolledUpItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mResultsProgressBar.setVisibility(View.VISIBLE);
        mResultsLayoutManager = new LinearLayoutManager(this);
        mResultsRecyclerView.setLayoutManager(mResultsLayoutManager);
        mResultsRecyclerView.setHasFixedSize(true);
        mSearchResultsAdapter = new SearchResultsAdapter(this, this);
        mResultsRecyclerView.setAdapter(mSearchResultsAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mResultsRecyclerView.getContext(),
                mResultsLayoutManager.getOrientation());
        mResultsRecyclerView.addItemDecoration(dividerItemDecoration);

        // Every time we create this activity we set the page number of our results to be 0
        MoviePreferences.setLastSearchPageNumber(this, 0);

        if (savedInstanceState == null) {
            // Check intent and see if there is a search query passed from MainActivity,
            // so we can populate our UI. If there isn't we close this activity
            // and display a toast message.
            Intent intent = getIntent();
            if (intent != null) {
                // If MainActivity passed a query string
                if (intent.hasExtra(SEARCH_QUERY_KEY)) {
                    // Save the passed query string
                    mSearchQuery = intent.getStringExtra(SEARCH_QUERY_KEY);
                    // Fetch results
                    fetchResults(this);
                } else {
                    closeOnError(errorMsg);
                }
            } else {
                closeOnError(errorMsg);
            }
        }

        // To create an infinite scrolling effect, we add an OnScrollListener to our RecyclerView
        mResultsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                visibleItems = mResultsLayoutManager.getChildCount();
                totalItems = mResultsLayoutManager.getItemCount();
                scrolledUpItems = mResultsLayoutManager.findFirstVisibleItemPosition();

                // We set a threshold, to help us know that the user is about to get to the end of
                // the list.
                int threshold = 5;

                // If the user is still scrolling and the the Threshold is bigger or equal with the
                // totalItems - visibleItems - scrolledUpItems, we know we have to load new Movies
                if (isScrolling && (threshold >= totalItems - visibleItems - scrolledUpItems)) {
                    isScrolling = false;
                    Log.v("SearchResults", "Load new results!");
                    loadNewResults();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Search query
        outState.putString(SEARCH_QUERY_KEY, mSearchQuery);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(SEARCH_QUERY_KEY)) {
            mSearchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY);
            if (!TextUtils.isEmpty(mSearchQuery))
                fetchResults(this);
        }
    }

    private void closeOnError(String message) {
        finish();
        toastThis(message);
    }

    private void toastThis(String toastMessage) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void loadNewResults() {
        mResultsProgressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchResults(getApplicationContext());
                mResultsProgressBar.setVisibility(View.GONE);
            }
        }, 1000);
    }

    // Fetch data or show connection error
    private void fetchResults(Context context) {
        // If there is a network connection, fetch data
        if (NetworkUtils.isConnected(context)) {
            showResults();

            // Before we fetch data, we need the last page number that was loaded in our RecyclerView,
            // increment it by 1 and save it in a preference for next data fetching
            int nextPage = MoviePreferences.getLastSearchPageNumber(context) + 1;
            MoviePreferences.setLastSearchPageNumber(context, nextPage);
            //Init or restart loader
            getSupportLoaderManager().restartLoader(SEARCH_LOADER_ID, null, this);
        } else {
            // If no connection, hide loading indicator, hide data and display connection error message
            showError();

            // Update message TextView with no connection error message
            mResultsMessagesTextView.setText(R.string.no_internet);

            // Every time we have a connection error, we set the page number of our results to be 0
            MoviePreferences.setLastSearchPageNumber(this, 0);
        }
    }

    // Hide the progress bar and show results
    private void showResults() {
        mResultsRecyclerView.setVisibility(View.VISIBLE);
        mResultsProgressBar.setVisibility(View.INVISIBLE);
        mResultsMessagesTextView.setVisibility(View.INVISIBLE);
        mNoResultsImageView.setVisibility(View.INVISIBLE);
    }

    // Hide progress bar and show no results message
    private void noResults() {
        mResultsRecyclerView.setVisibility(View.GONE);
        mResultsMessagesTextView.setVisibility(View.VISIBLE);
        mResultsMessagesTextView.setText(errorMsg);
        mResultsProgressBar.setVisibility(View.INVISIBLE);
        mNoResultsImageView.setVisibility(View.VISIBLE);
    }

    // Hide the results recycler view and loading indicator and show error message
    private void showError() {
        mResultsRecyclerView.setVisibility(View.INVISIBLE);
        mNoResultsImageView.setVisibility(View.VISIBLE);
        mNoResultsImageView.setImageResource(R.drawable.ic_cloud_off);
        mResultsMessagesTextView.setVisibility(View.VISIBLE);
        mResultsMessagesTextView.setText(noConnection);
        mResultsProgressBar.setVisibility(View.INVISIBLE);
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

    @Override
    public void onListItemClick(SearchResult resultClicked) {
        switch (resultClicked.getType()) {
            case RESULT_TYPE_MOVIE:
                Intent movieDetailsIntent = new Intent(SearchActivity.this, DetailsActivity.class);
                movieDetailsIntent.putExtra(DetailsActivity.MOVIE_ID_KEY, resultClicked.getId());
                movieDetailsIntent.putExtra(DetailsActivity.MOVIE_TITLE_KEY, resultClicked.getMovieTitle());
                startActivity(movieDetailsIntent);
                break;

            case RESULT_TYPE_PERSON:
                Intent actorProfileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
                actorProfileIntent.putExtra(DetailsActivity.ACTOR_ID_KEY, resultClicked.getId());
                actorProfileIntent.putExtra(DetailsActivity.ACTOR_NAME_KEY, resultClicked.getPersonName());
                actorProfileIntent.putExtra(DetailsActivity.MOVIE_BACKDROP_KEY, resultClicked.getProfilePath());
                startActivity(actorProfileIntent);
                break;
        }
    }

    @NonNull
    @Override
    public Loader<ArrayList<SearchResult>> onCreateLoader(int loaderId, @Nullable Bundle args) {
        switch (loaderId) {
            case SEARCH_LOADER_ID:
                // If the loader id matches ours, return a new search loader
                return new SearchLoader(getApplicationContext(), mSearchQuery);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<SearchResult>> loader, ArrayList<SearchResult> data) {
        // Every time we get new results we have 2 possibilities
        int currentPage = MoviePreferences.getLastSearchPageNumber(getApplicationContext());
        Log.v("PAGE", String.valueOf(currentPage));

        // If currentPage is "1", we know that the user has changed the search query.
        // In this situation we swap the results array with the new data
        if (currentPage == 1) {
            if (data != null && data.size() > 0)
                mSearchResultsAdapter.swapResults(data);
            else
                noResults();
        } else {
            // Otherwise, we add the new data to the old data, creating an infinite scrolling effect
            if (currentPage > 1)
                mSearchResultsAdapter.addResults(data);
        }

        // If the RecyclerView has no position, we assume the first position in the list
        if (mResultsPosition == RecyclerView.NO_POSITION) {
            mResultsPosition = 0;
            mResultsRecyclerView.smoothScrollToPosition(mResultsPosition);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<SearchResult>> loader) {

    }
}
