package com.mmmoussa.iqra;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mohamed on 2016-03-06.
 */
public class MatchArrayAdapter extends ArrayAdapter<JSONObject> {
    private final Context context;
    private final ArrayList<JSONObject> values;

    public MatchArrayAdapter(Context context, ArrayList<JSONObject> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.single_match_item, parent, false);
        TextView verseNumber = (TextView) rowView.findViewById(R.id.verseNumber);
        TextView arabicSurahNameTV = (TextView) rowView.findViewById(R.id.arabicSurahName);
        TextView translationSurahNameTV = (TextView) rowView.findViewById(R.id.translationSurahName);
        TextView arabicAyahTV = (TextView) rowView.findViewById(R.id.arabicVerse);
        TextView translationAyahTV = (TextView) rowView.findViewById(R.id.translationVerse);
        ImageButton shareButton = (ImageButton) rowView.findViewById(R.id.shareButton);
        LinearLayout hidableVersesLayout = (LinearLayout) rowView.findViewById(R.id.hidableVerses);
        JSONObject valueObj = values.get(position);

        try {
            int surahNum = valueObj.getInt("surahNum");
            final int ayahNum = valueObj.getInt("ayahNum");
            final String arabicSurahName = valueObj.getString("arabicSurahName");
            final String translationSurahName = valueObj.getString("translationSurahName");
            final String arabicAyah = valueObj.getString("arabicAyah");
            final String translationAyah = valueObj.getString("translationAyah");

            verseNumber.setText(getContext().getResources().getString(R.string.chapter_and_verse, surahNum, ayahNum));
            arabicSurahNameTV.setText(arabicSurahName);
            translationSurahNameTV.setText(translationSurahName);
            arabicAyahTV.setText(arabicAyah);
            translationAyahTV.setText(Html.fromHtml(translationAyah));
            hidableVersesLayout.setVisibility(LinearLayout.GONE);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent shareIntent = new Intent(context, ShareResultActivity.class);
                    shareIntent.putExtra("ayahNum", ayahNum);
                    shareIntent.putExtra("arabicSurahName", arabicSurahName);
                    shareIntent.putExtra("translationSurahName", translationSurahName);
                    shareIntent.putExtra("arabicAyah", arabicAyah);
                    shareIntent.putExtra("translationAyah", translationAyah);
                    context.startActivity(shareIntent);
                }
            });
        } catch (JSONException je) {
            Log.e("Set list value error: ", je.getMessage());
        }

        return rowView;
    }
}
