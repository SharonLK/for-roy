package com.askylol.bookaseat.activities;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.askylol.bookaseat.R;
import com.askylol.bookaseat.logic.Reservation;
import com.askylol.bookaseat.utils.Data;

import java.util.Locale;

public class MyOrdersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.nav_orders);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView listView = (ListView) findViewById(R.id.reservations_list);
        ReservationsAdapter adapter = new ReservationsAdapter(this, R.layout.view_reservation);
        listView.setAdapter(adapter);

        for (Reservation reservation : Data.INSTANCE.library.reservationsByUser(Data.INSTANCE.username)) {
            adapter.add(reservation);
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

    private class ReservationsAdapter extends ArrayAdapter<Reservation> {
        public ReservationsAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                v = inflater.inflate(R.layout.view_reservation, null);
            }

            Reservation r = getItem(position);

            if (r != null) {
                TextView date = (TextView) v.findViewById(R.id.date_textview);
                TextView time = (TextView) v.findViewById(R.id.from_to_textview);

                if (date != null) {
                    date.setText("YES");
                }

                if (time != null) {
                    time.setText(String.format(Locale.US,
                            "From %02d:%02d to %02d:%02d",
                            r.getStart().hour,
                            r.getStart().minute,
                            r.getEnd().hour,
                            r.getEnd().minute));
                }
            }

            return v;
        }
    }
}
