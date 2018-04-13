package com.future.bestmovies.movie;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MovieResponse {
    @SerializedName("results")
    private final ArrayList<Movie> results = new ArrayList<>();

    public ArrayList<Movie> getResults() {
        return results;
    }
}
