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

/**
 * Created by Sergey on 3/23/17.
 */

public class XenditActivity extends Activity {

    public static final String MESSAGE_KEY = "message_key";
    private static final String AUTHENTICATION_KEY = "authentication_key";

    private ProgressBar progressBar;

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
                "<script>addEventListener('message', function(e){ try {MobileBridge.postMessage(e['data']);} catch(err) {console.log('Android call error');} }, false);</script>" +
            "</head>" +
            "<body><iframe src='" + authentication.getPayerAuthenticationUrl() + "'></iframe></body>" +
            "</html>";

        webView.loadDataWithBaseURL(baseUrl, data, "text/html", "utf-8", null);
    }

    private class WebViewJavaScriptInterface {

        @android.webkit.JavascriptInterface
        public void postMessage(String message) {
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
        intent.putExtra(MESSAGE_KEY, message);
        sendBroadcast(intent);
    }
}