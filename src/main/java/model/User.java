package model;

import com.mysql.cj.jdbc.Blob;
import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String name;
    transient Blob avatar;
    private Status status;
    private int gender;
    private String fullName;
    private String email;
    private String phone;

    public User() {

    }
    public User(int id, String name, Blob avatar, int gender) {
        this.id = id;
        this.fullName = name;
        this.avatar = avatar;
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Blob getAvatar() {
        return avatar;
    }

    public void setAvatar(Blob avatar) {
        this.avatar = avatar;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
}
