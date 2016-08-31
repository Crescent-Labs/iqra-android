package com.mmmoussa.iqra;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private Spinner translationSpinner;
    private List<String> translationShortForms;
    private Context context;
    private SharedPreferences prefs;

    private Tracker mTracker;
    private String screenName = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setElevation(0);
            ab.setTitle(getResources().getString(R.string.settings_title));
        }

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        context = this;
        prefs = this.getSharedPreferences("com.mmmoussa.iqra", MODE_PRIVATE);

        translationShortForms = Arrays.asList(
                "en-sahih",
                "en-arberry",
                "en-asad",
                "en-daryabadi",
                "en-hilali",
                "en-pickthall",
                "en-qaribullah",
                "en-sarwar",
                "en-yusufali",
                "en-maududi",
                "en-shakir",
                "en-transliteration"
        );

        String currentTranslation = prefs.getString("translation", "en-hilali");
        int currentTranslationIndex = translationShortForms.indexOf(currentTranslation);
        if (currentTranslationIndex == -1) {
            // This should never run but just in case...
            currentTranslationIndex = 1;
        }

        translationSpinner = (Spinner) findViewById(R.id.translationSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.translation_spinner_item, getResources().getStringArray(R.array.translation_array));
        translationSpinner.setAdapter(adapter);
        translationSpinner.setSelection(currentTranslationIndex);

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putString("translation", translationShortForms.get(translationSpinner.getSelectedItemPosition())).apply();
                finish();
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
}
