package com.xendit.utils;

import android.support.annotation.NonNull;

import com.hypertrack.hyperlog.HyperLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by gonzalez on 8/22/17.
 */

public class CardValidator {

    private static final String TAG = "CardValidator";

    public enum CardType {
        VISA("VISA", "001"),
        MASTERCARD("MASTERCARD", "002"),
        AMEX("AMEX", "003"),
        DISCOVER("DISCOVER", "004"),
        JCB("JCB", "007"),
        VISA_ELECTRON("VISA_ELECTRON", "033"),
        DANKORT("DANKORT", "034"),
        MAESTRO("MAESTRO", "042"),
        OTHER("OTHER", null);

        private String cardName;
        private String cardKey;

        CardType(String cardName, String cardKey) {
            this.cardName = cardName;
            this.cardKey = cardKey;
        }

        @NonNull
        public String toString() {
            return cardName;
        }

        public String getCardTypeKey() {
            return cardKey;
        }
    }


    /**
     * Determines whether the credit card number provided is valid
     *
     * @param  cardNumber A credit card number
     * @return true if the credit card number is valid, false otherwise
     */
    public static boolean isCardNumberValid(String cardNumber) {
        HyperLog.d(TAG,"isCardNumberValid");
        if (cardNumber == null) {
            return false;
        }

        String cleanCardNumber = cleanCardNumber(cardNumber);
        CardType cardType = getCardType(cleanCardNumber);

        return cleanCardNumber.length() >= 12 &&
                cleanCardNumber.length() <= 19 &&
                isNumeric(cleanCardNumber) &&
                isValidLuhnNumber(cleanCardNumber) &&
                cardType != null && !cardType.equals(CardType.OTHER);
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

        String cleanMonth = removeWhitespace(expirationMonth);
        String cleanYear = removeWhitespace(expirationYear);

        return isNumeric(cleanMonth) && isNumeric(cleanYear) &&
                parseNumberSafely(cleanMonth) >= 1 &&
                parseNumberSafely(cleanMonth) <= 12 &&
                parseNumberSafely(cleanYear) >= 2017 &&
                parseNumberSafely(cleanYear) <= 2100 &&
                isNotInThePast(cleanMonth, cleanYear);
    }

    /**
     * Determines whether the card CVN length is valid
     *
     * @param  cardCVN The credit card CVN
     * @param  cardNumber The credit card number
     * @return true if the cvn length is valid for this card type, false otherwise
     */
    public static boolean isCvnValidForCardType (String cardCVN, String cardNumber) {
        if (cardCVN == null || cardNumber == null) {
            return false;
        }

        String cleanCvn = cleanCvn(cardCVN);
        String cleanCardNumber = cleanCardNumber(cardNumber);

        if (isNumeric(cleanCvn) && Integer.parseInt(cleanCvn) >= 0) {
            return isCardAmex(cleanCardNumber) ? cleanCvn.length() == 4 : cleanCvn.length() == 3;
        }

        return false;
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

        String cleanCvn = cleanCvn(cardCVN);

        return isNumeric(cleanCvn)
                && Integer.parseInt(cleanCvn) >= 0
                && cleanCvn.length() >= 3
                && cleanCvn.length() <= 4;
    }

    /**
     * Removes whitespaces from credit card number
     *
     * @param  cardNumber The credit card number
     * @return Returns cardNumber without whitepaces
     */
    public static String cleanCardNumber(String cardNumber) {
        return removeWhitespace(cardNumber);
    }

    /**
     * Removes whitespaces from cvn
     *
     * @param  cardCvn The credit card number
     * @return Returns cardCvn without whitepaces
     */
    public static String cleanCvn(String cardCvn) {
        return removeWhitespace(cardCvn);
    }

    /**
     * Computes the card type based on the card number
     *
     * @param  cardNumber The credit card number
     * @return CardType The card type, e.g. VISA
     */
    public static CardType getCardType(String cardNumber) {
        HyperLog.i(TAG,"getCardType");
        String cleanCardNumber = cleanCardNumber(cardNumber);

        if (cleanCardNumber == null) {
            return null;
        } else if (cleanCardNumber.indexOf("4") == 0) {
            if (isCardVisaElectron(cleanCardNumber)) {
                HyperLog.i(TAG,"getCardType return VISA_ELECTRON");
                return CardType.VISA_ELECTRON;
            } else {
                HyperLog.i(TAG,"getCardType return VISA");
                return CardType.VISA;
            }
        } else if (isCardAmex(cleanCardNumber)) {
            HyperLog.i(TAG,"getCardType return AMEX");
            return CardType.AMEX;
        } else if (isCardMastercard(cleanCardNumber)) {
            HyperLog.i(TAG,"getCardType return MASTERCARD");
            return CardType.MASTERCARD;
        } else if (isCardDiscover(cleanCardNumber)) {
            HyperLog.i(TAG,"getCardType return DISCOVER");
            return CardType.DISCOVER;
        } else if (isCardJCB(cleanCardNumber)) {
            HyperLog.i(TAG,"getCardType return JCB");
            return CardType.JCB;
        } else if (isCardDankort(cleanCardNumber)) {
            HyperLog.i(TAG,"getCardType return DANKORT");
            return CardType.DANKORT;
        } else if (isCardMaestro(cleanCardNumber)) {
            HyperLog.i(TAG,"getCardType return MAESTRO");
            return CardType.MAESTRO;
        } else {
            HyperLog.i(TAG,"getCardType return OTHER");
            return CardType.OTHER;
        }
    }

    private static String removeWhitespace(String str) {
        if (str == null) {
            return null;
        }

        return str.replaceAll("\\s", "");
    }

    private static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }

        return str.matches("[0-9]+");
    }

    private static boolean isValidLuhnNumber(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));

            if (alternate) {
                n *= 2;

                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    private static boolean isNotInThePast(String expirationMonth, String expirationYear) {
        DateFormat monthFormat = new SimpleDateFormat("MM", Locale.US);
        Date now = new Date();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = parseNumberSafely(monthFormat.format(now));

        int expMonth = parseNumberSafely(expirationMonth);
        int expYear = parseNumberSafely(expirationYear);

        return (expYear == currentYear && expMonth >= currentMonth) || expYear > currentYear;

    }

    private static int parseNumberSafely(String numberStr) {
        int number = -1;

        if (numberStr != null) {
            try {
                number = Integer.parseInt(numberStr);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return number;
    }

    private static boolean isCardAmex(String cardNumber) {
        return cardNumber != null && (cardNumber.indexOf("34") == 0 || cardNumber.indexOf("37") == 0);
    }

    private static boolean isCardMastercard(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 2) {
            int startingNumber = number(cardNumber.substring(0, 2));
            return startingNumber >= 51 && startingNumber <= 55;
        } else {
            return false;
        }
    }

    private static boolean isCardDiscover(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 6) {
            int firstStartingNumber = number(cardNumber.substring(0, 3));
            int secondStartingNumber = number(cardNumber.substring(0, 6));
            return (firstStartingNumber >= 644 && firstStartingNumber <= 649)
                    || (secondStartingNumber >= 622126 && secondStartingNumber <= 622925)
                    || cardNumber.indexOf("65") == 0
                    || cardNumber.indexOf("6011") == 0;
        } else {
            return false;
        }
    }

    private static boolean isCardMaestro(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 2) {
            int startingNumber = number(cardNumber.substring(0, 2));
            return startingNumber == 50
                    || (startingNumber >= 56 && startingNumber <= 64)
                    || (startingNumber >= 66 && startingNumber <= 69);
        }

        return false;
    }

    private static boolean isCardDankort(String cardNumber) {
        return cardNumber != null && cardNumber.indexOf("5019") == 0;
    }

    private static boolean isCardJCB(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 4) {
            int startingNumber = number(cardNumber.substring(0, 4));
            return startingNumber >= 3528 && startingNumber <= 3589;
        } else {
            return false;
        }
    }

    private static boolean isCardVisaElectron(String cardNumber) {
        return cardNumber != null && (cardNumber.indexOf("4026") == 0
                || cardNumber.indexOf("417500") == 0
                || cardNumber.indexOf("4405") == 0
                || cardNumber.indexOf("4508") == 0
                || cardNumber.indexOf("4844") == 0
                || cardNumber.indexOf("4913") == 0
                || cardNumber.indexOf("4917") == 0);
    }

    private static int number(String sNumber) {
        int number = -1;

        if (sNumber != null) {
            try {
                number = Integer.parseInt(sNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return number;
    }
}
