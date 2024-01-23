package com.xendit.example;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int LOCATION_REQUEST = 1;
    private TextView createTokenTextView;
    private TextView authenticationTextView;
    private TextView validationUtilTextView;

    private TextView storeCVNTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActionBarTitle("Xendit");

        createTokenTextView = (TextView) findViewById(R.id.createTokenTextView_MainActivity);
        authenticationTextView = (TextView) findViewById(R.id.authenticationTextView_MainActivity);
        validationUtilTextView = (TextView) findViewById(R.id.validationUtilTextView_MainActivity);
        storeCVNTextView = (TextView) findViewById(R.id.storeCVNTextView_MainActivity);

        createTokenTextView.setOnClickListener(this);
        authenticationTextView.setOnClickListener(this);
        storeCVNTextView.setOnClickListener(this);
        validationUtilTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.createTokenTextView_MainActivity) {
            startActivity(CreateTokenActivity.getLaunchIntent(this));
        } else if (id == R.id.authenticationTextView_MainActivity) {
            startActivity(AuthenticationActivity.getLaunchIntent(this));
        } else if (id == R.id.validationUtilTextView_MainActivity) {
            startActivity(ValidationUtilActivity.getLaunchIntent(this));
        } else if (id == R.id.storeCVNTextView_MainActivity) {
            startActivity(StoreCvnActivity.getLaunchIntent(this));
        }
    }

    private void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }
}