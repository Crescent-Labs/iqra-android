package com.mmmoussa.iqra;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.NumberFormat;
import java.util.Locale;

public class ShareResultActivity extends AppCompatActivity {

    private Context context;
    private Locale arabicLocale = new Locale("ar");
    NumberFormat arabicNF = NumberFormat.getInstance(arabicLocale);

    private Tracker mTracker;
    private String screenName = "Share Result";

    private int ayahNum;
    private String arabicSurahName;
    private String translationSurahName;
    private String arabicAyah;
    private String translationAyah;

    private CheckBox arabicAyahCheckBox;
    private CheckBox translationAyahCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_result);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setElevation(0);
            ab.setTitle(getResources().getString(R.string.action_share));
        }

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        context = this;

        Intent intent = getIntent();
        ayahNum = intent.getIntExtra("ayahNum", 0);
        arabicSurahName = intent.getStringExtra("arabicSurahName");
        translationSurahName = intent.getStringExtra("translationSurahName");
        arabicAyah = intent.getStringExtra("arabicAyah");
        translationAyah = intent.getStringExtra("translationAyah");

        arabicAyahCheckBox = (CheckBox) findViewById(R.id.arabicAyahCheckBox);
        translationAyahCheckBox = (CheckBox) findViewById(R.id.translationAyahCheckBox);
        Button shareButton = (Button) findViewById(R.id.shareButton);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void share() {
        String arabicAyahNum = arabicNF.format(ayahNum);

        boolean arabicAyahChecked = arabicAyahCheckBox.isChecked();
        boolean translationAyahChecked = translationAyahCheckBox.isChecked();

        if (!arabicAyahChecked && !translationAyahChecked) {
            Toast.makeText(context, getResources().getString(R.string.nothing_to_share), Toast.LENGTH_SHORT).show();
        } else {
            String shareMessage;

            if (arabicAyahChecked && translationAyahChecked) {
                shareMessage = arabicAyah + "\n(" + arabicSurahName + ", " + getResources().getString(R.string.ayah_in_arabic) + " " + arabicAyahNum + ")\n\n" + translationAyah + "\n(" + translationSurahName + ", ayah " + Integer.toString(ayahNum) + ")";
            } else if (arabicAyahChecked) {
                shareMessage = arabicAyah + "\n(" + arabicSurahName + ", " + getResources().getString(R.string.ayah_in_arabic) + " " + arabicAyahNum + ")";
            } else {
                shareMessage = translationAyah + "\n(" + translationSurahName + ", " + getResources().getString(R.string.ayah_translated) + " " + Integer.toString(ayahNum) + ")";
            }

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_with)));
        }
    }
}
