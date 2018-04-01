package com.future.bestmovies.data;

public class Actor {
    private final int id;
    private final String birthday;
    private final String deathday;
    private final int gender;
    private final String name;
    private final String biography;
    private final String placeOfBirth;
    private final String profilePath;

    public Actor (int id, String birthday, String deathday, int gender, String name, String biography, String placeOfBirth, String profilePath) {
        this.id = id;
        this.birthday = birthday;
        this.deathday = deathday;
        this.gender = gender;
        this.name = name;
        this.biography = biography;
        this.placeOfBirth = placeOfBirth;
        this.profilePath = profilePath;
    }

    public int getActorId() { return id; }
    public String getBirthday() { return birthday; }
    public String getDeathday() { return deathday; }
    public String getGender() {
        if (gender == 1)
            return "Female";
        else
            return "Male";
    }
    public String getActorName() { return name; }
    public String getBiography() { return biography; }
    public String getPlaceOfBirth() { return placeOfBirth; }
    public String getProfilePath() { return profilePath; }

}
