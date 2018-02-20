package com.future.bestmovies.data;

public class Movie {
    private int id;
    private double voteAverage;
    private String title;
    private String posterPath;
    private String overview;
    private String releaseDate;

    // No args constructor for use in serialization
    public Movie() {
    }

    public Movie(int id, double voteAverage, String title, String posterPath, String overview, String releaseDate) {
        this.id = id;
        this.voteAverage = voteAverage;
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
    }

    public int getMovieId() {
        return id;
    }
    public void setMovieId(int id) {
        this.id = id;
    }

    public double getVoteAverage() {
        return voteAverage;
    }
    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getMovieTitle() {
        return title;
    }
    public void setMovieTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }
    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}