package com.future.bestmovies.data;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.future.bestmovies.utils.NetworkUtils;


public class CastLoader extends AsyncTaskLoader<Cast[]> {
    private final String movieId;

    public CastLoader(Context context, String movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Cast[] loadInBackground() {
        return NetworkUtils.fetchMovieCast(movieId);
    }
}
