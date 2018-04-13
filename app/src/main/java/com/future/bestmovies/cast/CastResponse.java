package com.future.bestmovies.cast;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CastResponse {
    @SerializedName("cast")
    private final ArrayList<Cast> results = new ArrayList<>();

    public ArrayList<Cast> getCast() {
        return results;
    }
}
