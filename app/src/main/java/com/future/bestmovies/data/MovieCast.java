package com.future.bestmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieCast implements Parcelable {
    private String character;
    private int id;
    private String name;
    private String profilePath;

    public MovieCast(String character, int id, String name, String profilePath) {
        this.character = character;
        this.id = id;
        this.name = name;
        this.profilePath = profilePath;
    }

    private MovieCast(Parcel in) {
        character = in.readString();
        id = in.readInt();
        name = in.readString();
        profilePath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(character);
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(profilePath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<MovieCast> CREATOR = new Parcelable.Creator<MovieCast>() {
        @Override
        public MovieCast createFromParcel(Parcel in) {
            return new MovieCast(in);
        }

        @Override
        public MovieCast[] newArray(int size) {return new MovieCast[size];}
    };

    public String getCharacter() {
        return character;
    }
    public void setCharacter(String character) { this.character = character; }

    public int getActorId() {
        return id;
    }
    public void setActorId(int id) {
        this.id = id;
    }

    public String getActorName() {
        return name;
    }
    public void setActorName(String name) {
        this.name = name;
    }

    public String getProfilePath() {
        return profilePath;
    }
    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }
}
