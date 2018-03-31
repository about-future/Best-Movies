package com.future.bestmovies.data;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import com.future.bestmovies.utils.NetworkUtils;


public class ActorLoader extends AsyncTaskLoader<Actor> {
    private final String actorId;

    public ActorLoader(Context context, String actorId) {
        super(context);
        this.actorId = actorId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Actor loadInBackground() {
        return NetworkUtils.fetchActorDetails(actorId);
    }
}
