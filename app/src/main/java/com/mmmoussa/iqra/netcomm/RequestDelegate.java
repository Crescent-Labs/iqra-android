package com.mmmoussa.iqra.netcomm;

import android.content.Context;
import android.util.Log;

import com.mmmoussa.iqra.objects.Ayah;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mmmoussa.iqra.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;

public class RequestDelegate {
    private final String TAG = RequestDelegate.class.getName();

    private final String API_KEY = BuildConfig.IQRA_API_KEY;
    private final String API_VERSION = "v3.0";

    private final String REQUEST_URL = BuildConfig.IQRA_API_URL;
    private final String REQUEST_SEARCH = "search";
    private final String REQUEST_TRANSLATION = "translations";
    private final String REQUEST_DELIMITER = "/";
    private final String REQUEST_API_PATH = "api" + REQUEST_DELIMITER + API_VERSION;
    private final String REQUEST_SEARCH_URL = REQUEST_URL + REQUEST_API_PATH + REQUEST_DELIMITER + REQUEST_SEARCH;
    private final String REQUEST_TRANSLATION_URL = REQUEST_URL + REQUEST_API_PATH + REQUEST_DELIMITER + REQUEST_TRANSLATION;

    private static RequestDelegate instance = null;
    private Context context;

    private RequestDelegate(Context context) {
        this.context = context;
    }

    public static RequestDelegate getInstance(Context context) {
        if (instance == null) {
            instance = new RequestDelegate(context);
        } else {
            instance.updateContext(context);
        }

        return instance;
    }

    protected void updateContext(Context context) {
        this.context = context;
    }

    public void performSearchQuery(String query, String translation, NetworkRequestCallback callback) {
        JSONObject jsonParams = createSearchQueryParams(query, translation);
        performNetworkingRequest(REQUEST_SEARCH_URL, jsonParams, callback);
    }

    public void performTranslationChange(String translation, Ayah[] ayahs, NetworkRequestCallback callback) {
        JSONObject jsonParams = createTranslationParams(translation, ayahs);
        performNetworkingRequest(REQUEST_TRANSLATION_URL, jsonParams, callback);
    }

    private JSONObject createSearchQueryParams(String query, String translation) {
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("arabicText", query);
            jsonParams.put("translation", translation);
            jsonParams.put("apikey", API_KEY);
        } catch (JSONException je) {
            Log.e(TAG, je.getMessage());
        }
        return jsonParams;
    }

    private JSONArray createAyahParams(Ayah[] ayahs) throws JSONException {
        JSONArray params = new JSONArray();

        for (Ayah ayah : ayahs) {
            JSONObject obj = new JSONObject();
            obj.put("surahNum", ayah.getSurahNum());
            obj.put("ayahNum", ayah.getAyahNum());
            params.put(obj);
        }

        return params;
    }

    private JSONObject createTranslationParams(String translation, Ayah[] ayahs) {
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("apikey", API_KEY);
            jsonParams.put("translation", translation);
            jsonParams.put("ayahs", createAyahParams(ayahs));
        } catch (JSONException je) {
            Log.e(TAG, je.getMessage());
        }
        return jsonParams;
    }

    private void performNetworkingRequest(String requestUrl, JSONObject jsonParams, final NetworkRequestCallback callback) {
        ByteArrayEntity entity = null;

        try {
            entity = new ByteArrayEntity(jsonParams.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ue) {
            Log.e(TAG, ue.getMessage());
        }

        if (entity != null) {
            AsyncHttpClient client = new AsyncHttpClient();
            client.post(context, requestUrl, entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    callback.onSuccess(response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
                    callback.onFailure(e);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                    callback.onFailure(e);
                }
            });
        }
    }
}
