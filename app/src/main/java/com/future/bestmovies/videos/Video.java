package com.future.bestmovies.videos;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Video implements Parcelable{
    @SerializedName("key")
    private final String key;
    @SerializedName("name")
    private final String name;
    @SerializedName("type")
    private final String type;

    public Video(String key, String name, String type) {
        this.key = key;
        this.name = name;
        this.type = type;
    }

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
        parcel.writeString(key);
        parcel.writeString(name);
        parcel.writeString(type);
    }

    public Video (Parcel in) {
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
