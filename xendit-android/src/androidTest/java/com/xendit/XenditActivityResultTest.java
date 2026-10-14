package com.xendit;

import android.app.UiAutomation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import androidx.core.content.ContextCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xendit.Models.Authentication;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Checks which 3DS result messages XenditActivity delivers to the SDK's result receivers.
 */
@RunWith(AndroidJUnit4.class)
public class XenditActivityResultTest {

    private static final String RESULT = "{\"id\":\"auth_id\",\"status\":\"VERIFIED\"}";

    private Context context;
    private BroadcastReceiver exportedReceiver;
    private BroadcastReceiver sdkReceiver;
    private CountDownLatch receivedByExported;
    private CountDownLatch receivedBySdk;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        receivedByExported = new CountDownLatch(1);
        receivedBySdk = new CountDownLatch(1);

        // Exported on purpose: catches anything XenditActivity broadcasts, however it is scoped.
        exportedReceiver = latchReceiver(receivedByExported);
        ContextCompat.registerReceiver(context, exportedReceiver, new IntentFilter(Xendit.ACTION_KEY),
            ContextCompat.RECEIVER_EXPORTED);

        // Registered exactly the way the SDK registers its result receivers.
        sdkReceiver = latchReceiver(receivedBySdk);
        XenditImpl.registerXenditReceiver(context, sdkReceiver);
    }

    @After
    public void tearDown() {
        context.unregisterReceiver(exportedReceiver);
        context.unregisterReceiver(sdkReceiver);
    }

    @Test
    public void resultFromOtherOrigin_isNotDelivered() throws Exception {
        launchWithFrame(
            "setInterval(function(){ parent.postMessage('" + RESULT + "', '*'); }, 200);");

        assertFalse("Result from other origin was delivered",
            receivedByExported.await(8, TimeUnit.SECONDS));
    }

    @Test
    public void bridgeCallWithoutToken_isNotDelivered() throws Exception {
        launchWithFrame("setInterval(function(){"
            + " try { MobileBridge.postMessage('" + RESULT + "'); } catch (e) {}"
            + " try { MobileBridge.postMessage('wrong_token', '" + RESULT + "'); } catch (e) {}"
            + " }, 200);");

        assertFalse("Bridge call without token was delivered",
            receivedByExported.await(8, TimeUnit.SECONDS));
    }

    @Test
    public void resultBroadcastFromXenditActivity_isDeliveredToSdkReceiver() throws Exception {
        // Back press broadcasts a tokenization error through the same channel as the 3DS result.
        launchWithFrame("");
        Thread.sleep(2000);
        runShell("input keyevent KEYCODE_BACK");

        assertTrue("SDK receiver did not get XenditActivity's broadcast",
            receivedBySdk.await(8, TimeUnit.SECONDS));
    }

    @Test
    public void broadcastFromOtherApp_isNotDelivered() throws Exception {
        // `am broadcast` runs as the shell user, i.e. a different app from this one.
        runShell("am broadcast -a " + Xendit.ACTION_KEY + " --es " + XenditActivity.MESSAGE_KEY
            + " " + RESULT.replace("\"", "\\\""));

        assertFalse("Broadcast from other app was delivered",
            receivedBySdk.await(5, TimeUnit.SECONDS));
    }

    private static BroadcastReceiver latchReceiver(final CountDownLatch latch) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                latch.countDown();
            }
        };
    }

    private void launchWithFrame(String script) {
        // Frame with an opaque origin, i.e. not one of the trusted origins.
        String html = "<html><body><script>" + script + "</script></body></html>";
        JsonObject json = new JsonObject();
        json.addProperty("id", "auth_id");
        json.addProperty("status", "IN_REVIEW");
        // Uri.encode leaves ' as is, which would end XenditActivity's single-quoted iframe src.
        String url = "data:text/html," + Uri.encode(html).replace("'", "%27");
        json.addProperty("payer_authentication_url", url);
        Authentication authentication = new Gson().fromJson(json, Authentication.class);

        context.startActivity(XenditActivity.getLaunchIntent(context, authentication));
    }

    private static void runShell(String command) throws IOException {
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        ParcelFileDescriptor pfd = uiAutomation.executeShellCommand(command);
        try (FileInputStream in = new ParcelFileDescriptor.AutoCloseInputStream(pfd)) {
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                // Drain output so the command runs to completion.
            }
        }
    }
}
