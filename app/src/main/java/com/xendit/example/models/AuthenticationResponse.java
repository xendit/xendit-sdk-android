package com.xendit.example.models;

import androidx.annotation.Keep;

import com.xendit.Models.Authentication;
import com.xendit.Models.CardInfo;

@Keep
public class AuthenticationResponse {
    private String id;
    private String credit_card_token_id;
    private String status;
    private String masked_card_number;
    private CardInfo card_info;

    public AuthenticationResponse(Authentication authentication) {
        id = authentication.getId();
        credit_card_token_id = authentication.getCreditCardTokenId();
        status = authentication.getStatus();
        masked_card_number = authentication.getMaskedCardNumber();
        card_info = authentication.getCardInfo();
    }
}