package com.future.bestmovies.reviews;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable{
    private final int movieId;
    private final String author;
    private final String content;

    public Review(int movieId, String author, String content) {
        this.movieId = movieId;
        this.author = author;
        this.content = content;
    }

    private Review (Parcel in) {
        movieId = in.readInt();
        author = in.readString();
        content = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(movieId);
        parcel.writeString(author);
        parcel.writeString(content);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {return new Review[size];}
    };

    public int getMovieId() { return movieId; }
    public String getReviewAuthor() {
        return author;
    }
    public String getReviewContent() {
        return content;
    }
}
