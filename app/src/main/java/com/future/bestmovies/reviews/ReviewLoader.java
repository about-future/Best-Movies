package com.future.bestmovies.reviews;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.future.bestmovies.retrofit.ApiClient;
import com.future.bestmovies.retrofit.ApiInterface;
import com.future.bestmovies.utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;


public class ReviewLoader extends AsyncTaskLoader<ArrayList<Review>> {
    private ArrayList<Review> cacheReviews;
    private final int movieId;

    public ReviewLoader(Context context, int movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        if (cacheReviews == null)
            forceLoad();
    }

    @Override
    public ArrayList<Review> loadInBackground() {
        ApiInterface movieApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ReviewResponse> call = movieApiInterface.getMovieReviews(movieId, NetworkUtils.API_ID);

        ArrayList<Review> result = new ArrayList<>();
        try {
            result = Objects.requireNonNull(call.execute().body()).getResults();
        } catch (IOException e) {
            Log.v("Review Loader", "Error: " + e.toString());
        }

        return result;
    }

    @Override
    public void deliverResult(ArrayList<Review> data) {
        cacheReviews = data;
        super.deliverResult(data);
    }
}
