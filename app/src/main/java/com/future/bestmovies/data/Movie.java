package com.future.bestmovies.data;

import android.os.Parcel;
import android.os.Parcelable;


public class Movie implements Parcelable {
    private int id;
    private double voteAverage;
    private String title;
    private String posterPath;
    private String backdropPath;
    private String overview;
    private String releaseDate;
    private int[] genreIds;

    public Movie(int id, double voteAverage, String title, String posterPath, String backdropPath, String overview, String releaseDate, int[] genreIds) {
        this.id = id;
        this.voteAverage = voteAverage;
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.genreIds = genreIds;
    }

    private Movie(Parcel in) {
        id = in.readInt();
        voteAverage = in.readDouble();
        title = in.readString();
        posterPath = in.readString();
        backdropPath = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        genreIds = in.createIntArray();
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
        parcel.writeIntArray(genreIds);
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
    public double getVoteAverage() { return voteAverage; }
    public String getMovieTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public String getOverview() { return overview; }
    public String getReleaseDate() { return releaseDate; }
    public int[] getGenreIds() { return genreIds; }

    public void setMovieId(int id) { this.id = id; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }
    public void setMovieTitle(String title) { this.title = title; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }
    public void setOverview(String overview) { this.overview = overview; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public void setGenreIds(int[] genreIds) { this.genreIds = genreIds; }
}