package model;

import com.mysql.cj.jdbc.Blob;
import java.io.Serializable;
import java.sql.Date;

public class InfoUserLogged implements Serializable {
    private int id;
    private String email, phone, firstName, lastName, password;
    transient Blob avatar;
    private Date birthDay;
    private int gender;

    public InfoUserLogged() {

    };

    public InfoUserLogged(int id, String email, String phone, String firstName, String lastName, String password, Blob avatar, Date birthDay, int gender) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.avatar = avatar;
        this.birthDay = birthDay;
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public Blob getAvatar() {
        return avatar;
    }

    public void setAvatar(Blob avatar) {
        this.avatar = avatar;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getBirthday() {
        return birthDay;
    }

    public void setBirthday(Date birthday) {
        this.birthDay = birthday;
    }
}
