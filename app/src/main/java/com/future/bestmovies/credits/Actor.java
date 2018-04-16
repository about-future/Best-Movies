package com.future.bestmovies.credits;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Actor implements Parcelable {
    @SerializedName("id")
    private int id;
    @SerializedName("birthday")
    private String birthday;
    @SerializedName("deathday")
    private String deathDay;
    @SerializedName("gender")
    private int gender;
    @SerializedName("name")
    private String name;
    @SerializedName("biography")
    private String biography;
    @SerializedName("place_of_birth")
    private String placeOfBirth;
    @SerializedName("profile_path")
    private String profilePath;

    public Actor(int id, String biography, String birthday, String deathDay, int gender,
                 String name, String placeOfBirth, String profilePath) {
        this.id = id;
        this.birthday = birthday;
        this.deathDay = deathDay;
        this.gender = gender;
        this.name = name;
        this.biography = biography;
        this.placeOfBirth = placeOfBirth;
        this.profilePath = profilePath;
    }

    public Actor() {

    }

    private Actor (Parcel in) {
        id = in.readInt();
        birthday = in.readString();
        deathDay = in.readString();
        gender = in.readInt();
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
        parcel.writeString(deathDay);
        parcel.writeInt(gender);
        parcel.writeString(name);
        parcel.writeString(placeOfBirth);
        parcel.writeString(biography);
        parcel.writeString(profilePath);
    }

    public int getId() { return id; }
    public String getBirthday() { return birthday; }
    public String getDeathDay() { return deathDay; }
    public int getGender() { return gender; }
    public String getActorName() { return name; }
    public String getBiography() { return biography; }
    public String getPlaceOfBirth() { return placeOfBirth; }
    public String getProfilePath() { return profilePath; }


}
