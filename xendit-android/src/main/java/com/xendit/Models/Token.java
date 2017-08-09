package com.xendit.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sergey on 3/16/17.
 */

public class Token {

    private String id;
    private String status;
    private String authentication_id;
    private Authentication authentication;

    public Token(Authentication authentication) {
        this.id = authentication.getId();
        this.status = authentication.getStatus();
        this.authentication_id = authentication.getAuthenticationId();
        this.authentication = authentication;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getAuthenticationId() { return authentication_id; }

    @Deprecated
    public Authentication getAuthentication() {
        return authentication;
    }
}