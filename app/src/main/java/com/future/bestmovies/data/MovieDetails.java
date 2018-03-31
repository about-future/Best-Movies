package com.future.bestmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieDetails implements Parcelable {
    private int id;
    private double voteAverage;
    private String title;
    private String posterPath;
    private String backdropPath;
    private String overview;
    private String releaseDate;
    private String[] genreIds;
    private String language;
    private int runtime;

    public MovieDetails(String backdropPath, String[] genreIds, int id, String language,
                        String overview, String posterPath, double voteAverage,
                        String releaseDate, int runtime, String title) {
        this.id = id;
        this.voteAverage = voteAverage;
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.genreIds = genreIds;
        this.language = language;
        this.runtime = runtime;
    }

    private MovieDetails(Parcel in) {
        id = in.readInt();
        voteAverage = in.readDouble();
        title = in.readString();
        posterPath = in.readString();
        backdropPath = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        genreIds = in.createStringArray();
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
        parcel.writeStringArray(genreIds);
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
    public String[] getGenreIds() { return genreIds; }
    public String getLanguage() { return language; }
    public int getRuntime() { return runtime; }
}
