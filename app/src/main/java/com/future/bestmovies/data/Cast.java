package com.future.bestmovies.data;

import android.os.Parcel;
import android.os.Parcelable;


public class Cast implements Parcelable {
    private final int movieId;
    private final String character;
    private final int actorId;
    private final String name;
    private final String profilePath;

    public Cast(int movieId, String character, int id, String name, String profilePath) {
        this.movieId = movieId;
        this.character = character;
        this.actorId = id;
        this.name = name;
        this.profilePath = profilePath;
    }

    private Cast(Parcel in) {
        movieId = in.readInt();
        character = in.readString();
        actorId = in.readInt();
        name = in.readString();
        profilePath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(movieId);
        parcel.writeString(character);
        parcel.writeInt(actorId);
        parcel.writeString(name);
        parcel.writeString(profilePath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Cast> CREATOR = new Parcelable.Creator<Cast>() {
        @Override
        public Cast createFromParcel(Parcel in) {
            return new Cast(in);
        }

        @Override
        public Cast[] newArray(int size) {return new Cast[size];}
    };

    public int getMovieId() { return movieId; }
    public String getActorName() {
        return name;
    }
    public int getActorId() { return actorId; }
    public String getCharacter() { return character; }
    public String getProfilePath() { return profilePath; }
}
