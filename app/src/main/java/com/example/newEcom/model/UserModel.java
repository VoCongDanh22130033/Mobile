package com.example.newEcom.model;



public class UserModel {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String profileImage;

    public UserModel() {
        // Required empty constructor for Firebase
    }

    public UserModel(String uid, String name, String email, String phone, String profileImage) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profileImage = profileImage;
    }

    // Getters & Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
