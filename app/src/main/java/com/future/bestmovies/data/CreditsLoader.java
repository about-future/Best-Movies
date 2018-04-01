package com.future.bestmovies.data;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.future.bestmovies.utils.NetworkUtils;

import java.util.ArrayList;

public class CreditsLoader  extends AsyncTaskLoader<ArrayList<Credits>> {
    private final int actorId;

    public CreditsLoader(Context context, int actorId) {
        super(context);
        this.actorId = actorId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<Credits> loadInBackground() {
        return NetworkUtils.fetchActorCredits(actorId);
    }
}
