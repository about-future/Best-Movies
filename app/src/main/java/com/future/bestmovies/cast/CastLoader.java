package com.future.bestmovies.cast;

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


public class CastLoader extends AsyncTaskLoader<ArrayList<Cast>> {
    private ArrayList<Cast> cachedCast;
    private final int movieId;

    public CastLoader(Context context, int movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        if (cachedCast == null)
            forceLoad();
    }

    @Override
    public ArrayList<Cast> loadInBackground() {
        ApiInterface movieApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<CastResponse> call = movieApiInterface.getMovieCast(movieId, NetworkUtils.API_ID);

        ArrayList<Cast> result = new ArrayList<>();
        try {
            result = Objects.requireNonNull(call.execute().body()).getCast();
        } catch (IOException e) {
            Log.v("Cast Loader", "Error: " + e.toString());
        }

        return result;
    }

    @Override
    public void deliverResult(ArrayList<Cast> data) {
        cachedCast = data;
        super.deliverResult(data);
    }
}
