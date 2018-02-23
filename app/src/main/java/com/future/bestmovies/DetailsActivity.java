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

    // TODO: 1
    private static final String SANDWICH_NAME = "name";
    private static final String SANDWICH_IMAGE_URL = "image";
    private static final String SANDWICH_ORIGIN = "origin";
    private static final String SANDWICH_AKA = "also_known_as";
    private static final String SANDWICH_DESCRIPTION = "description";
    private static final String SANDWICH_INGREDIENTS = "ingredients";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView movieBackdropImageView = (ImageView) findViewById(R.id.details_backdrop_iv);
        TextView movieTitleTextView = (TextView) findViewById(R.id.details_title_tv);
        ImageView moviePosterImageView = (ImageView) findViewById(R.id.details_poster_iv);
        TextView moviePlotTextView = (TextView) findViewById(R.id.details_plot_tv);
        TextView movieRatingTextView = (TextView) findViewById(R.id.details_ratings_tv);
        TextView movieReleaseDateTextView = (TextView) findViewById(R.id.details_release_date_tv);

        //getSupportActionBar().setElevation(0f);
        if (savedInstanceState != null) {
            // TODO: 2

        } else {
            Intent intent = getIntent();
            if (intent == null || !intent.hasExtra(MOVIE_OBJECT)) {
                closeOnError();
            }

            Movie selectedMovie = intent.getParcelableExtra(MOVIE_OBJECT);

            if (selectedMovie == null) {
                // Movies data unavailable
                closeOnError();
                return;
            }

            String basePath = "http://image.tmdb.org/t/p/w500";

            String backdropUrl = basePath.concat(selectedMovie.getBackdropPath());
            Picasso.with(this)
                    .load(backdropUrl)
                    .into(movieBackdropImageView);

            movieTitleTextView.setText(selectedMovie.getMovieTitle());
            movieRatingTextView.setText(String.valueOf(selectedMovie.getVoteAverage()));
            movieReleaseDateTextView.setText(selectedMovie.getReleaseDate());

            String basePosterPath = "http://image.tmdb.org/t/p/w185";
            String posterUrl = basePosterPath.concat(selectedMovie.getPosterPath());
            Picasso.with(this)
                    .load(posterUrl)
                    .into(moviePosterImageView);

            moviePlotTextView.setText(selectedMovie.getOverview());
        }
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
        super.onSaveInstanceState(outState);

        // TODO: 3
        // Put the contents of the Views into the outState Bundle.
//        outState.putString(SANDWICH_NAME, getTitle().toString());
//        outState.putString(SANDWICH_IMAGE_URL, mImageUrl);
//        outState.putString(SANDWICH_ORIGIN, mOriginTv.getText().toString());
//        outState.putString(SANDWICH_AKA, mAlsoKnownTv.getText().toString());
//        outState.putString(SANDWICH_DESCRIPTION, mDescriptionTv.getText().toString());
//        outState.putString(SANDWICH_INGREDIENTS, mIngredientsTv.getText().toString());
    }
}