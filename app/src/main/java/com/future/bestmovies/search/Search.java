package com.future.bestmovies.search;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Search implements Parcelable {
    @SerializedName("id")
    private int id;
    @SerializedName("media_type")
    private String type;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("original_title")
    private String title;
    @SerializedName("name")
    private String name;

    public Search () {

    }

    public int getId() { return id; }
    public String getPosterPath() { return posterPath; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getType() { return type; }

    @Override
    public int describeContents() {
        return 0;
    }

    private Search (Parcel in) {
        id = in.readInt();
        posterPath = in.readString();
        name = in.readString();
        title = in.readString();
        type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(posterPath);
        parcel.writeString(name);
        parcel.writeString(title);
        parcel.writeString(type);
    }

    public static final Parcelable.Creator<Search> CREATOR = new Parcelable.Creator<Search>() {
        @Override
        public Search createFromParcel(Parcel in) {
            return new Search(in);
        }

        @Override
        public Search[] newArray(int size) {return new Search[size];}
    };
}
