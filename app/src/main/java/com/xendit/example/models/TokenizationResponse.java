package com.xendit.example.models;

import androidx.annotation.Keep;

import com.xendit.Models.CardInfo;
import com.xendit.Models.Token;

@Keep
public class TokenizationResponse {
    private String id;
    private String authentication_id;
    private String status;
    private String masked_card_number;
    private boolean should_3ds;
    private CardInfo card_info;

    public TokenizationResponse(Token token) {
        id = token.getId();
        authentication_id = token.getAuthenticationId();
        status = token.getStatus();
        masked_card_number = token.getMaskedCardNumber();
        should_3ds = token.getShould_3DS();
        card_info = token.getCardInfo();
    }
}