package com.future.bestmovies.videos;


import android.os.Parcel;
import android.os.Parcelable;

public class Video implements Parcelable{
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(movieId);
        parcel.writeString(key);
        parcel.writeString(name);
        parcel.writeString(type);
    }

    public Video (Parcel in) {
        movieId = in.readInt();
        key = in.readString();
        name = in.readString();
        type = in.readString();
    }

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {return new Video[size];}
    };
}
