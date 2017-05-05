package com.xendit;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void isCvnValid() throws Exception {
        boolean cvnValid = Xendit.isCvnValid("4256");
        assertTrue(cvnValid);
//        assertEquals(true, cvnValid);
    }

    @Test
    public void isCreditCardValid() throws Exception {
        boolean cardNumberValid = Xendit.isCardNumberValid("4149045387380958");
        assertTrue(cardNumberValid);
    }

    @Test
    public void isExpiryValid() throws Exception {
        boolean expiryValid = Xendit.isExpiryValid("06", "2020");
        assertTrue(expiryValid);
    }
}