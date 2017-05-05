package com.xendit.Models;

/**
 * Created by Sergey on 3/16/17.
 */

public class Token {

    private Authentication authentication;

    public Token(Authentication authentication) {
        this.authentication = authentication;
    }

    public Authentication getAuthentication() {
        return authentication;
    }
}