package com.xendit.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xendit.AuthenticationCallback;
import com.xendit.Models.Authentication;
import com.xendit.Models.XenditError;
import com.xendit.Xendit;

/**
 * Created by Sergey on 4/3/17.
 */

public class ThreeDSRecommendationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText tokenIdEditText;
    private Button submitBtn;
    private TextView resultTextView;

    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, ThreeDSRecommendationActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threeds_recommendation);

        setActionBarTitle("Authentication");

        tokenIdEditText = (EditText) findViewById(R.id.tokenIdEditText_ThreeDSRecommendationActivity);
        submitBtn = (Button) findViewById(R.id.submitBtn_ThreeDSRecommendationActivity);
        resultTextView = (TextView) findViewById(R.id.result_ThreeDSRecommendationActivity);

        submitBtn.setOnClickListener(this);
    }

    private void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {

        Xendit xendit = new Xendit(getApplicationContext(), CreateTokenActivity.PUBLISHABLE_KEY);

        // this is where new function will live
        String tokenId = tokenIdEditText.getText().toString();
        int amount = 50000;

        xendit.createAuthentication(tokenId, amount, new AuthenticationCallback() {
            @Override
            public void onSuccess(Authentication authentication) {
                resultTextView.setText("{ id: \"" + authentication.getId() + "\", status: \"" + authentication.getStatus() + "\" }");
                Toast.makeText(ThreeDSRecommendationActivity.this, "Status: " + authentication.getStatus(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(XenditError xenditError) {
                Toast.makeText(ThreeDSRecommendationActivity.this, xenditError.getErrorCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}