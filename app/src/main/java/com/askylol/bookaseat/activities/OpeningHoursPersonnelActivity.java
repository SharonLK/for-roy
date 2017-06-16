package com.askylol.bookaseat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askylol.bookaseat.R;
import com.askylol.bookaseat.utils.OpeningHours;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Sharon on 16-Jun-17.
 */

public class OpeningHoursPersonnelActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening_hours);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.nav_opening);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        LinearLayout openings = (LinearLayout) findViewById(R.id.opening_layout);
        LinearLayout days = (LinearLayout) findViewById(R.id.days);

        for (OpeningHours.Day day : OpeningHours.Day.values()) {
            ArrayList<Integer> tmp = intent.getIntegerArrayListExtra(day.toString());
            EditText textView = new EditText(OpeningHoursPersonnelActivity.this);
            EditText dayTextView = new EditText(OpeningHoursPersonnelActivity.this);
            String s = String.format(Locale.US, "%s:", day.toString());
            dayTextView.setText(s);
            dayTextView.setTextSize(16);
            dayTextView.setPadding(15, 15, 15, 15);
            days.addView(dayTextView);
            if (tmp != null) {
                s = String.format(Locale.US, "%02d:%02d - %02d:%02d", tmp.get(0), tmp.get(1), tmp.get(2), tmp.get(3));
            } else {
                s = String.format(Locale.US, "%s", getString(R.string.closed));
            }
            textView.setText(s);
            textView.setTextSize(16);
            textView.setPadding(15, 15, 15, 15);
            openings.addView(textView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateButtonOnClick(View view) {

    }

    public void revertButtonOnClick(View view) {

    }
}
