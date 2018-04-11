package com.future.bestmovies.movie;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MovieDetails implements Parcelable {
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("genres")
    private List<MovieGenre> genres;
    private String[] genreArray;
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

    public MovieDetails(String backdropPath, String[] genreArray, int id, String language,
                        String overview, String posterPath, double voteAverage,
                        String releaseDate, int runtime, String title) {
        this.id = id;
        this.voteAverage = voteAverage;
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.genreArray = genreArray;
        this.language = language;
        this.runtime = runtime;
    }

    public MovieDetails() {

    }

    private MovieDetails(Parcel in) {
        id = in.readInt();
        voteAverage = in.readDouble();
        title = in.readString();
        posterPath = in.readString();
        backdropPath = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        //genres = in.createStringArray();
        genres = new ArrayList<>();
        in.readList(genres, MovieGenre.class.getClassLoader());
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
        //parcel.writeStringArray(genres);
        parcel.writeList(genres);
        parcel.writeString(language);
        parcel.writeInt(runtime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<MovieDetails> CREATOR = new Parcelable.Creator<MovieDetails>() {
        @Override
        public MovieDetails createFromParcel(Parcel in) {
            return new MovieDetails(in);
        }

        @Override
        public MovieDetails[] newArray(int size) { return new MovieDetails[size]; }
    };

    public int getMovieId() { return id; }
    public double getVoteAverage() { return voteAverage; }
    public String getMovieTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public String getOverview() { return overview; }
    public String getReleaseDate() { return releaseDate; }
    public String[] getGenres() {
        if (genres == null) {
            return genreArray;
        } else {
            genreArray = new String[genres.size()];
            for (int i = 0; i < genres.size(); i++ )
                genreArray[i] = genres.get(i).getName();

            return genreArray;
        }
    }
    public String getLanguage() { return language; }
    public int getRuntime() { return runtime; }
}
