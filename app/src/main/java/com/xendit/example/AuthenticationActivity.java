package com.xendit.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.xendit.AuthenticationCallback;
import com.xendit.Models.Authentication;
import com.xendit.Models.CardInfo;
import com.xendit.Models.XenditError;
import com.xendit.Xendit;
import com.xendit.example.models.AuthenticationResponse;

/**
 * Created by Sergey on 4/3/17.
 */

public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText tokenIdEditText;
    private EditText amountEditText;
    private EditText cardCvnEditText;
    private Button authenticateBtn;
    private TextView resultTextView;

    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, AuthenticationActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        setActionBarTitle("Authentication");

        tokenIdEditText = (EditText) findViewById(R.id.tokenIdEditText_AuthenticationActivity);
        amountEditText = (EditText) findViewById(R.id.amountEditText_AuthenticationActivity);
        cardCvnEditText = (EditText) findViewById(R.id.cardCvnEditText_AuthenticationActivity);
        authenticateBtn = (Button) findViewById(R.id.authenticateBtn_AuthenticationActivity);
        resultTextView = (TextView) findViewById(R.id.result_AuthenticationActivity);

        amountEditText.setText(getString(R.string.amountTest));
        cardCvnEditText.setText(getString(R.string.cvnTest));

        authenticateBtn.setOnClickListener(this);
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

        Xendit xendit = new Xendit(getApplicationContext(), CreateTokenActivity.PUBLISHABLE_KEY, this);

        String tokenId = tokenIdEditText.getText().toString();
        String amount = amountEditText.getText().toString();
        String cardCvn = cardCvnEditText.getText().toString();

        xendit.createAuthentication(tokenId, amount, "IDR", cardCvn,new AuthenticationCallback() {
            @Override
            public void onSuccess(Authentication authentication) {
                Gson gson = new Gson();
                AuthenticationResponse authenticationResponse = new AuthenticationResponse(authentication);
                String json = gson.toJson(authenticationResponse);
                resultTextView.setText(json);
                Toast.makeText(AuthenticationActivity.this, "Status: " + authentication.getStatus(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(XenditError xenditError) {
                Toast.makeText(AuthenticationActivity.this, xenditError.getErrorCode(), Toast.LENGTH_SHORT).show();
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