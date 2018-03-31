package com.future.bestmovies.data;

import android.os.Parcel;
import android.os.Parcelable;


public class Movie implements Parcelable {
    private int id;
    private String title;
    private String posterPath;

    public Movie(int id, String posterPath, String title) {
        this.id = id;
        this.posterPath = posterPath;
        this.title = title;
    }

    private Movie(Parcel in) {
        id = in.readInt();
        posterPath = in.readString();
        title = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(posterPath);
        parcel.writeString(title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public int getMovieId() { return id; }
    public String getPosterPath() { return posterPath; }
    public String getTitle() { return title; }
}