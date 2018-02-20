package com.future.bestmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.utils.JsonUtils;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements MovieAdapter.GridItemClickListener {
    private static final int ID_MOVIES_LOADER = 24;

    MovieAdapter mAdapter;
    RecyclerView mMoviesRecyclerView;

    private ProgressBar mLoading;

    private Toast mToast;
    public Movie[] mMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.movies_rv);
        mLoading = (ProgressBar) findViewById(R.id.loading_pb);
        String json = getResources().getString(R.string.json);

        try {
            mMovies = JsonUtils.parseMoviesJson(json);
        } catch (JSONException e) {
            Log.e("MainActivity.java", "Error parsing Movies JSON: ", e);
        }

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
        if (id == R.id.action_settings) {
            mAdapter = new MovieAdapter(getApplicationContext(), mMovies, this);
            mMoviesRecyclerView.setAdapter(mAdapter);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    //public void onGridItemClick(long movieId) {
    public void onGridItemClick(int clickedItemPosition) {
        if (mToast != null) {
            mToast.cancel();
        }

        String toastMessage = "Item #" + clickedItemPosition + " clicked";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

        mToast.show();

        Intent movieDetailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
        movieDetailsIntent.putExtra(DetailsActivity.ITEM_POSITION, clickedItemPosition);
//        //Uri uriForMovieClicked = MovieContract.MovieEntry.buildMovieUriWithId(movieId);
//        //movieDetailsIntent.setData(uriForMovieClicked);
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