package com.xendit.utils;

import org.junit.Test;

import java.util.Calendar;

import static com.google.common.truth.Truth.assertThat;


/**
 * Created by gonzalez on 8/22/17.
 */

public class CardValidatorTests {

    private static String VALID_CARD_NUMBER = "4012111111111111";

    @Test
    public void isCardNumberValid_shouldTrimCardNumber() {
        assertThat(CardValidator.isCardNumberValid("   " + VALID_CARD_NUMBER + "     ")).isTrue();
    }

    @Test
    public void isCardNumberValid_shouldOnlyAllowNumericCharacters() {
        assertThat(CardValidator.isCardNumberValid(VALID_CARD_NUMBER + "Z")).isFalse();
    }

    @Test
    public void isCardNumberValid_shouldAllowValidCardNumber() {
        assertThat(CardValidator.isCardNumberValid(VALID_CARD_NUMBER)).isTrue();
    }

    @Test
    public void isCardNumberValid_shouldPassMasterCard() {
        assertThat(CardValidator.isCardNumberValid("5555555555554444")).isTrue();
        assertThat(CardValidator.isCardNumberValid("5105105105105100")).isTrue();
        assertThat(CardValidator.isCardNumberValid("5213724373543245")).isTrue();
        assertThat(CardValidator.isCardNumberValid("5513799778027330")).isTrue();
        assertThat(CardValidator.isCardNumberValid("5404666842103888")).isTrue();
    }

    @Test
    public void isCardNumberValid_shouldPassVisa() {
        assertThat(CardValidator.isCardNumberValid("4000000000000002")).isTrue();
        assertThat(CardValidator.isCardNumberValid("4111111111111111")).isTrue();
        assertThat(CardValidator.isCardNumberValid("4012888888881881")).isTrue();
        assertThat(CardValidator.isCardNumberValid("4222222222222")).isTrue();
        assertThat(CardValidator.isCardNumberValid("4929950253805473")).isTrue();
        assertThat(CardValidator.isCardNumberValid("4916154524086329")).isTrue();
        assertThat(CardValidator.isCardNumberValid("4556564166261021533")).isTrue();
    }

    @Test
    public void isCardNumberValid_shouldPassJCB() {
        assertThat(CardValidator.isCardNumberValid("3530111333300000")).isTrue();
        assertThat(CardValidator.isCardNumberValid("3566002020360505")).isTrue();
    }

    @Test
    public void isCardNumberValid_shouldPassDiscover() {
        assertThat(CardValidator.isCardNumberValid("6011111111111117")).isTrue();
        assertThat(CardValidator.isCardNumberValid("6011000990139424")).isTrue();
    }

    @Test
    public void isCardNumberValid_shouldPassAMEX() {
        assertThat(CardValidator.isCardNumberValid("378282246310005")).isTrue();
        assertThat(CardValidator.isCardNumberValid("371449635398431")).isTrue();
        assertThat(CardValidator.isCardNumberValid("378734493671000")).isTrue();
    }

    @Test
    public void isCardNumberValid_shouldPassValidNumbersWithSpaces() {
        assertThat(CardValidator.isCardNumberValid("4111 1111 1111 1111")).isTrue();
        assertThat(CardValidator.isCardNumberValid("4012 8888 8888 1881")).isTrue();
        assertThat(CardValidator.isCardNumberValid("3782 822463 10005")).isTrue();
    }

    @Test
    public void isCardNumberValid_shouldNotPassNumbersThatDontPassLuhnTest() {
        assertThat(CardValidator.isCardNumberValid("4000000000000001")).isFalse();
        assertThat(CardValidator.isCardNumberValid("4111111111111112")).isFalse();
    }

    @Test
    public void isCardNumberValid_shouldNotPassNumbersWithInvalidLength() {
        assertThat(CardValidator.isCardNumberValid("40000000002")).isFalse();
        assertThat(CardValidator.isCardNumberValid("40000000000000000002")).isFalse();
    }

    @Test
    public void isCardNumberValid_shouldNotAllowNullCardNumber() {
        assertThat(CardValidator.isCardNumberValid(null)).isFalse();
    }

    @Test
    public void isExpiryValid_shouldNotPassNonNumericValues() {
        assertThat(CardValidator.isExpiryValid("A", "2020")).isFalse();
        assertThat(CardValidator.isExpiryValid("12", "B")).isFalse();
    }

    @Test
    public void isExpiryValid_shouldNotPassInvalidMonths() {
        assertThat(CardValidator.isExpiryValid("00",
                Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 2))).isFalse();
        assertThat(CardValidator.isExpiryValid("-1",
                Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 2))).isFalse();
        assertThat(CardValidator.isExpiryValid("13",
                Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 2))).isFalse();
    }

    @Test
    public void isExpiryValid_shouldPassValidMonthAndYear() {
        assertThat(CardValidator.isExpiryValid("12", Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 2))).isTrue();
        assertThat(CardValidator.isExpiryValid("01", Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 2))).isTrue();
        assertThat(CardValidator.isExpiryValid("1", Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 2))).isTrue();
    }

    @Test
    public void isExpiryValid_shouldTrimMonthAndYear() {
        String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 2);
        assertThat(CardValidator.isExpiryValid("  12  ", "   " + year + "     ")).isTrue();
    }

    @Test
    public void isExpiryValid_shouldNotAllowNullValues() {
        assertThat(CardValidator.isExpiryValid(null, Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 2))).isFalse();
        assertThat(CardValidator.isExpiryValid("12", null)).isFalse();
    }

    @Test
    public void isCvnValid_shouldNotPassEmptyString() {
        assertThat(CardValidator.isCvnValid("")).isFalse();
    }

    @Test
    public void isCvnValid_shouldNotPassCVNThatIsTooLong() {
        assertThat(CardValidator.isCvnValid("12345")).isFalse();
    }

    @Test
    public void isCvnValid_shouldNotPassCVNThatIsTooShort() {
        assertThat(CardValidator.isCvnValid("12")).isFalse();
    }

    @Test
    public void isCvnValid_shouldAllowZeroValueCVN() {
        assertThat(CardValidator.isCvnValid("000")).isTrue();
    }

    @Test
    public void isCvnValid_shouldPassShortCVN() {
        assertThat(CardValidator.isCvnValid("123")).isTrue();
    }

    @Test
    public void isCvnValid_shouldNotPassLongCVN() {
        assertThat(CardValidator.isCvnValid("1234")).isTrue();
    }

    @Test
    public void isCvnValid_shouldTrimCVN() {
        assertThat(CardValidator.isCvnValid("  123  ")).isTrue();
    }

    @Test
    public void cleanCardNumber_shouldTrimAndRemoveSpaces() {
        assertThat(CardValidator.cleanCardNumber("     1234 1234 1234 1234     ")).isEqualTo("1234123412341234");
    }

    @Test
    public void cleanCardNumber_shouldReturnNullIfCardNumberIsNull() {
        assertThat(CardValidator.cleanCardNumber(null)).isEqualTo(null);
    }

    @Test
    public void cleanCvn_shouldTrimAndRemoveSpaces() {
        assertThat(CardValidator.cleanCvn("  1 2 3  ")).isEqualTo("123");
    }

    @Test
    public void cleanCvn_shouldReturnNullIfCardNumberIsNull() {
        assertThat(CardValidator.cleanCvn(null)).isEqualTo(null);
    }

    @Test
    public void getCardType_shouldHandleAmex() {
        assertThat(CardValidator.getCardType("378282246310005")).isEqualTo(CardValidator.CardType.AMEX);
        assertThat(CardValidator.getCardType("371449635398431")).isEqualTo(CardValidator.CardType.AMEX);
        assertThat(CardValidator.getCardType("378734493671000")).isEqualTo(CardValidator.CardType.AMEX);
    }

    @Test
    public void getCardType_shouldHandleDiscover() {
        assertThat(CardValidator.getCardType("6011111111111117")).isEqualTo(CardValidator.CardType.DISCOVER);
        assertThat(CardValidator.getCardType("6011000990139424")).isEqualTo(CardValidator.CardType.DISCOVER);
    }

    @Test
    public void getCardType_shouldHandleJCB() {
        assertThat(CardValidator.getCardType("3530111333300000")).isEqualTo(CardValidator.CardType.JCB);
        assertThat(CardValidator.getCardType("3566002020360505")).isEqualTo(CardValidator.CardType.JCB);
    }

    @Test
    public void getCardType_shouldHandleMastercard() {
        assertThat(CardValidator.getCardType("5555555555554444")).isEqualTo(CardValidator.CardType.MASTERCARD);
        assertThat(CardValidator.getCardType("5105105105105100")).isEqualTo(CardValidator.CardType.MASTERCARD);
    }

    @Test
    public void getCardType_shouldHandleVisa() {
        assertThat(CardValidator.getCardType("4111111111111111")).isEqualTo(CardValidator.CardType.VISA);
        assertThat(CardValidator.getCardType("4012888888881881")).isEqualTo(CardValidator.CardType.VISA);
        assertThat(CardValidator.getCardType("4222222222222")).isEqualTo(CardValidator.CardType.VISA);
    }

    @Test
    public void getCardType_shouldHandleDankort() {
        assertThat(CardValidator.getCardType("5019717010103742")).isEqualTo(CardValidator.CardType.DANKORT);
    }

    @Test
    public void getCardType_shouldHandleUnknownCardType() {
        assertThat(CardValidator.getCardType("000000000000")).isEqualTo(CardValidator.CardType.OTHER);
    }

    @Test
    public void isCvnValidForCardType_shouldHandleAMEX() {
        assertThat(CardValidator.isCvnValidForCardType("1234", "378282246310005")).isTrue();
        assertThat(CardValidator.isCvnValidForCardType("123", "378282246310005")).isFalse();
    }

    @Test
    public void isCvnValidForCardType_shouldHandleNonAMEX() {
        assertThat(CardValidator.isCvnValidForCardType("123", "4012888888881881")).isTrue();
        assertThat(CardValidator.isCvnValidForCardType("1234", "4012888888881881")).isFalse();
    }
}
