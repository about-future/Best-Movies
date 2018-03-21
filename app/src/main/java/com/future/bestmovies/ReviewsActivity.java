package com.future.bestmovies;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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

import static com.future.bestmovies.DetailsActivity.MOVIE_REVIEWS;

public class ReviewsActivity extends AppCompatActivity {

    private static final String REVIEWS_POSITION = "reviews_position";
    private int mReviewPosition = RecyclerView.NO_POSITION;
    private ArrayList<Review> mReviews;
    private ReviewAdapter mReviewAdapter;
    private RecyclerView mReviewsRecyclerView;
    private LinearLayoutManager mReviewLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mReviewsRecyclerView = findViewById(R.id.reviews_rv);
        // The layout manager for our reviews RecyclerView will be a LinerLayout, so we can display
        // movie reviews on a single column
        mReviewLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mReviewsRecyclerView.setLayoutManager(mReviewLayoutManager);
        mReviewsRecyclerView.setHasFixedSize(true);

        // If we have an instance saved and contains our movie id, we use it to populate our UI
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_REVIEWS)) {
            mReviews = savedInstanceState.getParcelableArrayList(MOVIE_REVIEWS);
            if (savedInstanceState.containsKey(REVIEWS_POSITION)) {
                mReviewPosition = savedInstanceState.getInt(REVIEWS_POSITION);
            }
        } else {
            // Otherwise, we check our intent and see if there is an ArrayList of Review objects
            // passed from DetailsActivity, so we can populate our reviews UI. If there isn't, we
            // close this activity and display a toast message.
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(MOVIE_REVIEWS)) {
                mReviews = intent.getParcelableArrayListExtra(MOVIE_REVIEWS);
            } else {
                closeOnError();
            }
        }

        // If mReviews contains no data, we close this activity and display a toast message
        if (mReviews.size() == 0) {
            closeOnError();
            return;
        }

        mReviewAdapter = new ReviewAdapter(this, mReviews);
        mReviewsRecyclerView.setAdapter(mReviewAdapter);

        if (mReviewPosition == RecyclerView.NO_POSITION) {
            mReviewPosition = 0;
        }

        mReviewsRecyclerView.scrollToPosition(mReviewPosition);
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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_REVIEWS, mReviews);
        if (mReviewLayoutManager.findFirstCompletelyVisibleItemPosition() != RecyclerView.NO_POSITION) {
            outState.putInt(REVIEWS_POSITION, mReviewLayoutManager.findFirstCompletelyVisibleItemPosition());
        } else {
            outState.putInt(REVIEWS_POSITION, mReviewLayoutManager.findFirstVisibleItemPosition());
        }

        super.onSaveInstanceState(outState);
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.no_reviews, Toast.LENGTH_SHORT).show();
    }
}
