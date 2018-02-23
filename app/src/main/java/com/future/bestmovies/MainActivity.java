package com.future.bestmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.utils.JsonUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements MovieAdapter.GridItemClickListener {
    private static final int ID_MOVIES_LOADER = 24;

    MovieAdapter mAdapter;
    RecyclerView mMoviesRecyclerView;

    private ProgressBar mLoading;
    public Movie[] mMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        mMoviesRecyclerView = findViewById(R.id.movies_rv);
        mLoading = findViewById(R.id.loading_pb);
        String json = getResources().getString(R.string.json);

        try {
            mMovies = JsonUtils.parseMoviesJson(json);

            String baseUrl = "http://image.tmdb.org/t/p/w500";
            String imageUrl = mMovies[0].getBackdropPath();
            String posterUrl = baseUrl.concat(imageUrl);
            ImageView mainBrackdrop = findViewById(R.id.main_backdrop);
            Picasso.with(this)
                    .load(posterUrl)
                    .into(mainBrackdrop);

            int numberOfColumns;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                numberOfColumns = 2;
            } else {
                numberOfColumns = 3;
            }

            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfColumns);
            mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
            mMoviesRecyclerView.setHasFixedSize(true);
            mAdapter = new MovieAdapter(this, mMovies, this);
            mMoviesRecyclerView.setAdapter(mAdapter);

        } catch (JSONException e) {
            Log.e("MainActivity.java", "Error parsing Movies JSON: ", e);
        }

        //showLoading();

        //Loader init
        //getSupportLoaderManager().initLoader(ID_MOVIES_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            mAdapter = new MovieAdapter(getApplicationContext(), mMovies, this);
            mMoviesRecyclerView.setAdapter(mAdapter);
            return true;
        }

        if (id == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    //public void onGridItemClick(long movieId) {
    public void onGridItemClick(int clickedItemPosition) {
        Intent movieDetailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
        movieDetailsIntent.putExtra(DetailsActivity.MOVIE_OBJECT, mMovies[clickedItemPosition]);
        startActivity(movieDetailsIntent);
    }

    private void showMovies() {
        // Hide the loading indicator and show movie data
        mLoading.setVisibility(View.INVISIBLE);
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        // Hide the movie data and show loading indicator
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }
}