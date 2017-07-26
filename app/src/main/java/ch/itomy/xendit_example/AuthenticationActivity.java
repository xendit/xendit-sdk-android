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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xendit.Models.Token;
import com.xendit.Models.XenditError;
import com.xendit.TokenCallback;
import com.xendit.Xendit;

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

        amountEditText.setText("123000");
        cardCvnEditText.setText("123");

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

        Xendit xendit = new Xendit(getApplicationContext(), CreateTokenActivity.PUBLISHABLE_KEY);
        xendit.createAuthentication(tokenIdEditText.getText().toString(), cardCvnEditText.getText().toString(), amountEditText.getText().toString(), new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                resultTextView.setText(token.getAuthentication().toString());
                Toast.makeText(AuthenticationActivity.this, "Status: " + token.getAuthentication().getStatus(), Toast.LENGTH_SHORT).show();
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