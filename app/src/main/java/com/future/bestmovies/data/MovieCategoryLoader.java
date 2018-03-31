package com.future.bestmovies.data;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.future.bestmovies.utils.NetworkUtils;

import java.util.ArrayList;


public class MovieCategoryLoader extends AsyncTaskLoader<ArrayList<Movie>> {

    public MovieCategoryLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() { forceLoad(); }

    @Override
    public ArrayList<Movie> loadInBackground() {
        return NetworkUtils.fetchMovieCategory(getContext());
    }
}
