package com.mmmoussa.iqra;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class RequirementsDialogActivity extends AppCompatActivity {

    private Tracker mTracker;
    private String screenName;
    private String requirementType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requirements_dialog);

        setFinishOnTouchOutside(false);

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        TextView requirementTitle = (TextView) findViewById(R.id.requirementTitle);
        TextView requirementText = (TextView) findViewById(R.id.requirementText);
        Button actionButton = (Button) findViewById(R.id.actionButton);

        requirementType = getIntent().getStringExtra("requirementType");
        switch (requirementType) {
            case "permission":
                screenName = "Missing Permissions";
                String permissionName = getIntent().getStringExtra("permissionName");
                requirementTitle.setText(getResources().getString(R.string.missing_permission_title));
                requirementText.setText(getResources().getString(R.string.missing_permission_explanation, permissionName));
                actionButton.setText(getResources().getString(R.string.ok));
                break;
            case "googleDisabled":
                screenName = "Google App Disabled";
                requirementTitle.setText(getResources().getString(R.string.disabled_google_app_title));
                requirementText.setText(getResources().getString(R.string.disabled_google_app_explanation));
                actionButton.setText(getResources().getString(R.string.ok));
                break;
            case "googleMissing":
                screenName = "Google App Not Installed";
                requirementTitle.setText(getResources().getString(R.string.missing_google_app_title));
                requirementText.setText(getResources().getString(R.string.missing_google_app_explanation));
                actionButton.setText(getResources().getString(R.string.download));
                break;
            default:
                break;
        }


        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (requirementType) {
                    case "permission":
                        finish();
                        break;
                    case "googleDisabled":
                        finish();
                        break;
                    case "googleMissing":
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.googlequicksearchbox")));
                        } catch (android.content.ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox")));
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        if (requirementType.equals("googleMissing")) {
            try {
                getPackageManager().getApplicationInfo("com.google.android.googlequicksearchbox", 0);
                finish();
            } catch (PackageManager.NameNotFoundException e) {
                // Ignore
            }
        }
    }
}
