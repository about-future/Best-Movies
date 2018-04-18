package com.future.bestmovies.movie;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.future.bestmovies.retrofit.ApiClient;
import com.future.bestmovies.retrofit.ApiInterface;
import com.future.bestmovies.utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;


public class CategoryLoader extends AsyncTaskLoader<ArrayList<Movie>> {
    private ArrayList<Movie> cacheMovies;

    public CategoryLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (cacheMovies == null)
            forceLoad();
    }

    @Override
    public ArrayList<Movie> loadInBackground() {
        ApiInterface moviesApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<CategoryResponse> call = moviesApiInterface.getMovies(
                MoviePreferences.getPreferredQueryType(getContext()),
                MoviePreferences.getLastPageNumber(getContext()),
                NetworkUtils.API_ID);

        ArrayList<Movie> result = new ArrayList<>();
        try {
            result = Objects.requireNonNull(call.execute().body()).getResults();
        } catch (IOException e) {
            Log.v("Movies Loader", "Error: " + e.toString());
        }

        return result;
    }

    @Override
    public void deliverResult(@Nullable ArrayList<Movie> data) {
        cacheMovies = data;
        super.deliverResult(data);
    }
}
