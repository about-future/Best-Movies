package com.future.bestmovies.reviews;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Review implements Parcelable{
    @SerializedName("author")
    private final String author;
    @SerializedName("content")
    private final String content;

    public Review(String author, String content) {
        this.author = author;
        this.content = content;
    }

    private Review (Parcel in) {
        author = in.readString();
        content = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
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

    public String getReviewAuthor() {
        return author;
    }
    public String getReviewContent() {
        return content;
    }
}
