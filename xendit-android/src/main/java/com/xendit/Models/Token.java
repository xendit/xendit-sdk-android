package com.xendit.Models;

/**
 * Created by Sergey on 3/16/17.
 */

public class Token {

    private String id;
    private String status;
    private String authentication_id;
    private AuthenticatedToken authentication;
    private String masked_card_number;
    private Boolean should_3ds;
    private CardMetadata metadata;
    private CardInfo card_info;

    public Token(AuthenticatedToken authentication) {
        this.id = authentication.getId();
        this.status = authentication.getStatus();
        this.authentication_id = authentication.getAuthenticationId();
        this.authentication = authentication;
        this.masked_card_number = authentication.getMaskedCardNumber();
        this.metadata = authentication.getMetadata();
        this.card_info = authentication.getCardInfo();
        this.should_3ds = true;
    }

    public Token(AuthenticatedToken authentication, ThreeDSRecommendation rec) {
        this.id = authentication.getId();
        this.status = authentication.getStatus();
        this.authentication_id = authentication.getAuthenticationId();
        this.authentication = authentication;
        this.masked_card_number = authentication.getMaskedCardNumber();
        this.should_3ds = rec.getShould_3DS();
        this.metadata = authentication.getMetadata();
        this.card_info = authentication.getCardInfo();
    }

    public Token(Authentication authenticatedToken) {
        this.id = authenticatedToken.getCreditCardTokenId();
        this.authentication_id = authenticatedToken.getId();
        this.status = authenticatedToken.getStatus();
        this.should_3ds = true;
        this.masked_card_number = authenticatedToken.getMaskedCardNumber();
        this.metadata = authenticatedToken.getMetadata();
        this.card_info = authenticatedToken.getCardInfo();
    }

    public Token(Authentication authenticatedToken, String tokenId) {
        this.id = tokenId;
        this.authentication_id = authenticatedToken.getId();
        this.status = authenticatedToken.getStatus();
        this.should_3ds = true;
        this.masked_card_number = authenticatedToken.getMaskedCardNumber();
        this.metadata = authenticatedToken.getMetadata();
        this.card_info = authenticatedToken.getCardInfo();
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

    public CardInfo getCardInfo() { return card_info; }

    @Deprecated
    public AuthenticatedToken getAuthentication() {
        return authentication;
    }
}