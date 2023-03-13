package com.xendit.example;

import static com.xendit.example.CreateTokenActivity.PUBLISHABLE_KEY;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.xendit.Models.Address;
import com.xendit.Models.BillingDetails;
import com.xendit.Models.Customer;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;
import com.xendit.Xendit;
import com.xendit.example.models.TokenizationResponse;
import com.xendit.utils.StoreCVNCallback;

public class StoreCvnActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String DUMMY_PUBLISHABLE_KEY = "xnd_public_development_O4uGfOR3gbOunJU4frcaHmLCYNLy8oQuknDm+R1r9G3S/b2lBQR+gQ==";

    private EditText tokenId;
    private EditText cardCVN;
    private Button storeCVNBtn;
    private TextView storeCVNResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_cvn);
        setActionBarTitle(getString(R.string.store_cvn));

        // Bind UI Component to class fields
        tokenId = (EditText) findViewById(R.id.tokenIdEditText_StoreCVNActivity);
        cardCVN = (EditText) findViewById(R.id.cvnEditText_StoreCVNActivity);
        storeCVNBtn = (Button) findViewById(R.id.btnStoreCVN);
        storeCVNResult = (TextView) findViewById(R.id.result_storeCVNActivity);

        storeCVNBtn.setOnClickListener(this);
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
        try{
            final Xendit xendit = new Xendit(getApplicationContext(), DUMMY_PUBLISHABLE_KEY, this);

            final ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            // Dummy data
            Address billingAddress = new Address();
            billingAddress.setCountry("ID");
            billingAddress.setStreetLine1("Panglima Polim IV");
            billingAddress.setStreetLine2("Ruko Grand Panglima Polim, Blok E");
            billingAddress.setCity("Jakarta Selatan");
            billingAddress.setProvinceState("DKI Jakarta");
            billingAddress.setCategory("WORK");
            billingAddress.setPostalCode("123123");

            BillingDetails billingDetails = new BillingDetails();
            billingDetails.setMobileNumber("+6208123123123");
            billingDetails.setEmail("john@xendit.co");
            billingDetails.setGivenNames("John");
            billingDetails.setSurname("Hudson");
            billingDetails.setPhoneNumber("+6208123123123");
            billingDetails.setAddress(billingAddress);

            Address shippingAddress = billingAddress;
            Address[] customerAddress = { shippingAddress };

            Customer customer = new Customer();
            customer.setMobileNumber("+6208123123123");
            customer.setEmail("john@xendit.co");
            customer.setGivenNames("John");
            customer.setSurname("Hudson");
            customer.setPhoneNumber("+6208123123123");
            customer.setNationality("ID");
            customer.setDateOfBirth("1990-04-13");
            customer.setDescription("test user");
            customer.setAddresses(customerAddress);

            StoreCVNCallback callback = new StoreCVNCallback() {
                @Override
                public void onSuccess(Token token) {
                    progressBar.setVisibility(View.GONE);
                    Gson gson = new Gson();
                    TokenizationResponse tokenizationResponse = new TokenizationResponse(token);
                    String json = gson.toJson(tokenizationResponse);
                    storeCVNResult.setText(json);
                    Log.d("STORE_CVN_SUCCESS", json.toString());
                    Toast.makeText(StoreCvnActivity.this, "Store CVN Successful", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(XenditError xenditError) {
                    progressBar.setVisibility(View.GONE);
                    Log.d("STORE_CVN_FAILED", xenditError.toString());
                    Toast.makeText(StoreCvnActivity.this, xenditError.getErrorCode() + " " +
                            xenditError.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            Log.d("JEB_DEBUG", tokenId.getText().toString() + this.cardCVN.getText().toString());
            xendit.storeCVN(tokenId.getText().toString(), this.cardCVN.getText().toString(), billingDetails, customer, "", callback);
        } catch (Exception e){
            Log.d("JEB_DEBUG", e.toString());
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

    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, StoreCvnActivity.class);
    }
}