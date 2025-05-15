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
import com.xendit.Models.XenditError;
import com.xendit.Xendit;
import com.xendit.example.models.AuthenticationResponse;

/**
 * Created by Sergey on 4/3/17.
 */

public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PUBLISHABLE_KEY = "xnd_public_development_D8wJuWpOY15JvjJyUNfCdDUTRYKGp8CSM3W0ST4d0N4CsugKyoGEIx6b84j1D7Pg";
    private EditText apiKeyEditText;
    private EditText tokenIdEditText;
    private EditText amountEditText;
    private EditText currencyEditText;
    private EditText cardCvnEditText;
    private EditText cardHolderFirstNameEditText;
    private EditText cardHolderLastNameEditText;
    private EditText cardHolderEmailEditText;
    private EditText cardHolderPhoneNumberEditText;
    private EditText midLabelText;
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

        apiKeyEditText = (EditText) findViewById(R.id.apiKeyEditText_AuthenticationActivity);
        tokenIdEditText = (EditText) findViewById(R.id.tokenIdEditText_AuthenticationActivity);
        amountEditText = (EditText) findViewById(R.id.amountEditText_AuthenticationActivity);
        currencyEditText = (EditText) findViewById(R.id.currencyEditText_AuthenticationActivity);
        cardCvnEditText = (EditText) findViewById(R.id.cardCvnEditText_AuthenticationActivity);
        cardHolderFirstNameEditText = (EditText) findViewById(R.id.cardHolderFirstNameEditText_AuthenticationActivity);
        cardHolderLastNameEditText = (EditText) findViewById(R.id.cardHolderLastNameEditText_AuthenticationActivity);
        cardHolderEmailEditText = (EditText) findViewById(R.id.cardHolderEmailEditText_AuthenticationActivity);
        cardHolderPhoneNumberEditText = (EditText) findViewById(R.id.cardHolderPhoneNumberEditText_AuthenticationActivity);
        midLabelText = (EditText) findViewById(R.id.midLabelEditText_AuthenticationActivity);

        authenticateBtn = (Button) findViewById(R.id.authenticateBtn_AuthenticationActivity);
        resultTextView = (TextView) findViewById(R.id.result_AuthenticationActivity);

        apiKeyEditText.setText(PUBLISHABLE_KEY);
        amountEditText.setText(getString(R.string.amountTest));
        currencyEditText.setText(getString(R.string.currencyTest));
        cardCvnEditText.setText(getString(R.string.cvnTest));
        cardHolderFirstNameEditText.setText(R.string.cardHolderFirstNameTest);
        cardHolderLastNameEditText.setText(R.string.cardHolderLastNameTest);
        cardHolderEmailEditText.setText(R.string.cardHolderEmailTest);
        cardHolderPhoneNumberEditText.setText(R.string.cardHolderPhoneNumberTest);

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

        String apiKey = apiKeyEditText.getText().toString();
        String tokenId = tokenIdEditText.getText().toString();
        String amount = amountEditText.getText().toString();
        String currency = currencyEditText.getText().toString();
        String cardCvn = cardCvnEditText.getText().toString();
        String onBehalfOf = "";
        String midLabel = midLabelText.getText().toString();

        Xendit xendit = Xendit.create(getApplicationContext(), apiKey, this);

        AuthenticationCallback callback = new AuthenticationCallback() {
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
                String errorMessage = String.format("{ \"error_code\": \"%s\", \"message\": \"%s\" }", xenditError.getErrorCode(), xenditError.getErrorMessage());
                resultTextView.setText(errorMessage);
                Toast.makeText(AuthenticationActivity.this, xenditError.getErrorCode(), Toast.LENGTH_SHORT).show();
            }
        };

        if (midLabel.isBlank()){
            if (cardCvn.isBlank()) {
                xendit.createAuthentication(tokenId, amount, currency, onBehalfOf, callback);
            } else {
                xendit.createAuthentication(tokenId, amount, currency, cardCvn, onBehalfOf, callback);
            }
        } else{
            xendit.createAuthentication(tokenId, amount, currency, cardCvn, onBehalfOf, midLabel , callback);
        }

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
