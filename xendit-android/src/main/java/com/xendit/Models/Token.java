package com.xendit.Models;

/**
 * Created by Sergey on 3/16/17.
 */

public class Token {

    private String id;
    private String status;
    private String authentication_id;
    private Authentication authentication;
    private String masked_card_number;
    private Boolean should_3ds;
    private CardMetadata metadata;

    public Token(Authentication authentication) {
        this.id = authentication.getId();
        this.status = authentication.getStatus();
        this.authentication_id = authentication.getAuthenticationId();
        this.authentication = authentication;
        this.masked_card_number = authentication.getMaskedCardNumber();
        this.metadata = authentication.getMetadata();
        this.should_3ds = true;
    }

    public Token(Authentication authentication, ThreeDSRecommendation rec) {
        this.id = authentication.getId();
        this.status = authentication.getStatus();
        this.authentication_id = authentication.getAuthenticationId();
        this.authentication = authentication;
        this.masked_card_number = authentication.getMaskedCardNumber();
        this.should_3ds = rec.getShould_3DS();
        this.metadata = authentication.getMetadata();
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getAuthenticationId() { return authentication_id; }

    public String getMaskedCardNumber() { return masked_card_number; }

    public Boolean getShould_3DS() { return should_3ds; }

    public CardMetadata getMetadata() { return metadata; }

    @Deprecated
    public Authentication getAuthentication() {
        return authentication;
    }
}