package de.peterloos.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Peter on 10.12.2016.
 */

@IgnoreExtraProperties
public class User {

    private String email;
    private String displayName;
    private String photoUrl;

    public User () {
        // default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User (String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = "";
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public String toString () {
        return String.format("User: %s - DisplayName: %s", this.email, this.displayName);
    }
}
