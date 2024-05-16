package com.example.demo.model;


import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class manager {

    @Id
    private long id;
    private String username;
    private String password;
    private String mobNumber;

    // Constructors, getters, setters, and other methods

    // Constructors (if needed)
    public manager() {
    }

    public manager(String username, String password, String mobNumber) {
        this.username = username;
        this.password = password;
        this.mobNumber = mobNumber;
    }

    // Getters and setters
    // Override toString() for debugging or logging

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobNumber() {
        return mobNumber;
    }

    public void setMobNumber(String mobNumber) {
        this.mobNumber = mobNumber;
    }

    // Override toString() for debugging or logging
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", mobNumber='" + mobNumber + '\'' +
                '}';
    }
}
