package com.future.bestmovies.data;

/**
 * Created by Office on 19/3/2018.
 */

public class Video {
    private final int movieId;
    private final String key;
    private final String name;
    private final String type;

    public Video(int movieId, String key, String name, String type) {
        this.movieId = movieId;
        this.key = key;
        this.name = name;
        this.type = type;
    }

    public int getMovieId() { return movieId; }
    public String getVideoKey() {
        return key;
    }
    public String getVideoName() {
        return name;
    }
    public String getVideoType() {
        return type;
    }
}
