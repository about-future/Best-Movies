package com.future.bestmovies.credits;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.future.bestmovies.retrofit.ApiClient;
import com.future.bestmovies.retrofit.ApiInterface;
import com.future.bestmovies.utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;

public class CreditsLoader  extends AsyncTaskLoader<ArrayList<Credits>> {
    private ArrayList<Credits> cachedCredits;
    private final int actorId;

    public CreditsLoader(Context context, int actorId) {
        super(context);
        this.actorId = actorId;
    }

    @Override
    protected void onStartLoading() {
        if (cachedCredits == null)
            forceLoad();
    }

    @Override
    public ArrayList<Credits> loadInBackground() {
        ApiInterface movieApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<CreditsResponse> call = movieApiInterface.getActorCredits(actorId, NetworkUtils.API_ID);

        ArrayList<Credits> result = new ArrayList<>();
        try {
            result = call.execute().body().getCredits();
        } catch (IOException e) {
            Log.v("Credits Loader", "Error: " + e.toString());
        }

        return result;
    }

    @Override
    public void deliverResult(@Nullable ArrayList<Credits> data) {
        cachedCredits = data;
        super.deliverResult(data);
    }
}
