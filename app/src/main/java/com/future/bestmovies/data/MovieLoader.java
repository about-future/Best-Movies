package com.future.bestmovies.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.future.bestmovies.utils.NetworkUtils;
import java.net.URL;

public class MovieLoader extends AsyncTaskLoader<Movie[]> {

    public MovieLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        //Log.v("MOVIE LOADER", "onStartLoading... again!");
        forceLoad();
    }

    @Override
    public Movie[] loadInBackground() {
        return NetworkUtils.fetchMovieData(getContext());
    }
}
