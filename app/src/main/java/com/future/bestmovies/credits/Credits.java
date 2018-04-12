package com.future.bestmovies.credits;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Credits implements Parcelable{
    @SerializedName("character")
    private final String character;
    @SerializedName("id")
    private final int movieId;
    @SerializedName("poster_path")
    private final String posterPath;
    @SerializedName("release_date")
    private final String releaseDate;
    @SerializedName("title")
    private final String title;

    public Credits (String character, int movieId, String posterPath, String releaseDate, String title) {
        this.character = character;
        this.movieId = movieId;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.title = title;
    }

    private Credits (Parcel in) {
        character = in.readString();
        movieId = in.readInt();
        posterPath = in.readString();
        releaseDate = in.readString();
        title = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(character);
        parcel.writeInt(movieId);
        parcel.writeString(posterPath);
        parcel.writeString(releaseDate);
        parcel.writeString(title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Credits> CREATOR = new Parcelable.Creator<Credits>() {
        @Override
        public Credits createFromParcel(Parcel in) {
            return new Credits(in);
        }

        @Override
        public Credits[] newArray(int size) {return new Credits[size];}
    };

    public String getCharacter() { return character; }
    public int getMovieId() { return movieId; }
    public String getPosterPath() { return posterPath; }
    public String getReleaseDate() { return releaseDate; }
    public String getTitle() { return title; }
}
