package com.mmmoussa.iqra;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private Context context;
    private final int MY_PERMISSIONS_REQUEST_INTERNET = 100;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 200;
    private boolean hasInternetPermission;
    private boolean hasRecordAudioPermission;

    private String apiURL = "https://api.iqraapp.com/api/v3.0/search";
    private String API_KEY = BuildConfig.IQRA_API_KEY;
    public static final String TAG = MainActivity.class.getSimpleName();
    private SpeechRecognizer mSpeechRecognizer = null;
    private Intent mSpeechRecognizerIntent;
    private boolean mIsListening;
    private ArrayList<Integer> voiceLevelChanges;

    private ImageButton recordButton;
    private TextView micText;
    private ImageView recordCircle;
    private TextView partialResult;

    private SharedPreferences prefs;

    private Tracker mTracker;
    private String screenName = "Home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setElevation(0);
            ab.setDisplayShowTitleEnabled(false);
        }

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        hasInternetPermission = false;
        hasRecordAudioPermission = false;

        isOnline();
        doPermissionCheck();
        doGoogleAppCheck();

        prefs = this.getSharedPreferences("com.mmmoussa.iqra", MODE_PRIVATE);

        recordButton = (ImageButton) findViewById(R.id.recordButton);
        micText = (TextView) findViewById(R.id.micText);
        recordCircle = (ImageView) findViewById(R.id.recordCircle);
        partialResult = (TextView) findViewById(R.id.partialResult);

        voiceLevelChanges = new ArrayList<>();

        setupSpeechInput();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.requestLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent aboutIntent = new Intent(context, AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.action_contact:
                Intent contactIntent = new Intent(context, ContactActivity.class);
                startActivity(contactIntent);
                break;
            case R.id.action_share:
                share();
                break;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(context, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void share() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_message));
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_with)));
    }

    private void isOnline() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                boolean online = false;
                Runtime runtime = Runtime.getRuntime();
                try {
                    Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                    int     exitValue = ipProcess.waitFor();
                    online = (exitValue == 0);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

                if (!online) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void doGoogleAppCheck() {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo("com.google.android.googlequicksearchbox", 0);
            if (!ai.enabled) {
                Intent googleAppIntent = new Intent(context, RequirementsDialogActivity.class);
                googleAppIntent.putExtra("requirementType", "googleDisabled");
                startActivity(googleAppIntent);
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            Intent googleAppIntent = new Intent(context, RequirementsDialogActivity.class);
            googleAppIntent.putExtra("requirementType", "googleMissing");
            startActivity(googleAppIntent);
        }
    }

    private void doPermissionCheck() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_INTERNET);
        } else {
            hasInternetPermission = true;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            hasRecordAudioPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_INTERNET:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasInternetPermission = true;
                } else {
                    Intent permissionIntent = new Intent(context, RequirementsDialogActivity.class);
                    permissionIntent.putExtra("requirementType", "permission");
                    permissionIntent.putExtra("permissionName", getResources().getString(R.string.internet));
                    startActivity(permissionIntent);
                }
                break;
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasRecordAudioPermission = true;
                } else {
                    Intent permissionIntent = new Intent(context, RequirementsDialogActivity.class);
                    permissionIntent.putExtra("requirementType", "permission");
                    permissionIntent.putExtra("permissionName", getResources().getString(R.string.record_audio));
                    startActivity(permissionIntent);
                }
                break;
        }
    }

    public void listen(View view) {
        if (hasInternetPermission && hasRecordAudioPermission) {
            if (!mIsListening) {
                Log.d(TAG, "Started listening");
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                voiceLevelChanges.clear();
                voiceLevelChanges.addAll(Arrays.asList(90, 90, 90, 90, 90));
                recordCircle.setImageResource(R.drawable.record_circle_active);
            } else {
                mSpeechRecognizer.stopListening();
            }
            mIsListening = !mIsListening;
        } else {
            doPermissionCheck();
        }
    }

    protected void setupSpeechInput() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        if (!mSpeechRecognizerIntent.hasExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE)) {
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        }
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-AE");
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        mIsListening = false;
    }

    private void callApi(String arabicText) {
        lockScreenOrientation();

        SpannableString ss1=  new SpannableString(getResources().getString(R.string.getting_match));
        ss1.setSpan(new RelativeSizeSpan(1.7f), 0, ss1.length(), 0);

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(ss1);
        progress.setCancelable(false);
        progress.show();

        ByteArrayEntity entity = null;

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("arabicText", arabicText);
            jsonParams.put("translation", prefs.getString("translation", "en-hilali"));
            jsonParams.put("apikey", API_KEY);
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
                    try {
                        // TODO: JsonReader is more effecient than a JSONObject
                        JSONObject result = response.getJSONObject("result");
                        JSONArray matches = result.getJSONArray("matches");
                        int numOfMatches = matches.length();
                        if (numOfMatches == 0) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_matches), Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(getApplicationContext(), SearchResultsActivity.class);
                            if (numOfMatches > 150) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.too_many_results), Toast.LENGTH_LONG).show();
                                JSONArray shortenedMatches = new JSONArray();
                                for (int i = 0; i < 150; i++) {
                                    shortenedMatches.put(matches.get(i));
                                }
                                result.put("matches", shortenedMatches);
                            }
                            Log.d(TAG, "Number of matches: " + numOfMatches);
                            intent.putExtra("response", result.toString());
                            intent.putExtra("numOfMatches", numOfMatches);
                            startActivity(intent);
                        }
                    } catch (JSONException je) {
                        Log.e("API result problem: ", je.getMessage());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    progress.dismiss();
                    // unlockScreenOrientation();
                    Log.e("API result problem: ", e.getMessage());
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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


    /**
     * Methods for RecognitionListener
     */
    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginingOfSpeech");
        micText.setText(getString(R.string.now_recording));
        recordCircle.getLayoutParams().width = 90;
        recordCircle.getLayoutParams().height = 90;
        recordCircle.requestLayout();
        recordCircle.setImageResource(R.drawable.record_circle_active);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
        micText.setText(getString(R.string.done_recording));
        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.requestLayout();
        recordCircle.setImageResource(R.drawable.record_circle_inactive);
    }

    @Override
    public void onError(int error) {
        String mError = "";
        switch (error) {
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                mError = getResources().getString(R.string.error_network_timeout);
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                mError = getResources().getString(R.string.error_network);
                mSpeechRecognizer.cancel();
                mIsListening = false;
                recordCircle.setImageResource(R.drawable.record_circle_inactive);
                break;
            case SpeechRecognizer.ERROR_AUDIO:
                mError = getResources().getString(R.string.error_audio);
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_SERVER:
                mError = getResources().getString(R.string.error_server);
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                mError = getResources().getString(R.string.error_client);
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                mError = getResources().getString(R.string.error_speech_timeout);
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                mError = getResources().getString(R.string.error_no_match);
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                mError = getResources().getString(R.string.error_recognizer_busy);
                mSpeechRecognizer.cancel();
                mIsListening = false;
                recordCircle.setImageResource(R.drawable.record_circle_inactive);
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                mError = getResources().getString(R.string.error_insufficient_permissions);
                mSpeechRecognizer.cancel();
                mIsListening = false;
                recordCircle.setImageResource(R.drawable.record_circle_inactive);
                break;
        }
        Log.i(TAG,  "Error: " +  error + " - " + mError);

        micText.setText(mError);
        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.requestLayout();
        partialResult.setText("");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d(TAG, "onPartialResults");
        ArrayList<String> results = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (results != null) {
            partialResult.setText(results.get(0));
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
        micText.setText(getString(R.string.begin_recording));
        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.requestLayout();
    }

    @Override
    public void onResults(Bundle results) {
        mIsListening = false;
        micText.setText(getString(R.string.tap_on_mic));
        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.requestLayout();
        recordCircle.setImageResource(R.drawable.record_circle_inactive);
        partialResult.setText("");
        // Log.d(TAG, "onResults"); //$NON-NLS-1$
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        // matches are the return values of speech recognition engine
        if (matches != null) {
            // Log.d(TAG, matches.toString()); //$NON-NLS-1$
            callApi(matches.get(0));
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.cannot_understand), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        if (rmsdB < 0) {
            rmsdB = 0;
        }
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (rmsdB * 8) + 80, getResources().getDisplayMetrics());

        voiceLevelChanges.remove(0);
        voiceLevelChanges.add(size);

        int adjustedSize = 0;

        for (int i = 0; i < voiceLevelChanges.size(); i++) {
            adjustedSize += voiceLevelChanges.get(i);
        }

        adjustedSize = adjustedSize / voiceLevelChanges.size();

        if (!mIsListening) {
            recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
            recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
            recordCircle.setImageResource(R.drawable.record_circle_inactive);
        } else {
            recordCircle.getLayoutParams().width = adjustedSize;
            recordCircle.getLayoutParams().height = adjustedSize;
        }
        recordCircle.requestLayout();
    }
}
