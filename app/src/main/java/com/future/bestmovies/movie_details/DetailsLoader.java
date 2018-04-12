package com.future.bestmovies.movie_details;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.future.bestmovies.retrofit.ApiClient;
import com.future.bestmovies.retrofit.ApiInterface;
import com.future.bestmovies.utils.NetworkUtils;

import java.io.IOException;

import retrofit2.Call;

public class DetailsLoader extends AsyncTaskLoader<Details> {
    private Details cachedMovie;
    private final int movieId;

    public DetailsLoader(Context context, int movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        if (cachedMovie == null)
            forceLoad();
    }

    @Override
    public Details loadInBackground() {
        ApiInterface movieApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Details> call = movieApiInterface.getMovieDetails(movieId, NetworkUtils.API_ID);

        Details result = new Details();
        try {
            result = call.execute().body();
        } catch (IOException e) {
            Log.v("Details Loader", "Error: " + e.toString());
        }

        return result;
    }

    @Override
    public void deliverResult(Details data) {
        cachedMovie = data;
        super.deliverResult(data);
    }
}
