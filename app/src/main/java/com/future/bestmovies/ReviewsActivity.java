package com.future.bestmovies;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.data.Review;
import com.future.bestmovies.data.ReviewAdapter;
import com.future.bestmovies.data.ReviewLoader;

import java.util.ArrayList;

public class ReviewsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Review>> {
    private static final int REVIEWS_LOADER_ID = 4356;
    public static final String MOVIE_ID = "movie_id";

    private int mReviewPosition = RecyclerView.NO_POSITION;
    private String mSelectedMovieId;
    private ReviewAdapter mReviewAdapter;
    private RecyclerView mReviewsRecyclerView;
    private ProgressBar mReviewsProgressBar;
    private TextView mReviewsMessagesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // If we have an instance saved and contains our movie id, we use it to populate our UI
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_ID)) {
            mSelectedMovieId = savedInstanceState.getString(MOVIE_ID);
        } else {
            // Otherwise, we check our intent and see if there is a Movie object passed from
            // MainActivity, so we can populate our UI. If there isn't we close this activity and
            // display a toast message.
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(MOVIE_ID)) {
                mSelectedMovieId = intent.getStringExtra(MOVIE_ID);
            } else {
                closeOnError();
            }
        }

        // If the mSelectedMovieId contains no data, we close this activity and display a toast message
        if (TextUtils.isEmpty(mSelectedMovieId)) {
            // Movie id unavailable
            closeOnError();
            return;
        }

        mReviewAdapter = new ReviewAdapter(this, new ArrayList<Review>());
        mReviewsRecyclerView = findViewById(R.id.reviews_rv);
        mReviewsProgressBar = findViewById(R.id.loading_reviews_pb);
        // The layout manager for our Cast RecyclerView will be a LinerLayout, so we can display
        // our cast on a single line, horizontally
        LinearLayoutManager reviewLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mReviewsRecyclerView.setLayoutManager(reviewLayoutManager);
        mReviewsRecyclerView.setHasFixedSize(true);
        mReviewsRecyclerView.setAdapter(mReviewAdapter);
        mReviewsMessagesTextView = findViewById(R.id.reviews_messages_tv);
        mReviewsMessagesTextView.setText(R.string.loading);

        getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, this);
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

    // Hide the progress bar and show reviews
    private void showReviews() {
        mReviewsRecyclerView.setVisibility(View.VISIBLE);
        mReviewsProgressBar.setVisibility(View.INVISIBLE);
        mReviewsMessagesTextView.setVisibility(View.INVISIBLE);
    }

    // Show progress bar and hide reviews
    private void hideReviews() {
        mReviewsRecyclerView.setVisibility(View.INVISIBLE);
        mReviewsProgressBar.setVisibility(View.VISIBLE);
        mReviewsMessagesTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<ArrayList<Review>> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case REVIEWS_LOADER_ID:
                // If the loaded id matches ours, return a new movie review loader
                return new ReviewLoader(getApplicationContext(), mSelectedMovieId);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Review>> loader, ArrayList<Review> movieReviews) {
        mReviewAdapter.swapReviews(movieReviews);

        // If our RecyclerView has is not position, we assume the first position in the list
        // and set the RecyclerView a the beginning of our results
        if (mReviewPosition == RecyclerView.NO_POSITION) {
            mReviewPosition = 0;
            mReviewsRecyclerView.smoothScrollToPosition(mReviewPosition);
        }

        //If the movieReviews has data
        if (movieReviews.size() != 0) {
            // Show movie reviews
            showReviews();
        } else {
            // Otherwise, hide progress bar and show "No reviews available" message
            mReviewsMessagesTextView.setVisibility(View.VISIBLE);
            mReviewsMessagesTextView.setText(R.string.no_reviews);
            mReviewsProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Review>> loader) {
        mReviewAdapter.swapReviews(new ArrayList<Review>());
    }
}
