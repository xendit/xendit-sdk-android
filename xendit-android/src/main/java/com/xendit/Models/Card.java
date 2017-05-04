package com.xendit.Models;

/**
 * Created by Dimon_GDA on 3/7/17.
 */

public class Card {

    private String creditCardNumber;
    private String cardExpirationMonth;
    private String cardExpirationYear;
    private String creditCardCVN;


    public Card(String creditCardNumber, String cardExpirationMonth, String cardExpirationYear, String creditCardCVN) {
        this.creditCardNumber = creditCardNumber;
        this.cardExpirationMonth = cardExpirationMonth;
        this.cardExpirationYear = cardExpirationYear;
        this.creditCardCVN = creditCardCVN;
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
}