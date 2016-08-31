package com.mmmoussa.iqra;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;

public class ContactActivity extends AppCompatActivity {

    private Context context;
    public static final String TAG = ContactActivity.class.getSimpleName();
    private String apiURL = "http://iqraapp.com/api/v1.0/email";

    private Tracker mTracker;
    private String screenName = "Contact";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setElevation(0);
            ab.setTitle(getResources().getString(R.string.contact_title));
        }

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        context = this;

        final EditText nameField = (EditText) findViewById(R.id.nameField);
        final EditText emailField = (EditText) findViewById(R.id.emailField);
        final EditText messageField = (EditText) findViewById(R.id.messageField);

        Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageField.getText().toString().trim();
                if (message.equals("")) {
                    Toast.makeText(context, getResources().getString(R.string.no_message_entered), Toast.LENGTH_SHORT).show();
                } else {
                    callApi(nameField.getText().toString().trim(), emailField.getText().toString().trim(), message);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void callApi(String name, String email, String message) {
        lockScreenOrientation();

        SpannableString ss1=  new SpannableString(getResources().getString(R.string.sending_message));
        ss1.setSpan(new RelativeSizeSpan(1.7f), 0, ss1.length(), 0);

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(ss1);
        progress.setCancelable(false);
        progress.show();

        ByteArrayEntity entity = null;

        JSONObject jsonParams = new JSONObject();
        try {
            if (!name.equals("")) {
                jsonParams.put("name", name);
            }
            if (!email.equals("")) {
                jsonParams.put("email", email);
            }
            jsonParams.put("message", message);
        } catch (JSONException je) {
            Log.e(TAG, je.getMessage());
        }
        try {
            entity = new ByteArrayEntity(jsonParams.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ue) {
            Log.e(TAG, ue.getMessage());
        }

        if (entity != null) {
            AsyncHttpClient client = new AsyncHttpClient();
            client.post(getApplicationContext(), apiURL, entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // called when response HTTP status is "200 OK"
                    progress.dismiss();
                    // unlockScreenOrientation();
                    Log.v(TAG, response.toString());
                    Toast.makeText(context, getResources().getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    progress.dismiss();
                    // unlockScreenOrientation();

                    String errorMessage = e.getMessage();
                    if (errorMessage == null) {
                        Log.e("API result problem: ", "Socket Timeout");
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.server_connection_lost), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("API result problem: ", errorMessage);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void lockScreenOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
}
