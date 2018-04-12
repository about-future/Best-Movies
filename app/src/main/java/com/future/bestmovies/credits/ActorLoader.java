package com.future.bestmovies.credits;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.future.bestmovies.retrofit.ApiClient;
import com.future.bestmovies.retrofit.ApiInterface;
import com.future.bestmovies.utils.NetworkUtils;

import java.io.IOException;

import retrofit2.Call;


public class ActorLoader extends AsyncTaskLoader<Actor> {
    private Actor cachedActorProfile;
    private final int actorId;

    public ActorLoader(Context context, int actorId) {
        super(context);
        this.actorId = actorId;
    }

    @Override
    protected void onStartLoading() {
        if (cachedActorProfile == null)
            forceLoad();
    }

    @Override
    public Actor loadInBackground() {
        ApiInterface movieApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Actor> call = movieApiInterface.getActorProfile(actorId, NetworkUtils.API_ID);

        Actor result = new Actor();
        try {
            result = call.execute().body();
        } catch (IOException e) {
            Log.v("Actor Loader", "Error: " + e.toString());
        }

        return result;
    }

    @Override
    public void deliverResult(Actor data) {
        cachedActorProfile = data;
        super.deliverResult(data);
    }
}
