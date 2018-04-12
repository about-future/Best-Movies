package com.future.bestmovies.videos;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.future.bestmovies.retrofit.ApiClient;
import com.future.bestmovies.retrofit.ApiInterface;
import com.future.bestmovies.reviews.Review;
import com.future.bestmovies.reviews.ReviewResponse;
import com.future.bestmovies.utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;


public class VideoLoader extends AsyncTaskLoader<ArrayList<Video>> {
    private ArrayList<Video> cachedVideos;
    private final int movieId;

    public VideoLoader(Context context, int movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        if (cachedVideos == null)
            forceLoad();
    }

    @Override
    public ArrayList<Video> loadInBackground() {
        ApiInterface movieApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<VideoResponse> call = movieApiInterface.getMovieVideos(movieId, NetworkUtils.API_ID);

        ArrayList<Video> result = new ArrayList<>();
        try {
            result = call.execute().body().getResults();
        } catch (IOException e) {
            Log.v("Video Loader", "Error: " + e.toString());
        }

        return result;
    }

    @Override
    public void deliverResult(ArrayList<Video> data) {
        cachedVideos = data;
        super.deliverResult(data);
    }
}
