package com.askylol.bookaseat.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.widget.TextView;

import com.askylol.bookaseat.R;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.nav_about_us);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textView = (TextView)findViewById(R.id.sofa_credit_view);
        String s = String.format(Locale.US, "%s Freepik %s www.flaticon.com", getString(R.string.sofa_credit), getString(R.string.from));
        textView.setText(s);

        Linkify.MatchFilter mMatchFilter = new Linkify.MatchFilter() {
            public final boolean acceptMatch(CharSequence s, int start, int end) {
                return true;
            }
        };
        Linkify.TransformFilter mTransformFilter = new Linkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return "www.freepik.com";
            }
        };

        Linkify.TransformFilter mTransformFilter2 = new Linkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return "www.flaticon.com";
            }
        };

        Pattern mPattern = Pattern.compile("Freepik");
        Pattern mPattern2 = Pattern.compile("www.flaticon.com");
        String scheme = "http://";
        Linkify.addLinks(textView, mPattern, scheme, mMatchFilter, mTransformFilter);
        Linkify.addLinks(textView, mPattern2, scheme, mMatchFilter, mTransformFilter2);
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
}
