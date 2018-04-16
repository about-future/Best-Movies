package com.future.bestmovies.search;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class SearchResult {
    @SerializedName("id")
    private int id;
    @SerializedName("media_type")
    private String type;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("profile_path")
    private String profilePath;
    @SerializedName("original_title")
    private String movieTitle;
    @SerializedName("original_name")
    private String showTitle;
    @SerializedName("name")
    private String personName;

    public SearchResult() {

    }

    public int getId() { return id; }
    public String getPosterPath() { return posterPath; }
    public String getProfilePath() { return profilePath; }
    public String getPersonName() { return personName; }
    public String getMovieTitle() { return movieTitle; }
    public String getShowTitle() { return showTitle; }

    public String getType() { return type; }
}
