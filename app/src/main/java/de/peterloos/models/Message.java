package de.peterloos.models;

/**
 * Created by Peter on 17.12.2016.
 */

public class Message {

    private String uid;
    private String displayName;
    private String photoUrl;
    private String text;

    public Message() {

        // default constructor required for calls to DataSnapshot.getValue(User.class)
        this.uid = "";
        this.displayName = "";
        this.text = "";
        this.photoUrl = "";
    }

    public Message(String uid, String displayName, String text, String uri) {
        this.uid = uid;
        this.displayName = displayName;
        this.text = text;
        this.photoUrl = uri;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public void setPhotoUrl(String url) {
        this.photoUrl = url;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString () {
        return String.format("Message: Text: %s - DisplayName: %s", this.text, this.displayName);
    }
}
