package com.example.logtest.controller;


import com.example.logtest.model.INC_USERS;
import com.example.logtest.model.Users;

public class AuthResponse {

    private String token;
    private INC_USERS userDetails;

    public AuthResponse() {
    }

    public AuthResponse(String token, INC_USERS userDetails) {
        this.token = token;
        this.userDetails = userDetails;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public INC_USERS getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(INC_USERS userDetails) {
        this.userDetails = userDetails;
    }
}