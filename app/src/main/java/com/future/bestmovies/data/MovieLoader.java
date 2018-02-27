package com.future.bestmovies.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import com.future.bestmovies.utils.NetworkUtils;
import java.net.URL;

public class MovieLoader extends AsyncTaskLoader<Movie[]> {
    private URL mUrl;

    public MovieLoader(Context context, URL url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Movie[] loadInBackground() {
        if (mUrl == null) return null;

        return NetworkUtils.fetchMovieData(mUrl);
    }
}
