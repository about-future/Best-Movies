package com.future.bestmovies.data;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.future.bestmovies.utils.NetworkUtils;

import java.util.ArrayList;


public class VideoLoader extends AsyncTaskLoader<ArrayList<Video>> {
    private final String movieId;

    public VideoLoader(Context context, String movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<Video> loadInBackground() {
        return NetworkUtils.fetchMovieVideos(movieId);
    }
}
