package com.future.bestmovies.movie;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.future.bestmovies.retrofit.ApiClient;
import com.future.bestmovies.retrofit.ApiInterface;
import com.future.bestmovies.utils.NetworkUtils;

import java.io.IOException;

import retrofit2.Call;

public class MovieDetailsLoader extends AsyncTaskLoader<MovieDetails> {
    private MovieDetails cachedMovie;
    private final int movieId;

    public MovieDetailsLoader(Context context, int movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        if (cachedMovie == null)
            forceLoad();
    }

    @Override
    public MovieDetails loadInBackground() {
        ApiInterface movieApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<MovieDetails> call = movieApiInterface.getMovieDetails(movieId, NetworkUtils.API_ID);

        MovieDetails result = new MovieDetails();
        try {
            result = call.execute().body();
        } catch (IOException e) {
            Log.v("MovieDetails Loader", "Error: " + e.toString());
        }

        return result; //NetworkUtils.fetchMovieDetails(movieId);
    }

    @Override
    public void deliverResult(MovieDetails data) {
        cachedMovie = data;
        super.deliverResult(data);
    }
}
