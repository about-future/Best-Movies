package com.future.bestmovies.data;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.future.bestmovies.utils.NetworkUtils;

public class MovieDetailsLoader extends AsyncTaskLoader<MovieDetails> {
    private final int movieId;

    public MovieDetailsLoader(Context context, int movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() { forceLoad(); }

    @Override
    public MovieDetails loadInBackground() { return NetworkUtils.fetchMovieDetails(movieId); }
}
