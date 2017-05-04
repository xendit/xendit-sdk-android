package ch.itomy.xendit_example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xendit.Models.Card;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;
import com.xendit.TokenCallback;
import com.xendit.Xendit;

/**
 * Created by Sergey on 4/3/17.
 */

public class CreateTokenActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PUBLISH_KEY = "xnd_public_development_O4uGfOR3gbOunJU4frcaHmLCYNLy8oQuknDm+R1r9G3S/b2lBQR+gQ==";

    private EditText cardNumberEditText;
    private EditText expMonthEditText;
    private EditText expYearEditText;
    private EditText cvnEditText;
    private EditText amountEditText;
    private Button createTokenBtn;
    private CheckBox multipleUseCheckBox;
    private TextView resultTextView;

    private boolean isMultipleUse;

    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, CreateTokenActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_token);

        setActionBarTitle("Create Token");

        cardNumberEditText = (EditText) findViewById(R.id.cardNumberEditText_CreateTokenActivity);
        expMonthEditText = (EditText) findViewById(R.id.expMonthEditText_CreateTokenActivity);
        expYearEditText = (EditText) findViewById(R.id.expYearEditText_CreateTokenActivity);
        cvnEditText = (EditText) findViewById(R.id.cvnEditText_CreateTokenActivity);
        amountEditText = (EditText) findViewById(R.id.amountEditText_CreateTokenActivity);
        createTokenBtn = (Button) findViewById(R.id.createTokenBtn_CreateTokenActivity);
        multipleUseCheckBox = (CheckBox) findViewById(R.id.multipleUseCheckBox_CreateTokenActivity);
        resultTextView = (TextView) findViewById(R.id.result_CreateTokenActivity);

        createTokenBtn.setOnClickListener(this);

        cardNumberEditText.setText("4000000000000002");
        expMonthEditText.setText("12");
        expYearEditText.setText("2017");
        cvnEditText.setText("123");
        amountEditText.setText("123000");
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
        Card card = new Card(cardNumberEditText.getText().toString(),
                expMonthEditText.getText().toString(),
                expYearEditText.getText().toString(),
                cvnEditText.getText().toString());

        isMultipleUse = multipleUseCheckBox.isChecked();

        Xendit xendit = new Xendit(getApplicationContext(), PUBLISH_KEY);
        xendit.createToken(card, amountEditText.getText().toString(), isMultipleUse, new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                resultTextView.setText(token.getAuthentication().toString());
                Toast.makeText(CreateTokenActivity.this, "Status: " + token.getAuthentication().getStatus(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(XenditError xenditError) {
                Toast.makeText(CreateTokenActivity.this, xenditError.getError(), Toast.LENGTH_SHORT).show();
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