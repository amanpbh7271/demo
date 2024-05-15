package com.example.demo.controller;

import com.example.demo.model.Users;

public class AuthResponse {

    private String token;
    private Users userDetails;

    public AuthResponse() {
    }

    public AuthResponse(String token, Users userDetails) {
        this.token = token;
        this.userDetails = userDetails;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Users getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(Users userDetails) {
        this.userDetails = userDetails;
    }
}
