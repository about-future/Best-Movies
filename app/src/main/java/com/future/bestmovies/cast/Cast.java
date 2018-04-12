package com.future.bestmovies.cast;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;


public class Cast implements Parcelable {
    @SerializedName("character")
    private final String character;
    @SerializedName("id")
    private final int actorId;
    @SerializedName("name")
    private final String name;
    @SerializedName("profile_path")
    private final String profilePath;

    public Cast(String character, int id, String name, String profilePath) {
        this.character = character;
        this.actorId = id;
        this.name = name;
        this.profilePath = profilePath;
    }

    private Cast(Parcel in) {
        character = in.readString();
        actorId = in.readInt();
        name = in.readString();
        profilePath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
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

    public String getActorName() {
        return name;
    }
    public int getActorId() { return actorId; }
    public String getCharacter() { return character; }
    public String getProfilePath() { return profilePath; }
}
