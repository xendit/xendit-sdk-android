package com.xendit.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.xendit.utils.CardValidator;

/**
 * Created by Sergey on 4/3/17.
 */

public class ValidationUtilActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText cardNumberEditText;
    private EditText expMonthEditText;
    private EditText expYearEditText;
    private EditText cvnEditText;
    private Button validateBtn;

    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, ValidationUtilActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation_util);

        setActionBarTitle("Validation Util");

        cardNumberEditText = (EditText) findViewById(R.id.cardNumberEditText_ValidationUtilActivity);
        expMonthEditText = (EditText) findViewById(R.id.expMonthEditText_ValidationUtilActivity);
        expYearEditText = (EditText) findViewById(R.id.expYearEditText_ValidationUtilActivity);
        cvnEditText = (EditText) findViewById(R.id.cvnEditText_ValidationUtilActivity);
        validateBtn = (Button) findViewById(R.id.validateBtn_ValidationUtilActivity);

        validateBtn.setOnClickListener(this);
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


        if (!CardValidator.isCardNumberValid(cardNumberEditText.getText().toString())) {
            Toast.makeText(this, "Card number is invalid", Toast.LENGTH_SHORT).show();
        } else if (!CardValidator.isExpiryValid(expMonthEditText.getText().toString(), expYearEditText.getText().toString())) {
            Toast.makeText(this, "Card expiration date is invalid", Toast.LENGTH_SHORT).show();
        } else if (!CardValidator.isCvnValid(cvnEditText.getText().toString())) {
            Toast.makeText(this, "Card cvn is invalid", Toast.LENGTH_SHORT).show();
        } else if (!CardValidator.isCvnValidForCardType(cvnEditText.getText().toString(), cardNumberEditText.getText().toString())){
            Toast.makeText(this, "Card cvn is invalid for this card type", Toast.LENGTH_SHORT).show();
        } else if (CardValidator.isCardNumberValid(cardNumberEditText.getText().toString())
                && CardValidator.isExpiryValid(expMonthEditText.getText().toString(), expYearEditText.getText().toString())
                && CardValidator.isCvnValid(cvnEditText.getText().toString())
                && CardValidator.isCvnValidForCardType(cvnEditText.getText().toString(), cardNumberEditText.getText().toString())) {
            Toast.makeText(this, "Card is valid", Toast.LENGTH_SHORT).show();
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