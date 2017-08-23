package com.xendit.utils;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by gonzalez on 8/22/17.
 */

public class CardValidatorTests {

    private static String VALID_CARD_NUMBER = "4012111111111111";

    @Test
    public void isCardNumberValid_shouldTrimCardNumber() {
        Assert.assertTrue(CardValidator.isCardNumberValid("   " + VALID_CARD_NUMBER + "     "));
    }

    @Test
    public void isCardNumberValid_shouldOnlyAllowNumericCharacters() {
        Assert.assertFalse(CardValidator.isCardNumberValid(VALID_CARD_NUMBER + "Z"));
    }

    @Test
    public void isCardNumberValid_shouldAllowValidCardNumber() {
        Assert.assertTrue(CardValidator.isCardNumberValid(VALID_CARD_NUMBER));
    }

    @Test
    public void isCardNumberValid_shouldPassMasterCard() {
        Assert.assertTrue(CardValidator.isCardNumberValid("5555555555554444"));
        Assert.assertTrue(CardValidator.isCardNumberValid("5105105105105100"));
    }

    @Test
    public void isCardNumberValid_shouldPassVisa() {
        Assert.assertTrue(CardValidator.isCardNumberValid("4000000000000002"));
        Assert.assertTrue(CardValidator.isCardNumberValid("4111111111111111"));
        Assert.assertTrue(CardValidator.isCardNumberValid("4012888888881881"));
        Assert.assertTrue(CardValidator.isCardNumberValid("4222222222222"));
    }

    @Test
    public void isCardNumberValid_shouldPassJCB() {
        Assert.assertTrue(CardValidator.isCardNumberValid("3530111333300000"));
        Assert.assertTrue(CardValidator.isCardNumberValid("3566002020360505"));
    }

    @Test
    public void isCardNumberValid_shouldPassDiscover() {
        Assert.assertTrue(CardValidator.isCardNumberValid("6011111111111117"));
        Assert.assertTrue(CardValidator.isCardNumberValid("6011000990139424"));
    }

    @Test
    public void isCardNumberValid_shouldPassDinersClub() {
        Assert.assertTrue(CardValidator.isCardNumberValid("30569309025904"));
        Assert.assertTrue(CardValidator.isCardNumberValid("38520000023237"));
    }

    @Test
    public void isCardNumberValid_shouldPassAMEX() {
        Assert.assertTrue(CardValidator.isCardNumberValid("378282246310005"));
        Assert.assertTrue(CardValidator.isCardNumberValid("371449635398431"));
        Assert.assertTrue(CardValidator.isCardNumberValid("378734493671000"));
    }

    @Test
    public void isCardNumberValid_shouldPassValidNumbersWithSpaces() {
        Assert.assertTrue(CardValidator.isCardNumberValid("4111 1111 1111 1111"));
        Assert.assertTrue(CardValidator.isCardNumberValid("4012 8888 8888 1881"));
        Assert.assertTrue(CardValidator.isCardNumberValid("3782 822463 10005"));
    }

    @Test
    public void isCardNumberValid_shouldNotPassNumbersThatDontPassLuhnTest() {
        Assert.assertFalse(CardValidator.isCardNumberValid("4000000000000001"));
        Assert.assertFalse(CardValidator.isCardNumberValid("4111111111111112"));
    }

    @Test
    public void isCardNumberValid_shouldNotPassNumbersWithInvalidLength() {
        Assert.assertFalse(CardValidator.isCardNumberValid("40000000002"));
        Assert.assertFalse(CardValidator.isCardNumberValid("40000000000000000002"));
    }

    @Test
    public void isCardNumberValid_shouldNotAllowNullCardNumber() {
        Assert.assertFalse(CardValidator.isCardNumberValid(null));
    }

    @Test
    public void isExpiryValid_shouldNotPassNonNumericValues() {
        Assert.assertFalse(CardValidator.isExpiryValid("A", "2020"));
        Assert.assertFalse(CardValidator.isExpiryValid("12", "B"));
    }

    @Test
    public void isExpiryValid_shouldNotPassInvalidMonths() {
        Assert.assertFalse(CardValidator.isExpiryValid("00", "2020"));
        Assert.assertFalse(CardValidator.isExpiryValid("-1", "2020"));
        Assert.assertFalse(CardValidator.isExpiryValid("13", "2020"));
    }

    @Test
    public void isExpiryValid_shouldPassValidMonthAndYear() {
        Assert.assertTrue(CardValidator.isExpiryValid("12", "2020"));
        Assert.assertTrue(CardValidator.isExpiryValid("01", "2020"));
        Assert.assertTrue(CardValidator.isExpiryValid("1", "2020"));
    }

    @Test
    public void isExpiryValid_shouldTrimMonthAndYear() {
        Assert.assertTrue(CardValidator.isExpiryValid("  12  ", "  2020  "));
    }

    @Test
    public void isExpiryValid_shouldNotAllowNullValues() {
        Assert.assertFalse(CardValidator.isExpiryValid(null, "2020"));
        Assert.assertFalse(CardValidator.isExpiryValid("12", null));
    }

    @Test
    public void isCvnValid_shouldNotPassEmptyString() {
        Assert.assertFalse(CardValidator.isCvnValid(""));
    }

    @Test
    public void isCvnValid_shouldNotPassCVNThatIsTooLong() {
        Assert.assertFalse(CardValidator.isCvnValid("12345"));
    }

    @Test
    public void isCvnValid_shouldNotPassCVNThatIsTooShort() {
        Assert.assertFalse(CardValidator.isCvnValid("12"));
    }

    @Test
    public void isCvnValid_shouldAllowZeroValueCVN() {
        Assert.assertTrue(CardValidator.isCvnValid("000"));
    }

    @Test
    public void isCvnValid_shouldPassShortCVN() {
        Assert.assertTrue(CardValidator.isCvnValid("123"));
    }

    @Test
    public void isCvnValid_shouldNotPassLongCVN() {
        Assert.assertTrue(CardValidator.isCvnValid("1234"));
    }

    @Test
    public void isCvnValid_shouldTrimCVN() {
        Assert.assertTrue(CardValidator.isCvnValid("  123  "));
    }

    @Test
    public void cleanCardNumber_shouldTrimAndRemoveSpaces() {
        Assert.assertEquals(CardValidator.cleanCardNumber("     1234 1234 1234 1234     "), "1234123412341234");
    }

    @Test
    public void cleanCardNumber_shouldReturnNullIfCardNumberIsNull() {
        Assert.assertEquals(CardValidator.cleanCardNumber(null), null);
    }

    @Test
    public void cleanCvn_shouldTrimAndRemoveSpaces() {
        Assert.assertEquals(CardValidator.cleanCvn("  1 2 3  "), "123");
    }

    @Test
    public void cleanCvn_shouldReturnNullIfCardNumberIsNull() {
        Assert.assertEquals(CardValidator.cleanCvn(null), null);
    }
}
