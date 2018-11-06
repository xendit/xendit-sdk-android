package com.xendit.example;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.widget.Button;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SampleAppDefaultTest {

    private UiDevice mDevice;

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(
            MainActivity.class);


    /**
     * The target app package.
     */
    private static final String TARGET_PACKAGE =
            InstrumentationRegistry.getTargetContext().getPackageName();

    @Before
    public void startActivityFromHomeScreen() {
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.xendit.example", appContext.getPackageName());
    }

    @Test
    public void testCreateToken() {
        UiObject2 createToken = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "createTokenTextView_MainActivity")),
                        100);
        createToken.click();

        Sleep(3000);

        UiObject2 createTokenButton = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "createTokenBtn_CreateTokenActivity")),
                        100);
        assertThat(createTokenButton, notNullValue());

        UiObject2 yearText = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "expYearEditText_CreateTokenActivity")),
                        100);
        assertThat(yearText, notNullValue());

        yearText.setText("2019");

        createTokenButton.click();

        Sleep(35000);

        try {
            mDevice.findObject(new UiSelector()
                    .index(0)
                    .className(Button.class)).click();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        Sleep(20000);

        mDevice.pressBack();
    }

    @Test
    public void testValidateUtil() {
        UiObject2 validationUtil = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "validationUtilTextView_MainActivity")),
                        100);
        validationUtil.click();

        Sleep(3000);

        UiObject2 cardNumberEditText = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "cardNumberEditText_ValidationUtilActivity")),
                        100);
        assertThat(cardNumberEditText, notNullValue());
        cardNumberEditText.setText("4000000000000002");

        UiObject2 expMonthEditText = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "expMonthEditText_ValidationUtilActivity")),
                        100);
        assertThat(expMonthEditText, notNullValue());
        expMonthEditText.setText("12");

        UiObject2 expYearEditText = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "expYearEditText_ValidationUtilActivity")),
                        100);
        assertThat(expYearEditText, notNullValue());
        expYearEditText.setText("2019");

        UiObject2 cvnEditText = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "cvnEditText_ValidationUtilActivity")),
                        100);
        assertThat(cvnEditText, notNullValue());
        cvnEditText.setText("123");

        UiObject2 validateBtn = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "validateBtn_ValidationUtilActivity")),
                        100);
        validateBtn.click();

        mDevice.pressBack();
    }

    private static void Sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
