package com.future.bestmovies.movie_details;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Details implements Parcelable {
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("genres")
    private ArrayList<Genre> genres;
    @SerializedName("id")
    private int id;
    @SerializedName("original_language")
    private String language;
    @SerializedName("overview")
    private String overview;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("vote_average")
    private double voteAverage;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("runtime")
    private int runtime;
    @SerializedName("original_title")
    private String title;

    public Details(String backdropPath, ArrayList<Genre> genres, int id, String language,
                   String overview, String posterPath, double voteAverage,
                   String releaseDate, int runtime, String title) {
        this.id = id;
        this.voteAverage = voteAverage;
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.genres = genres;
        this.language = language;
        this.runtime = runtime;
    }

    public Details() {

    }

    private Details(Parcel in) {
        id = in.readInt();
        voteAverage = in.readDouble();
        title = in.readString();
        posterPath = in.readString();
        backdropPath = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        // TOD: Genres list implementation still unsure
        genres = new ArrayList<>();
        in.readList(genres, Genre.class.getClassLoader());
        language = in.readString();
        runtime = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeDouble(voteAverage);
        parcel.writeString(title);
        parcel.writeString(posterPath);
        parcel.writeString(backdropPath);
        parcel.writeString(overview);
        parcel.writeString(releaseDate);
        parcel.writeTypedList(genres);
        parcel.writeString(language);
        parcel.writeInt(runtime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Details> CREATOR = new Parcelable.Creator<Details>() {
        @Override
        public Details createFromParcel(Parcel in) {
            return new Details(in);
        }

        @Override
        public Details[] newArray(int size) { return new Details[size]; }
    };

    public int getMovieId() { return id; }
    public double getVoteAverage() { return voteAverage; }
    public String getMovieTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public String getOverview() { return overview; }
    public String getReleaseDate() { return releaseDate; }
    public String getGenres() {
        // Create a string array with the same size as the genres ArrayList
        String[] genre = new String[genres.size()];
        // Populate the string array with each genre name
        for (int i = 0; i < genres.size(); i++)
            genre[i] = genres.get(i).getName();
        // Join all genres with commas between them and return the resulted string
        return TextUtils.join(", ", genre);
    }
    public String getLanguage() { return language; }
    public int getRuntime() { return runtime; }
}
