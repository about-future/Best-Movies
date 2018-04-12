package com.future.bestmovies.videos;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class VideoResponse {
    @SerializedName("results")
    private ArrayList<Video> results = new ArrayList<>();

    public ArrayList<Video> getResults() {
        return results;
    }
}
