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
    private CardInfo card_info;
    private String failure_reason;

    public Token(AuthenticatedToken authentication) {
        this.id = authentication.getId();
        this.status = authentication.getStatus();
        this.authentication_id = authentication.getAuthenticationId();
        this.authentication = authentication;
        this.masked_card_number = authentication.getMaskedCardNumber();
        this.card_info = authentication.getCardInfo();
        this.should_3ds = true;
        this.failure_reason = authentication.getFailureReason();
    }

    public Token(AuthenticatedToken authentication, ThreeDSRecommendation rec) {
        this.id = authentication.getId();
        this.status = authentication.getStatus();
        this.authentication_id = authentication.getAuthenticationId();
        this.authentication = authentication;
        this.masked_card_number = authentication.getMaskedCardNumber();
        this.should_3ds = rec.getShould_3DS();
        this.card_info = authentication.getCardInfo();
        this.failure_reason = authentication.getFailureReason();
    }

    public Token(Authentication authenticatedToken) {
        this.id = authenticatedToken.getCreditCardTokenId();
        this.authentication_id = authenticatedToken.getId();
        this.status = authenticatedToken.getStatus();
        this.should_3ds = true;
        this.masked_card_number = authenticatedToken.getMaskedCardNumber();
        this.card_info = authenticatedToken.getCardInfo();
        this.failure_reason = authenticatedToken.getFailureReason();
    }

    public Token(Authentication authenticatedToken, String tokenId) {
        this.id = tokenId;
        this.authentication_id = authenticatedToken.getId();
        this.status = authenticatedToken.getStatus();
        this.should_3ds = true;
        this.masked_card_number = authenticatedToken.getMaskedCardNumber();
        this.card_info = authenticatedToken.getCardInfo();
        this.failure_reason = authenticatedToken.getFailureReason();
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

    public CardInfo getCardInfo() { return card_info; }

    public String getFailureReason() { return failure_reason; }

    @Deprecated
    public AuthenticatedToken getAuthentication() {
        return authentication;
    }
}