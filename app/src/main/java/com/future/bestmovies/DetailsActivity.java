package com.future.bestmovies;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.future.bestmovies.data.Movie;
import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity {
    public static final String MOVIE_OBJECT = "movie";
    private Movie mSelectedMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView movieBackdropImageView = findViewById(R.id.details_backdrop_iv);
        TextView movieTitleTextView = findViewById(R.id.details_title_tv);
        ImageView moviePosterImageView = findViewById(R.id.details_poster_iv);
        TextView moviePlotTextView = findViewById(R.id.details_plot_tv);
        TextView movieRatingTextView = findViewById(R.id.details_ratings_tv);
        TextView movieReleaseDateTextView = findViewById(R.id.details_release_date_tv);

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

        String basePath = "http://image.tmdb.org/t/p/w500";

        String backdropUrl = basePath.concat(mSelectedMovie.getBackdropPath());
        Picasso.with(this)
                .load(backdropUrl)
                .into(movieBackdropImageView);

        movieTitleTextView.setText(mSelectedMovie.getMovieTitle());
        movieRatingTextView.setText(String.valueOf(mSelectedMovie.getVoteAverage()));
        movieReleaseDateTextView.setText(mSelectedMovie.getReleaseDate());

        String basePosterPath = "http://image.tmdb.org/t/p/w185";
        String posterUrl = basePosterPath.concat(mSelectedMovie.getPosterPath());
        Picasso.with(this)
                .load(posterUrl)
                .into(moviePosterImageView);

        moviePlotTextView.setText(mSelectedMovie.getOverview());
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
}