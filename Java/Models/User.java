package pk.edu.itu.bsai23023.chatring.Models;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String name;
    private String phoneNumber;
    private String profileImage;
    private long weirdId = -11;

    public User() {}

    public long getWeirdId() {
        return weirdId;
    }

    public void setWeirdId(long weirdId) {
        this.weirdId = weirdId;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String token;

    public User(String uid, String name, String phoneNumber, String profileImage) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}