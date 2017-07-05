package com.askylol.bookaseat.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
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
import com.askylol.bookaseat.utils.Date;
import com.askylol.bookaseat.utils.DateAndTime;
import com.askylol.bookaseat.utils.Pair;
import com.askylol.bookaseat.utils.TimeOfDay;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

        ListView reservationsListView = (ListView) findViewById(R.id.reservations_list);
        ReservationsAdapter reservationsAdapter = new ReservationsAdapter(this, R.layout.view_reservation);
        reservationsListView.setAdapter(reservationsAdapter);

        ListView historyListView = (ListView) findViewById(R.id.history_list);
        ReservationsAdapter historyAdapter = new ReservationsAdapter(this, R.layout.view_reservation);
        historyListView.setAdapter(historyAdapter);

        Calendar c = Calendar.getInstance();
        DateAndTime dateAndTime = new DateAndTime(
                new Date(c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR)),
                new TimeOfDay(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));

        List<Pair<String, Reservation>> reservationsDates = Data.INSTANCE.library.reservationsByUser(Data.INSTANCE.mail);
        Collections.sort(reservationsDates, new Comparator<Pair<String, Reservation>>() {
            @Override
            public int compare(Pair<String, Reservation> o1, Pair<String, Reservation> o2) {
                if (o1 == null || o2 == null || o1.second == null || o2.second == null) {
                    return 0;
                }

                DateAndTime first = new DateAndTime(new Date(o1.first, "_"), o1.second.getStart());
                DateAndTime second = new DateAndTime(new Date(o2.first, "_"), o2.second.getStart());

                if (first.equals(second)) {
                    return 0;
                }

                return first.before(second) ? -1 : 1;
            }
        });

        if (Data.INSTANCE.library != null) {
            for (Pair<String, Reservation> dateReservation : reservationsDates) {
                String date = dateReservation.first;
                Reservation reservation = dateReservation.second;

                if (dateAndTime.after(new DateAndTime(new Date(date, "_"), reservation.getEnd()))) {
                    historyAdapter.add(dateReservation);
                } else {
                    reservationsAdapter.add(dateReservation);
                }
            }
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

    public void onHistoryButtonClicked(View view) {
        ListView history = (ListView) findViewById(R.id.history_list);
        if (history.getVisibility() == View.GONE) {
            history.setVisibility(View.VISIBLE);
        } else {
            history.setVisibility(View.GONE);
        }
    }

    private class ReservationsAdapter extends ArrayAdapter<Pair<String, Reservation>> {
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

            Pair<String, Reservation> dateReservation = getItem(position);

            if (dateReservation == null) {
                return v;
            }

            String d = dateReservation.first;
            Reservation r = dateReservation.second;

            if (r != null) {
                TextView date = (TextView) v.findViewById(R.id.date_textview);
                TextView time = (TextView) v.findViewById(R.id.from_to_textview);

                if (date != null) {
                    date.setText(d.replaceAll("_", "/"));
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
