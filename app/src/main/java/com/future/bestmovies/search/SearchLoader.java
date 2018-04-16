package com.future.bestmovies.search;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.future.bestmovies.movie.MoviePreferences;
import com.future.bestmovies.retrofit.ApiClient;
import com.future.bestmovies.retrofit.ApiInterface;
import com.future.bestmovies.utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;

public class SearchLoader extends AsyncTaskLoader<ArrayList<SearchResult>> {
    private ArrayList<SearchResult> cachedSearchResults;
    private final String searchQuery;
    //private Context context;

    public SearchLoader(Context context, String searchQuery) {
        super(context);
        //this.context = context;
        this.searchQuery = searchQuery;
    }

    @Override
    protected void onStartLoading() {
        if (cachedSearchResults == null)
            forceLoad();
    }

    @Override
    public ArrayList<SearchResult> loadInBackground() {
        ApiInterface movieApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<SearchResponse> call = movieApiInterface.searchResults(
                searchQuery,
                NetworkUtils.API_ID,
                MoviePreferences.getLastSearchPageNumber(getContext()),
                false);

        ArrayList<SearchResult> result = new ArrayList<>();
        try {
            result = Objects.requireNonNull(call.execute().body()).getResults();
        } catch (IOException e) {
            Log.v("Review Loader", "Error: " + e.toString());
        }

        return result;
    }

    @Override
    public void deliverResult(ArrayList<SearchResult> data) {
        cachedSearchResults = data;
        super.deliverResult(data);
    }
}
