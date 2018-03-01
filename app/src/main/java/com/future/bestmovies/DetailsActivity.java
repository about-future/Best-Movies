package com.future.bestmovies;

import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.MovieUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;

public class DetailsActivity extends AppCompatActivity {
    public static final String MOVIE_OBJECT = "movie";
    private Movie mSelectedMovie;
    private ConstraintLayout mMovieDetailsLayout;
    private TextView mMessagesTextView;
    private ImageView mCloudImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMovieDetailsLayout = findViewById(R.id.movie_details_layout);
        mCloudImageView = findViewById(R.id.no_connection_cloud_iv);
        mMessagesTextView = findViewById(R.id.messages_tv);

        // If there is a network connection, fetch data
        if(NetworkUtils.isConnected(this)){
            showMovieDetails();
        } else {
            // Otherwise, hide data and display connection error message
            showError();
            // Update message TextView with no connection error message
            mMessagesTextView.setText(R.string.no_internet);
        }

        ImageView movieBackdropImageView = findViewById(R.id.details_backdrop_iv);
        TextView movieTitleTextView = findViewById(R.id.details_title_tv);
        ImageView moviePosterImageView = findViewById(R.id.details_poster_iv);
        TextView moviePlotTextView = findViewById(R.id.details_plot_tv);
        TextView movieRatingTextView = findViewById(R.id.details_ratings_tv);
        TextView movieReleaseDateTextView = findViewById(R.id.details_release_date_tv);
        TextView movieGenreTextView = findViewById(R.id.details_genre_tv);

        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_OBJECT)) {
            mSelectedMovie = savedInstanceState.getParcelable(MOVIE_OBJECT);
        } else {
            Intent intent = getIntent();
            if (intent == null || !intent.hasExtra(MOVIE_OBJECT)) {
                closeOnError();
            }

            mSelectedMovie = intent.getParcelableExtra(MOVIE_OBJECT);
        }

        if (mSelectedMovie == null) {
            // Movies data unavailable
            closeOnError();
            return;
        }

        if (!TextUtils.isEmpty(mSelectedMovie.getBackdropPath())) {
            Picasso.with(this)
                    .load(ImageUtils.buildImageUrlWithImageType(
                            this,
                            mSelectedMovie.getBackdropPath(),
                            ImageUtils.BACKDROP))
                    .into(movieBackdropImageView);
        } else {
            movieBackdropImageView.setImageResource(R.drawable.backdrop);
        }

        movieTitleTextView.setText(mSelectedMovie.getMovieTitle());
        movieRatingTextView.setText(String.valueOf(mSelectedMovie.getVoteAverage()));
        movieReleaseDateTextView.setText(mSelectedMovie.getReleaseDate());

        if (!TextUtils.isEmpty(mSelectedMovie.getPosterPath())) {
            Picasso.with(this)
                    .load(ImageUtils.buildImageUrlWithImageType(
                            this,
                            mSelectedMovie.getPosterPath(),
                            ImageUtils.POSTER))
                    .into(moviePosterImageView);
        } else {
            moviePosterImageView.setImageResource(R.drawable.poster);
        }

        moviePlotTextView.setText(mSelectedMovie.getOverview());

        String[] movieGenre = new String[mSelectedMovie.getGenreIds().length];
        for (int i = 0; i < mSelectedMovie.getGenreIds().length; i++) {
            movieGenre[i] = MovieUtils.getStringMovieGenre(this, mSelectedMovie.getGenreIds()[i]);
        }
        movieGenreTextView.setText(TextUtils.join(", ", movieGenre));
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
}