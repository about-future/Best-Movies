package com.future.bestmovies.data;

public class Review {
    private final int movieId;
    private final String author;
    private final String content;

    public Review(int movieId, String author, String content) {
        this.movieId = movieId;
        this.author = author;
        this.content = content;
    }

    public int getMovieId() { return movieId; }
    public String getReviewAuthor() {
        return author;
    }
    public String getReviewContent() {
        return content;
    }
}
