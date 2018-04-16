package com.future.bestmovies.search;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SearchResponse {
    @SerializedName("results")
    private final ArrayList<SearchResult> results = new ArrayList<>();

    public ArrayList<SearchResult> getResults() { return results; }
}
