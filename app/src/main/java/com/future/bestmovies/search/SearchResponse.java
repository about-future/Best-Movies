package com.future.bestmovies.search;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SearchResponse {
    @SerializedName("results")
    private final ArrayList<Search> results = new ArrayList<>();

    public ArrayList<Search> getResults() { return results; }
}
