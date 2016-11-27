package com.mmmoussa.iqra;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mmmoussa.iqra.netcomm.NetworkRequestCallback;
import com.mmmoussa.iqra.netcomm.RequestDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {

    public static final String TAG = SearchResultsActivity.class.getSimpleName();

    private SharedPreferences prefs;

    private Tracker mTracker;
    private String screenName = "Search Results";

    private TextView arabicVerse;
    private TextView resultCount;
    private Spinner translationSpinner;
    private ListView matchList;

    private MatchArrayAdapter matchAdapter;
    private List<String> translationShortForms;
    private boolean isTranslationChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // Obtain app preferences
        prefs = this.getSharedPreferences("com.mmmoussa.iqra", MODE_PRIVATE);

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // Extract information from intent
        Intent intent = getIntent();
        String response = intent.getStringExtra("response");
        int numOfMatches = intent.getIntExtra("numOfMatches", 1);

        translationShortForms = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.translation_code)));

        configureActionBar();
        bindViews();
        setupTranslationSpinner();
        displayResultCount(numOfMatches);
        ArrayList<JSONObject> matches = parseSearchQuery(response);
        setupMatchListAdapter(matches);
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

    private void configureActionBar() {
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
            ab.setElevation(0);
        }
    }

    private void bindViews() {
        arabicVerse = (TextView) findViewById(R.id.arabicVerse);
        resultCount = (TextView) findViewById(R.id.resultCount);
        translationSpinner = (Spinner) findViewById(R.id.translationSpinner);
        matchList = (ListView) findViewById(R.id.listView);
    }

    private void setupTranslationSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.translation_spinner_item, getResources().getStringArray(R.array.translation_array));
        translationSpinner.setAdapter(adapter);
        translationSpinner.setSelection(getCurrentTranslationIndex());
        translationSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isTranslationChanged = true;
                return false;
            }
        });
        translationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isTranslationChanged) {
                    return;
                }
                isTranslationChanged = false;
                prefs.edit().putString("translation", translationShortForms.get(translationSpinner.getSelectedItemPosition())).apply();
                onTranslationChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
    }

    private int getCurrentTranslationIndex() {
        String currentTranslation = prefs.getString("translation", "en-hilali");
        return translationShortForms.indexOf(currentTranslation);
    }

    private void displayResultCount(int numOfMatches) {
        if (numOfMatches == 1) {
            resultCount.setText(getResources().getString(R.string.one_result_count));
        } else if (numOfMatches <= 150) {
            resultCount.setText(getResources().getString(R.string.under_limit_result_count, numOfMatches));
        } else {
            resultCount.setText(getResources().getString(R.string.result_count, numOfMatches));
        }
    }

    private void setupMatchListAdapter(final ArrayList<JSONObject> matches) {
        matchAdapter = new MatchArrayAdapter(this, matches);
        matchList.setAdapter(matchAdapter);
        matchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                for (int i = 0; i < matches.size(); i++) {
                    if (i != position) {
                        LinearLayout itemView = (LinearLayout) getViewByPosition(i, matchList);
                        LinearLayout itemHidableVerses = (LinearLayout) itemView.findViewById(R.id.hidableVerses);
                        itemHidableVerses.setVisibility(LinearLayout.GONE);
                    }
                }

                LinearLayout hidableVerses = (LinearLayout) view.findViewById(R.id.hidableVerses);
                if (hidableVerses.getVisibility() == LinearLayout.GONE) {
                    hidableVerses.setVisibility(LinearLayout.VISIBLE);
                } else if (hidableVerses.getVisibility() == LinearLayout.VISIBLE) {
                    hidableVerses.setVisibility(LinearLayout.GONE);
                }

                matchList.post(new Runnable() {
                    @Override
                    public void run() {
                        matchList.smoothScrollToPosition(position);
                        matchList.setSelection(position);
                    }
                });
            }
        });
    }

    private ArrayList<JSONObject> parseSearchQuery(String response) {
        final ArrayList<JSONObject> matches = new ArrayList<>();

        try {
            JSONObject responseObj = new JSONObject(response);
            String query = responseObj.getString("queryText");
            Log.d(TAG, query);
            arabicVerse.setText(query);
            JSONArray jsonMatches = responseObj.getJSONArray("matches");
            int numberOfMatches = jsonMatches.length();
            for (int i = 0; i < numberOfMatches; i++) {
                matches.add(jsonMatches.getJSONObject(i));
            }
        } catch (JSONException je) {
            Log.e("Failed to get match: ", je.getMessage());
        }

        return matches;
    }

    private void onTranslationChanged() {
        SpannableString ss1 = new SpannableString(getResources().getString(R.string.getting_match));
        ss1.setSpan(new RelativeSizeSpan(1.7f), 0, ss1.length(), 0);

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(ss1);
        progress.setCancelable(false);
        progress.show();

        RequestDelegate requestDelegate = RequestDelegate.getInstance(getApplicationContext());
        requestDelegate.performSearchQuery(arabicVerse.getText().toString(), prefs.getString("translation", "en-hilali"), new NetworkRequestCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.v(TAG, response.toString());
                parseSearchQueryResponse(response);
                progress.dismiss();
            }

            @Override
            public void onFailure(Throwable error) {
                onSearchQueryError(error);
                progress.dismiss();
            }
        });
    }

    private void parseSearchQueryResponse(JSONObject response) {
        try {
            JSONObject result = response.getJSONObject("result");
            JSONArray matches = result.getJSONArray("matches");
            int numOfMatches = matches.length();

            if (numOfMatches > 150) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.too_many_results), Toast.LENGTH_LONG).show();
                JSONArray shortenedMatches = new JSONArray();
                for (int i = 0; i < 150; i++) {
                    shortenedMatches.put(matches.get(i));
                }
                result.put("matches", shortenedMatches);
            }

            Log.d(TAG, "Number of matches: " + numOfMatches);
            updateMatchAdapterDataSet(parseSearchQuery(result.toString()));
            displayResultCount(numOfMatches);
        } catch (JSONException je) {
            Log.e("API result problem: ", je.getMessage());
        }
    }

    private void updateMatchAdapterDataSet(List<JSONObject> matches) {
        matchAdapter.clear();
        for (JSONObject ayah : matches) {
            matchAdapter.add(ayah);
        }
    }

    private void onSearchQueryError(Throwable error) {
        String errorMessage = error.getMessage();
        if (errorMessage == null) {
            Log.e("API result problem: ", "Socket Timeout");
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.server_connection_lost), Toast.LENGTH_SHORT).show();
        } else {
            Log.e("API result problem: ", errorMessage);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
