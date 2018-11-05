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
        String name = getClass().getName();
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
            mDevice.findObject(new UiSelector().textContains("Submit")).click();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        mDevice.pressBack();

        Sleep(20000);

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
