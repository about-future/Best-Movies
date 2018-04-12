package com.future.bestmovies.reviews;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ReviewResponse {
    @SerializedName("results")
    private ArrayList<Review> results = new ArrayList<>();

    public ArrayList<Review> getResults() {
        return results;
    }
}
