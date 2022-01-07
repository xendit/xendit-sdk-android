package com.xendit.example;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import android.widget.Button;

import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SampleAppDefaultTest {

    private UiDevice mDevice;

    private Context appContext = InstrumentationRegistry.getTargetContext();

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

        yearText.setText(Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 1));

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

        UiObject2 resultText = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "result_CreateTokenActivity")),
                        100);
        assertThat(resultText.getText(), CoreMatchers.containsString("should_3ds"));

        Sleep(2000);

        mDevice.pressBack();
    }

    @Test
    public void testCreateMultipleToken() {
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

        UiObject2 multipleUseTokenCheckbox = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "multipleUseCheckBox_CreateTokenActivity")),
                        100);
        assertThat(multipleUseTokenCheckbox, notNullValue());

        yearText.setText(Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 1));
        multipleUseTokenCheckbox.click();

        createTokenButton.click();

        Sleep(5000);

        UiObject2 resultText = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "result_CreateTokenActivity")),
                        100);
        assertThat(resultText.getText(), CoreMatchers.containsString("should_3ds"));

        try {
            JSONObject jsonObject = new JSONObject(resultText.getText());
            assertEquals(jsonObject.get("should_3ds"), true);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }

        Sleep(2000);

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
        cardNumberEditText.setText(appContext.getString(R.string.cardNumbTest));

        UiObject2 expMonthEditText = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "expMonthEditText_ValidationUtilActivity")),
                        100);
        assertThat(expMonthEditText, notNullValue());
        expMonthEditText.setText(appContext.getString(R.string.expMonthTest));

        UiObject2 expYearEditText = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "expYearEditText_ValidationUtilActivity")),
                        100);
        assertThat(expYearEditText, notNullValue());
        expYearEditText.setText(Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 1));

        UiObject2 cvnEditText = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "cvnEditText_ValidationUtilActivity")),
                        100);
        assertThat(cvnEditText, notNullValue());
        cvnEditText.setText(appContext.getString(R.string.cvnTest));

        UiObject2 validateBtn = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "validateBtn_ValidationUtilActivity")),
                        100);
        validateBtn.click();

        Sleep(3000);

        mDevice.pressBack();
    }

    @Test
    public void testAuthActivity() {
        UiObject2 auth = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "authenticationTextView_MainActivity")),
                        100);
        auth.click();

        Sleep(3000);

        String id = CreateTokenActivity.getTokenId();

        UiObject2 tokenIdEditText = mDevice
                .wait(Until.findObject(By.res(TARGET_PACKAGE, "tokenIdEditText_AuthenticationActivity")),
                        100);
        assertThat(tokenIdEditText, notNullValue());
        tokenIdEditText.setText(id);

        Sleep(3000);

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
