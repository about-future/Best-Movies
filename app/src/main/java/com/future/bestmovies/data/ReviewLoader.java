package com.future.bestmovies.data;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.future.bestmovies.utils.NetworkUtils;

import java.util.ArrayList;


public class ReviewLoader extends AsyncTaskLoader<ArrayList<Review>> {
    private final int movieId;

    public ReviewLoader(Context context, int movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<Review> loadInBackground() {
        return NetworkUtils.fetchMovieReviews(movieId);
    }
}
