package com.future.bestmovies.movie;

import com.google.gson.annotations.SerializedName;

public class MovieGenre {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;

    public MovieGenre (String name) {
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
}
