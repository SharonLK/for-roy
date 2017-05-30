package com.askylol.bookaseat.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import com.askylol.bookaseat.R;
import com.askylol.bookaseat.logic.Library;
import com.askylol.bookaseat.logic.Seat;
import com.askylol.bookaseat.logic.User;
import com.askylol.bookaseat.utils.Point;
import com.askylol.bookaseat.utils.TimeOfDay;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qozix.tileview.TileView;
import com.qozix.tileview.hotspots.HotSpot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle mDrawerToggle;
    private Library library;
    private TileView tileView;

    private List<View> views = new ArrayList<>();

    private Calendar selectedTime = Calendar.getInstance();

    ValueEventListener libraryChangedListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            library = dataSnapshot.getValue(Library.class);
            library.setLibraryRef(dataSnapshot.getRef());
            updateTileViewViews();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // TODO: handle errors
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedTime = Calendar.getInstance();

        updateHoursAndMinutes((Button) findViewById(R.id.time_button));
        updateDate((Button) findViewById(R.id.date_button));

        tileView = (TileView) findViewById(R.id.tile_view);
        tileView.setSize(4000, 2000);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference libraryRef = database.getReference("libraries").child("library5");

        libraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                library = dataSnapshot.getValue(Library.class);
                library.setLibraryRef(libraryRef);
                tileView.addDetailLevel(1.0f, "tile-%d_%d.png", 256, 256);
                libraryChangedListener.onDataChange(dataSnapshot);
                libraryRef.addValueEventListener(libraryChangedListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: handle error? logging?
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.nav_library);
            actionBar.setDisplayHomeAsUpEnabled(true);
            mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {

                public void onDrawerClosed(View view) {
                    supportInvalidateOptionsMenu();
                    //drawerOpened = false;
                }

                public void onDrawerOpened(View drawerView) {
                    supportInvalidateOptionsMenu();
                    //drawerOpened = true;
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            drawerLayout.addDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();

            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Class c;
                    switch (item.getItemId()) {
                        case R.id.nav_about_us:
                            c = AboutActivity.class;
                            break;
                        case R.id.nav_opening:
                            c = OpeningHoursActivity.class;
                            break;
                        case R.id.nav_orders:
                            c = MyOrdersActivity.class;
                            break;
                        case R.id.nav_settings:
                            c = SettingsActivity.class;
                            break;
                        default:
                            c = null;
                            break;
                    }
                    if (c != null) {
                        startActivity(new Intent(MainActivity.this, c));
                        drawerLayout.closeDrawers();
                    }
                    return true;
                }
            });
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void updateHoursAndMinutes(Button hoursAndMinutesButton) {
        hoursAndMinutesButton.setText(new SimpleDateFormat("HH:mm").format(selectedTime.getTimeInMillis()));
    }

    private void updateDate(Button dateButton) {
        dateButton.setText(new SimpleDateFormat("dd.MM.yyyy").format(selectedTime.getTimeInMillis()));
    }

    private void updateTileViewViews() {
        for (View view : views) {
            tileView.removeView(view);
        }

        for (Map.Entry<String, Seat> entry : library.getIdToSeat().entrySet()) {
            Seat seat = entry.getValue();
            final Point location = seat.getLocation();
            final String id = entry.getKey();

            final boolean free = library.isSeatFree(id, "29_5_17", new TimeOfDay(selectedTime.get(Calendar.HOUR), selectedTime.get(Calendar.MINUTE))); // TODO

            HotSpot hotSpot = new HotSpot();
            hotSpot.setTag(this);
            hotSpot.set(location.x - 50, location.y - 50, location.x + 50, location.y + 50);
            if (free) {
                hotSpot.setHotSpotTapListener(new HotSpot.HotSpotTapListener() {
                    @Override
                    public void onHotSpotTap(HotSpot hotSpot, int x, int y) {
                        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Reserve a Seat")
                                .setView(R.layout.dialog_reservation)
                                .create();

                        dialog.show();

                        final TimeOfDay startTime = new TimeOfDay(selectedTime.get(Calendar.HOUR), selectedTime.get(Calendar.MINUTE));
                        final TimeOfDay endTime = startTime.add(1, 0);

                        final Button startTimeButton = (Button) dialog.findViewById(R.id.startTimeButton);
                        startTimeButton.setText(String.format("%02d:%02d", startTime.hour, startTime.minute));
                        startTimeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                        startTime.hour = selectedHour;
                                        startTime.minute = selectedMinute;
                                        startTimeButton.setText(selectedHour + ":" + selectedMinute);
                                    }
                                }, startTime.hour, startTime.minute, true);
                                timePickerDialog.setTitle("Select Time");
                                timePickerDialog.show();
                            }
                        });

                        final Button endTimeButton = (Button) dialog.findViewById(R.id.endTimeButton);
                        endTimeButton.setText(String.format("%02d:%02d", endTime.hour, endTime.minute));
                        endTimeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                        endTime.hour = selectedHour;
                                        endTime.minute = selectedMinute;
                                        endTimeButton.setText(selectedHour + ":" + selectedMinute);
                                    }
                                }, endTime.hour, endTime.minute, true);
                                timePickerDialog.setTitle("Select Time");
                                timePickerDialog.show();
                            }
                        });

                        Button reserveButton = (Button) dialog.findViewById(R.id.reserveButton);
                        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
//                        TextView nearestReservationLabel = (TextView) dialog.findViewById(R.id.nearestReservationLabel);
//
//                        Reservation nearestReservation = library.getNearestReservation(id, "29_5_17", selectedTime);
//                        if (nearestReservation == null) {
//                            nearestReservationLabel.setText("No other reservation today, titparea!");
//                        } else {
//                            nearestReservationLabel.setText("Nearest reservation starts at: " + nearestReservation.getStart());
//                        }

                        reserveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                library.reserve(id, new User("ylev"), startTime, endTime); // TODO: Update to real user
                                dialog.dismiss();
                            }
                        });

                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
            }

            tileView.addHotSpot(hotSpot);

            RelativeLayout relativeLayout = new RelativeLayout(this);
            ImageView logo = new ImageView(this);
            logo.setImageResource(free ? R.drawable.chair_icon : R.drawable.chair_icon_occupied);
            RelativeLayout.LayoutParams logoLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            logoLayoutParams.leftMargin = location.x - 50;
            logoLayoutParams.topMargin = location.y - 50;
            logoLayoutParams.width = 100;
            logoLayoutParams.height = 100;
            relativeLayout.addView(logo, logoLayoutParams);
            tileView.addScalingViewGroup(relativeLayout);
            views.add(relativeLayout);
        }
    }

    public void onDateClick(View view) {
        final Button dateButton = (Button) view;
        Calendar mCurrentTime = Calendar.getInstance();
        DatePickerDialog mDatePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedTime.set(year, month, dayOfMonth);
                updateDate(dateButton);
            }
        }, mCurrentTime.get(Calendar.YEAR), mCurrentTime.get(Calendar.MONTH), mCurrentTime.get(Calendar.DAY_OF_MONTH));
        DatePicker dp = mDatePicker.getDatePicker();
        dp.setMinDate(mCurrentTime.getTimeInMillis());
        mCurrentTime.add(Calendar.DATE, 6);
        dp.setMaxDate(mCurrentTime.getTimeInMillis());
        mDatePicker.setTitle("Select Date");
        mDatePicker.show();
    }

    public void onTimeClick(View view) {
        final Button timeButton = (Button) view;
        Calendar mCurrentTime = Calendar.getInstance();
        TimePickerDialog mTimePicker =
                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        selectedTime.set(Calendar.HOUR, selectedHour);
                        selectedTime.set(Calendar.MINUTE, selectedMinute);
                        updateHoursAndMinutes(timeButton);
                        updateTileViewViews();
                    }
                }, mCurrentTime.get(Calendar.HOUR_OF_DAY), mCurrentTime.get(Calendar.MINUTE), true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }
}
