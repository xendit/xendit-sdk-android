package com.xendit.Models;

import com.xendit.utils.CardValidator;

/**
 * Created by Dimon_GDA on 3/7/17.
 */

public class Card {

    private final String creditCardNumber;
    private final String cardExpirationMonth;
    private final String cardExpirationYear;
    private final String creditCardCVN;
    private final CardHolderData cardHolderData;

    public Card(String creditCardNumber, String cardExpirationMonth, String cardExpirationYear, String creditCardCVN) {
        this.creditCardNumber = CardValidator.cleanCardNumber(creditCardNumber);
        this.cardExpirationMonth = cardExpirationMonth;
        this.cardExpirationYear = cardExpirationYear;
        this.creditCardCVN = CardValidator.cleanCvn(creditCardCVN);
        this.cardHolderData = null;
    }

    public Card(String creditCardNumber, String cardExpirationMonth, String cardExpirationYear, String creditCardCVN, String cardHolderFirstName, String cardHolderLastName, String cardHolderEmail, String cardHolderPhoneNumber) {
        this.creditCardNumber = CardValidator.cleanCardNumber(creditCardNumber);
        this.cardExpirationMonth = cardExpirationMonth;
        this.cardExpirationYear = cardExpirationYear;
        this.creditCardCVN = CardValidator.cleanCvn(creditCardCVN);
        this.cardHolderData = new CardHolderData(cardHolderFirstName, cardHolderLastName, cardHolderEmail, cardHolderPhoneNumber);
    }

    public Card(String creditCardNumber, String cardExpirationMonth, String cardExpirationYear, String creditCardCVN, CardHolderData cardHolderData) {
        this.creditCardNumber = CardValidator.cleanCardNumber(creditCardNumber);
        this.cardExpirationMonth = cardExpirationMonth;
        this.cardExpirationYear = cardExpirationYear;
        this.creditCardCVN = CardValidator.cleanCvn(creditCardCVN);
        this.cardHolderData = cardHolderData;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public String getCardExpirationMonth() {
        return cardExpirationMonth;
    }

    public String getCardExpirationYear() {
        return cardExpirationYear;
    }

    public String getCreditCardCVN() {
        return creditCardCVN;
    }

    public CardHolderData getCardHolder() {
        return cardHolderData;
    }

    public String getCardHolderFirstName() {
        if (cardHolderData != null) {
            return cardHolderData.getFirstName();
        }
        return null;
    }

    public String getCardHolderLastName() {
        if (cardHolderData != null) {
            return cardHolderData.getLastName();
        }
        return null;
    }

    public String getCardHolderEmail() {
        if (cardHolderData != null) {
            return cardHolderData.getEmail();
        }
        return null;
    }

    public String getCardHolderPhoneNumber() {
        if (cardHolderData != null) {
            return cardHolderData.getPhoneNumber();
        }
        return null;
    }
}