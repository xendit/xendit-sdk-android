package com.xendit.utils;

import android.content.Context;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.hypertrack.hyperlog.HyperLog;


import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;

import static com.google.common.truth.Truth.assertThat;


/**
 * Created by gonzalez on 8/22/17.
 */

@RunWith(AndroidJUnit4.class)
@MediumTest
public class CardValidatorTests {

    private Context context = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        // init hyperlog
        HyperLog.initialize(context);
        HyperLog.setLogLevel(Log.VERBOSE);
    }



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
        Assert.assertTrue(CardValidator.isCardNumberValid("5213724373543245"));
        Assert.assertTrue(CardValidator.isCardNumberValid("5513799778027330"));
        Assert.assertTrue(CardValidator.isCardNumberValid("5404666842103888"));
    }

    @Test
    public void isCardNumberValid_shouldPassVisa() {
        Assert.assertTrue(CardValidator.isCardNumberValid("4000000000000002"));
        Assert.assertTrue(CardValidator.isCardNumberValid("4111111111111111"));
        Assert.assertTrue(CardValidator.isCardNumberValid("4012888888881881"));
        Assert.assertTrue(CardValidator.isCardNumberValid("4222222222222"));
        Assert.assertTrue(CardValidator.isCardNumberValid("4929950253805473"));
        Assert.assertTrue(CardValidator.isCardNumberValid("4916154524086329"));
        Assert.assertTrue(CardValidator.isCardNumberValid("4556564166261021533"));
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
    public void isCardNumberValid_shouldNotAllowCardOfUnkownType() {
        Assert.assertFalse(CardValidator.isCardNumberValid("122000000000003"));
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

    @Test
    public void getCardType_shouldHandleAmex() {
        Assert.assertEquals(CardValidator.getCardType("378282246310005"), CardValidator.CardType.AMEX);
        Assert.assertEquals(CardValidator.getCardType("371449635398431"), CardValidator.CardType.AMEX);
        Assert.assertEquals(CardValidator.getCardType("378734493671000"), CardValidator.CardType.AMEX);
    }

    @Test
    public void getCardType_shouldHandleDiscover() {
        Assert.assertEquals(CardValidator.getCardType("6011111111111117"), CardValidator.CardType.DISCOVER);
        Assert.assertEquals(CardValidator.getCardType("6011000990139424"), CardValidator.CardType.DISCOVER);
    }

    @Test
    public void getCardType_shouldHandleJCB() {
        Assert.assertEquals(CardValidator.getCardType("3530111333300000"), CardValidator.CardType.JCB);
        Assert.assertEquals(CardValidator.getCardType("3566002020360505"), CardValidator.CardType.JCB);
    }

    @Test
    public void getCardType_shouldHandleMastercard() {
        Assert.assertEquals(CardValidator.getCardType("5555555555554444"), CardValidator.CardType.MASTERCARD);
        Assert.assertEquals(CardValidator.getCardType("5105105105105100"), CardValidator.CardType.MASTERCARD);
    }

    @Test
    public void getCardType_shouldHandleVisa() {
        Assert.assertEquals(CardValidator.getCardType("4111111111111111"), CardValidator.CardType.VISA);
        Assert.assertEquals(CardValidator.getCardType("4012888888881881"), CardValidator.CardType.VISA);
        Assert.assertEquals(CardValidator.getCardType("4222222222222"), CardValidator.CardType.VISA);
    }

    @Test
    public void getCardType_shouldHandleDankort() {
        Assert.assertEquals(CardValidator.getCardType("5019717010103742"), CardValidator.CardType.DANKORT);
    }

    @Test
    public void getCardType_shouldHandleUnknownCardType() {
        assertThat(CardValidator.getCardType("000000000000")).isEqualTo(CardValidator.CardType.OTHER);
        //Assert.assertEquals(CardValidator.getCardType("000000000000"), CardValidator.CardType.OTHER);
    }

    @Test
    public void isCvnValidForCardType_shouldHandleAMEX() {
        Assert.assertTrue(CardValidator.isCvnValidForCardType("1234", "378282246310005"));
        Assert.assertFalse(CardValidator.isCvnValidForCardType("123", "378282246310005"));
    }

    @Test
    public void isCvnValidForCardType_shouldHandleNonAMEX() {
        Assert.assertTrue(CardValidator.isCvnValidForCardType("123", "4012888888881881"));
        Assert.assertFalse(CardValidator.isCvnValidForCardType("1234", "4012888888881881"));
    }
}
