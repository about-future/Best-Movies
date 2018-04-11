package com.future.bestmovies.credits;

import android.os.Parcel;
import android.os.Parcelable;

public class Actor implements Parcelable {
    private final int id;
    private final String birthday;
    private final String deathday;
    private final String gender;
    private final String name;
    private final String biography;
    private final String placeOfBirth;
    private final String profilePath;

    public Actor(int id, String birthday, String deathday, String gender, String name, String biography, String placeOfBirth, String profilePath) {
        this.id = id;
        this.birthday = birthday;
        this.deathday = deathday;
        this.gender = gender;
        this.name = name;
        this.biography = biography;
        this.placeOfBirth = placeOfBirth;
        this.profilePath = profilePath;
    }

    private Actor (Parcel in) {
        id = in.readInt();
        birthday = in.readString();
        deathday = in.readString();
        gender = in.readString();
        name = in.readString();
        biography = in.readString();
        placeOfBirth = in.readString();
        profilePath = in.readString();
    }

    public static final Parcelable.Creator<Actor> CREATOR = new Parcelable.Creator<Actor>() {
        @Override
        public Actor createFromParcel(Parcel in) {
            return new Actor(in);
        }

        @Override
        public Actor[] newArray(int size) {return new Actor[size];}
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(birthday);
        parcel.writeString(deathday);
        parcel.writeString(gender);
        parcel.writeString(name);
        parcel.writeString(placeOfBirth);
        parcel.writeString(biography);
        parcel.writeString(profilePath);
    }

    public int getActorId() { return id; }
    public String getBirthday() { return birthday; }
    public String getDeathday() { return deathday; }
    public String getGender() { return gender; }
    public String getActorName() { return name; }
    public String getBiography() { return biography; }
    public String getPlaceOfBirth() { return placeOfBirth; }
    public String getProfilePath() { return profilePath; }


}
