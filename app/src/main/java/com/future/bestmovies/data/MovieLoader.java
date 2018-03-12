package com.future.bestmovies.data;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.future.bestmovies.utils.NetworkUtils;


public class MovieLoader extends AsyncTaskLoader<Movie[]> {

    public MovieLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() { forceLoad(); }

    @Override
    public Movie[] loadInBackground() {
        return NetworkUtils.fetchMovieData(getContext());
    }
}
