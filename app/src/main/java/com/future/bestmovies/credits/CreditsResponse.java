package com.future.bestmovies.credits;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CreditsResponse {
    @SerializedName("cast")
    private ArrayList<Credits> results = new ArrayList<>();

    public ArrayList<Credits> getCredits() { return results; }
}
