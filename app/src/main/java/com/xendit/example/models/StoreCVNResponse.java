package com.xendit.example.models;

import androidx.annotation.Keep;

import com.xendit.Models.CardInfo;
import com.xendit.Models.Token;

@Keep
public class StoreCVNResponse {
    private String id;
    private String authentication_id;
    private String status;
    private String masked_card_number;
    private CardInfo card_info;
    private String failure_reason;

    public StoreCVNResponse(Token token) {
        id = token.getId();
        authentication_id = token.getAuthenticationId();
        status = token.getStatus();
        masked_card_number = token.getMaskedCardNumber();
        card_info = token.getCardInfo();
        failure_reason = token.getFailureReason();
    }
}