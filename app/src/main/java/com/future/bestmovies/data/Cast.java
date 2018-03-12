package com.future.bestmovies.data;

import android.os.Parcel;
import android.os.Parcelable;


public class Cast implements Parcelable {
    private final String character;
    private final int id;
    private final String name;
    private final String profilePath;

    public Cast(String character, int id, String name, String profilePath) {
        this.character = character;
        this.id = id;
        this.name = name;
        this.profilePath = profilePath;
    }

    private Cast(Parcel in) {
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
    public String getProfilePath() { return profilePath; }
}
