package com.future.bestmovies.search;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.future.bestmovies.movie.MoviePreferences;
import com.future.bestmovies.retrofit.ApiClient;
import com.future.bestmovies.retrofit.ApiInterface;
import com.future.bestmovies.reviews.Review;
import com.future.bestmovies.reviews.ReviewResponse;
import com.future.bestmovies.utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;

public class SearchLoader extends AsyncTaskLoader<ArrayList<Search>> {
    private ArrayList<Search> cachedSearch;
    private final String searchQuery;
    //private Context context;

    public SearchLoader(Context context, String searchQuery) {
        super(context);
        //this.context = context;
        this.searchQuery = searchQuery;
    }

    @Override
    protected void onStartLoading() {
        if (cachedSearch == null)
            forceLoad();
    }

    @Override
    public ArrayList<Search> loadInBackground() {
        ApiInterface movieApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<SearchResponse> call = movieApiInterface.searchResults(
                searchQuery,
                NetworkUtils.API_ID,
                MoviePreferences.getLastSearchPageNumber(getContext()),
                false);

        ArrayList<Search> result = new ArrayList<>();
        try {
            result = Objects.requireNonNull(call.execute().body()).getResults();
        } catch (IOException e) {
            Log.v("Review Loader", "Error: " + e.toString());
        }

        return result;
    }

    @Override
    public void deliverResult(ArrayList<Search> data) {
        cachedSearch = data;
        super.deliverResult(data);
    }
}
