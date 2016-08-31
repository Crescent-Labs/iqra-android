package com.mmmoussa.iqra;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResultsActivity extends AppCompatActivity {

    public static final String TAG = SearchResultsActivity.class.getSimpleName();

    private Tracker mTracker;
    private String screenName = "Search Results";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setElevation(0);
            ab.setDisplayShowTitleEnabled(false);
        }

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        TextView arabicVerse = (TextView) findViewById(R.id.arabicVerse);
        TextView resultCount = (TextView) findViewById(R.id.resultCount);
        final ListView matchList = (ListView) findViewById(R.id.listView);

        Intent intent = getIntent();
        String response = intent.getStringExtra("response");
        int numOfMatches = intent.getIntExtra("numOfMatches", 1);

        if (numOfMatches == 1) {
            resultCount.setText(getResources().getString(R.string.one_result_count));
        } else if (numOfMatches <= 150) {
            resultCount.setText(getResources().getString(R.string.under_limit_result_count, numOfMatches));
        } else {
            resultCount.setText(getResources().getString(R.string.result_count, numOfMatches));
        }

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

        MatchArrayAdapter matchAdapter = new MatchArrayAdapter(this, matches);
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

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
