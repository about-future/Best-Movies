package com.future.bestmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.future.bestmovies.reviews.Review;
import com.future.bestmovies.reviews.ReviewAdapter;
import com.future.bestmovies.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.future.bestmovies.DetailsActivity.MOVIE_BACKDROP_KEY;
import static com.future.bestmovies.DetailsActivity.MOVIE_REVIEWS_KEY;
import static com.future.bestmovies.DetailsActivity.MOVIE_TITLE_KEY;

public class ReviewsActivity extends AppCompatActivity {

    private static final String REVIEWS_POSITION_KEY = "reviews_position";
    private int mReviewPosition = RecyclerView.NO_POSITION;

    @BindView(R.id.reviews_rv)
    RecyclerView mReviewsRecyclerView;
    @BindView(R.id.reviews_backdrop_iv)
    ImageView movieBackdropImageView;
    @BindView(R.id.reviews_toolbar)
    Toolbar toolbar;

    private ArrayList<Review> mReviews;
    ReviewAdapter mReviewAdapter;
    private LinearLayoutManager mReviewLayoutManager;
    private String mMovieTitle;
    private String mMovieBackdrop;
    private Bundle mBundleState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        ButterKnife.bind(this);

        // We initialize and set the toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mReviewLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mReviewsRecyclerView.setLayoutManager(mReviewLayoutManager);
        mReviewsRecyclerView.setHasFixedSize(false);
        mReviewAdapter = new ReviewAdapter(this);
        mReviewsRecyclerView.setAdapter(mReviewAdapter);

        if (savedInstanceState == null) {
            // Check our intent and see if there is an ArrayList of Review objects passed from
            // DetailsActivity, so we can populate our reviews UI. If there isn't, we close this
            // activity and display a toast message.
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(MOVIE_REVIEWS_KEY)) {
                mReviews = intent.getParcelableArrayListExtra(MOVIE_REVIEWS_KEY);
                mReviewAdapter.swapReviews(mReviews);

                if (intent.hasExtra(MOVIE_TITLE_KEY)) {
                    mMovieTitle = intent.getStringExtra(MOVIE_TITLE_KEY);
                    // Set activity title as the movie name
                    setTitle(mMovieTitle);
                }
                if (intent.hasExtra(MOVIE_BACKDROP_KEY)) {
                    mMovieBackdrop = intent.getStringExtra(MOVIE_BACKDROP_KEY);
                    // Set the backdrop for the movie reviews
                    Picasso.get()
                            .load(ImageUtils.buildImageUrl(
                                    this,
                                    mMovieBackdrop,
                                    ImageUtils.BACKDROP))
                            .into(movieBackdropImageView);
                }
            } else {
                closeOnError();
            }
        }

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
        outState.putParcelableArrayList(MOVIE_REVIEWS_KEY, mReviews);
        if (mReviewLayoutManager.findFirstCompletelyVisibleItemPosition() != RecyclerView.NO_POSITION) {
            outState.putInt(REVIEWS_POSITION_KEY, mReviewLayoutManager.findFirstCompletelyVisibleItemPosition());
        } else {
            outState.putInt(REVIEWS_POSITION_KEY, mReviewLayoutManager.findFirstVisibleItemPosition());
        }
        outState.putString(MOVIE_TITLE_KEY, mMovieTitle);
        outState.putString(MOVIE_BACKDROP_KEY, mMovieBackdrop);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(MOVIE_REVIEWS_KEY)) {
            mReviews = savedInstanceState.getParcelableArrayList(MOVIE_REVIEWS_KEY);
            mReviewAdapter.swapReviews(mReviews);
        }

        // Restore previous position
        if (savedInstanceState.containsKey(REVIEWS_POSITION_KEY)) {
            mReviewPosition = savedInstanceState.getInt(REVIEWS_POSITION_KEY);
        }

        // Restore activity title as the movie name
        if (savedInstanceState.containsKey(MOVIE_TITLE_KEY)) {
            mMovieTitle = savedInstanceState.getString(MOVIE_TITLE_KEY);
            setTitle(mMovieTitle);
        }

        // Restore the backdrop for the movie reviews
        if (savedInstanceState.containsKey(MOVIE_BACKDROP_KEY)) {
            mMovieBackdrop = savedInstanceState.getString(MOVIE_BACKDROP_KEY);
            Picasso.get()
                    .load(ImageUtils.buildImageUrl(
                            this,
                            mMovieBackdrop,
                            ImageUtils.BACKDROP))
                    .error(R.drawable.ic_landscape)
                    .into(movieBackdropImageView);
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBundleState = new Bundle();

        // Save Reviews position
        if (mReviewLayoutManager.findFirstCompletelyVisibleItemPosition() != RecyclerView.NO_POSITION) {
            mBundleState.putInt(REVIEWS_POSITION_KEY, mReviewLayoutManager.findFirstCompletelyVisibleItemPosition());
        } else {
            mBundleState.putInt(REVIEWS_POSITION_KEY, mReviewLayoutManager.findFirstVisibleItemPosition());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restore RecyclerView position
        if (mBundleState != null) {
            mReviewPosition = mBundleState.getInt(REVIEWS_POSITION_KEY);
            if (mReviewPosition == RecyclerView.NO_POSITION) {
                mReviewPosition = 0;
            }
            mReviewsRecyclerView.scrollToPosition(mReviewPosition);
        }
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.no_reviews, Toast.LENGTH_SHORT).show();
    }
}
