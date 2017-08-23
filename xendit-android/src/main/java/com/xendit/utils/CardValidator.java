package com.xendit.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by gonzalez on 8/22/17.
 */

public class CardValidator {

    /**
     * Determines whether the credit card number provided is valid
     *
     * @param  cardNumber A credit card number
     * @return true if the credit card number is valid, false otherwise
     */
    public static boolean isCardNumberValid(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }

        String normalizedCardNumber = cardNumber.replaceAll("\\s", "");

        return normalizedCardNumber.length() >= 12 &&
                normalizedCardNumber.length() <= 19 &&
                isNumeric(normalizedCardNumber) &&
                isValidLuhnNumber(normalizedCardNumber);
    }

    /**
     * Determines whether the card expiration month and year are valid
     *
     * @param  expirationMonth The month a card expired represented by digits (e.g. 12)
     * @param  expirationYear The year a card expires represented by digits (e.g. 2026)
     * @return true if both the expiration month and year are valid
     */
    public static boolean isExpiryValid(String expirationMonth, String expirationYear) {
        if (expirationMonth == null || expirationYear == null) {
            return false;
        }

        String normalizedMonth = expirationMonth.replaceAll("\\s", "");
        String normalizedYear = expirationYear.replaceAll("\\s", "");

        return isNumeric(normalizedMonth) && isNumeric(normalizedYear) &&
                parseNumberSafely(normalizedMonth) >= 1 &&
                parseNumberSafely(normalizedMonth) <= 12 &&
                parseNumberSafely(normalizedYear) >= 2017 &&
                parseNumberSafely(normalizedYear) <= 2100 &&
                isNotInThePast(normalizedMonth, normalizedYear);
    }

    /**
     * Determines whether the card CVN is valid
     *
     * @param  cardCVN The credit card CVN
     * @return true if the cvn is valid, false otherwise
     */
    public static boolean isCvnValid(String cardCVN) {
        if (cardCVN == null) {
            return false;
        }

        String normalizedCvn = cardCVN.replaceAll("\\s", "");

        return isNumeric(normalizedCvn)
                && Integer.parseInt(normalizedCvn) >= 0
                && normalizedCvn.length() >= 3
                && normalizedCvn.length() <= 4;
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private static boolean isValidLuhnNumber(String cardNumber) {
        String numberSequence = new StringBuilder(cardNumber).reverse().toString();

        int total = 0;

        for (int i = 0; i < numberSequence.length(); i++) {
            int num = Integer.parseInt(numberSequence.substring(i, i + 1));

            if (i % 2 == 0) {
                total += num;
            } else {
                int numDouble = num * 2;

                switch (numDouble) {
                    case 10: total += 1; break;
                    case 12: total += 3; break;
                    case 14: total += 5; break;
                    case 16: total += 7; break;
                    case 18: total += 9; break;
                    default: total += numDouble; break;
                }
            }
        }

        return (total % 10) == 0;
    }

    private static boolean isNotInThePast(String expirationMonth, String expirationYear) {
        DateFormat monthFormat = new SimpleDateFormat("MM", Locale.US);
        Date now = new Date();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = parseNumberSafely(monthFormat.format(now));

        int expMonth = parseNumberSafely(expirationMonth);
        int expYear = parseNumberSafely(expirationYear);

        if ((expYear == currentYear && expMonth >= currentMonth) || expYear > currentYear) {
            return true;
        }

        return false;
    }

    private static int parseNumberSafely(String numberStr) {
        int number = -1;

        if (numberStr != null) {
            try {
                number = Integer.parseInt(numberStr);
            }
            catch (Exception e) {}
        }

        return number;
    }
}
