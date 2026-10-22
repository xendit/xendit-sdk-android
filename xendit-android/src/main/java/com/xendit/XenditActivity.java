package com.xendit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.xendit.Models.HasAuthenticationUrl;
import com.xendit.utils.Auth3DSEventValidator;
import java.security.SecureRandom;

/**
 * Created by Sergey on 3/23/17.
 */

public class XenditActivity extends Activity {

    public static final String MESSAGE_KEY = "message_key";
    private static final String AUTHENTICATION_KEY = "authentication_key";

    /**
     * Origins the 3DS result is accepted from.
     */
    private static final String[] TRUSTED_MESSAGE_ORIGINS = {"https://redirect.xendit.co"};

    private ProgressBar progressBar;

    /**
     * Per-activity token passed by the page script with each message to MobileBridge.
     */
    private final String bridgeToken = generateBridgeToken();

    public static Intent getLaunchIntent(Context context, HasAuthenticationUrl authentication) {
        Intent intent = new Intent(context, XenditActivity.class);
        intent.setClass(context, XenditActivity.class);
        intent.setAction(XenditActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra(AUTHENTICATION_KEY, authentication);
        return intent;
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_xendit);

        HasAuthenticationUrl authentication = getIntent().getParcelableExtra(AUTHENTICATION_KEY);

        WebView webView = findViewById(R.id.webView_XenditActivity);
        progressBar = findViewById(R.id.progressBar_XenditActivity);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

        });

        webView.addJavascriptInterface(new WebViewJavaScriptInterface(), "MobileBridge");

        String baseUrl = "https://api.xendit.co";
        String data = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "<style type=\"text/css\">" +
                    "html, body {margin: 0; padding: 0;}" +
                    "iframe {border: none;width: 100%;height: 100%;position: fixed;left: 0;top: 0;}" +
                "</style>" +
                "<script>" +
                    "var trustedOrigins = " + toJsArray(TRUSTED_MESSAGE_ORIGINS) + ";" +
                    "addEventListener('message', function(e){" +
                        "if (trustedOrigins.indexOf(e.origin) === -1) return;" +
                        "try {MobileBridge.postMessage('" + bridgeToken + "', e['data']);} catch(err) {console.log('Android call error');}" +
                    "}, false);" +
                "</script>" +
            "</head>" +
            "<body><iframe src='" + escapeHtmlAttribute(authentication.getPayerAuthenticationUrl()) + "'></iframe></body>" +
            "</html>";

        webView.loadDataWithBaseURL(baseUrl, data, "text/html", "utf-8", null);
    }

    private class WebViewJavaScriptInterface {

        @android.webkit.JavascriptInterface
        public void postMessage(String token, String message) {
            if (!bridgeToken.equals(token) || message == null) {
                return;
            }
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (Auth3DSEventValidator.is3DSResultEventFromXendit(message, XenditActivity.this)) {
                        sendBroadcastReceiver(message);
                        finish();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendBroadcastReceiver(getString(R.string.tokenization_error));
    }

    private void sendBroadcastReceiver(String message) {
        Intent intent = new Intent();
        intent.setAction(Xendit.ACTION_KEY);
        // Deliver the 3DS result only to this app.
        intent.setPackage(getPackageName());
        intent.putExtra(MESSAGE_KEY, message);
        sendBroadcast(intent);
    }

    private static String generateBridgeToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String toJsArray(String[] values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            sb.append('\'').append(values[i]).append('\'');
        }
        return sb.append(']').toString();
    }

    private static String escapeHtmlAttribute(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
            .replace("'", "&#39;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}