package com.mmmoussa.iqra.netcomm;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mmmoussa.iqra.R;
import com.mmmoussa.iqra.SearchResultsActivity;
import com.mmmoussa.iqra.objects.Ayah;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;

public class RequestDelegate {
    private final String TAG = RequestDelegate.class.getName();

    private final String REQUEST_SEARCH_URL = "https://api.iqraapp.com/api/v3.0/search";

    private RequestDelegate instance = null;
    private Context context;

    private RequestDelegate (Context context) {
        this.context = context;
    }

    public RequestDelegate getInstance (Context context) {
        if (instance == null) {
            instance = new RequestDelegate(context);
        } else {
            this.context = context;
        }

        return instance;
    }


    /**
     *
     *
     * @param query
     * @param translation
     * @param API_KEY
     * @return
     */
    public List<Ayah> performSearchQuery (String query, String translation, String API_KEY) {
        ByteArrayEntity entity = null;

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("arabicText", query);
            jsonParams.put("translation", translation);
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
            client.post(context, REQUEST_SEARCH_URL, entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // called when response HTTP status is "200 OK"
                    Log.v(TAG, response.toString());
                    try {
                        JSONObject result = response.getJSONObject("result");
                        JSONArray matches = result.getJSONArray("matches");

                        int numOfMatches = matches.length();
                        if (numOfMatches == 0) {

                        } else {
                            if (numOfMatches > 150) {
                                JSONArray shortenedMatches = new JSONArray();
                                for (int i = 0; i < 150; i++) {
                                    shortenedMatches.put(matches.get(i));
                                }
                                result.put("matches", shortenedMatches);
                            }
                            Log.d(TAG, "Number of matches: " + numOfMatches);
                        }
                    } catch (JSONException je) {
                        Log.e("API result problem: ", je.getMessage());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    Log.e("API result problem: ", e.getMessage());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    String errorMessage = e.getMessage();
                    if (errorMessage == null) {
                        Log.e("API result problem: ", "Socket Timeout");
                    } else {
                        Log.e("API result problem: ", errorMessage);
                    }
                }
            });
        }

        return null;
    }
}
