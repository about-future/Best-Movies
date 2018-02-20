package com.future.bestmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.utils.JsonUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

public class DetailsActivity extends AppCompatActivity {
    public Movie[] mMovies;
    public static final String ITEM_POSITION = "item_position";
    private static final int DEFAULT_POSITION = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        int position = intent.getIntExtra(ITEM_POSITION, DEFAULT_POSITION);
        if (position == DEFAULT_POSITION) {
            // EXTRA_POSITION not found in intent
            closeOnError();
            return;
        }

        String json = getResources().getString(R.string.json);

        try {
            mMovies = JsonUtils.parseMoviesJson(json);
            if (mMovies == null) {
                // Movies data unavailable
                closeOnError();
                return;
            }

            Movie selectedMovie = mMovies[position];
            TextView results = (TextView) findViewById(R.id.jsonResult_tv);

            results.setText(selectedMovie.getOverview());

            setTitle(selectedMovie.getMovieTitle());

        } catch (JSONException e) {
            Log.e("DetailsActivity.java", "Error parsing Movies JSON: ", e);
        }

//        ImageView iv = (ImageView) findViewById(R.id.details_poster_iv);
//        String mImageUrl = "http://image.tmdb.org/t/p/w185/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg";
//        Picasso.with(this)
//                .load(mImageUrl)
//                .into(iv);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.details_error_message, Toast.LENGTH_SHORT).show();
    }
}