package com.askylol.bookaseat.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.askylol.bookaseat.R;
import com.askylol.bookaseat.logic.Library;
import com.askylol.bookaseat.logic.Reservation;
import com.askylol.bookaseat.logic.Seat;
import com.askylol.bookaseat.logic.User;
import com.askylol.bookaseat.utils.CalendarUtils;
import com.askylol.bookaseat.utils.Data;
import com.askylol.bookaseat.utils.LocationService;
import com.askylol.bookaseat.utils.OpeningHours;
import com.askylol.bookaseat.utils.Pair;
import com.askylol.bookaseat.utils.Point;
import com.askylol.bookaseat.utils.TimeOfDay;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qozix.tileview.TileView;
import com.qozix.tileview.hotspots.HotSpot;
import com.qozix.tileview.widgets.ZoomPanLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static final String SELECTED_DATE_TIME_KEY = "selectedDateTimeKey";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2247;
    public static final int NOTIFICATION_CLICK = 0, NOTIFICATION_YES = 1, NOTIFICATION_NO = 2;
    private ActionBarDrawerToggle mDrawerToggle;
    private TileView tileView;

    private List<View> views = new ArrayList<>();

    private Calendar selectedDateTime = Calendar.getInstance();
    private Timer trackTimer = new Timer();

    ValueEventListener libraryChangedListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Data.INSTANCE.library = dataSnapshot.getValue(Library.class);
            Data.INSTANCE.library.setLibraryRef(dataSnapshot.getRef());
            updateTileViewViews();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // TODO: handle errors
        }
    };
    private BroadcastReceiver locationService = new LocationService();
    private BroadcastReceiver wifiReceiver = new WifiBroadcastReceiver();

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedDateTime = Calendar.getInstance();
        initializeStartTimeButton();

        updateDate((Button) findViewById(R.id.date_button));

        tileView = (TileView) findViewById(R.id.tile_view);
        tileView.setSize(4000, 2000);
        tileView.setMinimumScaleMode(ZoomPanLayout.MinimumScaleMode.FIT);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference libraryRef = database.getReference("libraries").child("library5");

        libraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Data.INSTANCE.library = dataSnapshot.getValue(Library.class);
                Data.INSTANCE.library.setLibraryRef(libraryRef);
                tileView.addDetailLevel(1.0f, "tile-%d_%d.png", 256, 256);
                libraryChangedListener.onDataChange(dataSnapshot);
                libraryRef.addValueEventListener(libraryChangedListener);

                if (Data.INSTANCE.library.isAdmin(Data.INSTANCE.mail)) {
                    ((NavigationView) findViewById(R.id.nav_view)).getMenu().clear();
                    ((NavigationView) findViewById(R.id.nav_view)).inflateMenu(R.menu.activity_admin_drawer);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: handle error? logging?
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.nav_library);
            actionBar.setDisplayHomeAsUpEnabled(true);
            mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {

                public void onDrawerClosed(View view) {
                    supportInvalidateOptionsMenu();
                    navigationView.setCheckedItem(R.id.nav_library);
                    //drawerOpened = false;
                }

                public void onDrawerOpened(View drawerView) {
                    supportInvalidateOptionsMenu();
                    navigationView.setCheckedItem(R.id.nav_library);
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
                        case R.id.nav_log_out:
                            c = null;
                            signOut();
                            break;
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
                            if (Data.INSTANCE.library.isAdmin(Data.INSTANCE.mail)) {
                                c = SettingsPersonnelActivity.class;
                            } else {
                                c = SettingsActivity.class;
                            }
                            break;
                        case R.id.nav_admin_management:
                            c = AdminManagementActivity.class;
                            break;
                        default:
                            c = null;
                            break;
                    }
                    if (c != null) {
                        Intent intent = new Intent(MainActivity.this, c);
                        if (c == OpeningHoursActivity.class) {
                            Map<String, Pair<TimeOfDay, TimeOfDay>> ws = Data.INSTANCE.library.getOpeningHours().getWeeklySchedule();
                            for (OpeningHours.Day day : OpeningHours.Day.values()) {
                                Pair<TimeOfDay, TimeOfDay> hPair = ws.get(day.toString());
                                ArrayList<Integer> tmp;
                                if (hPair != null && hPair.first != null && hPair.second != null) {
                                    tmp = new ArrayList<>();
                                    tmp.add(hPair.first.hour);
                                    tmp.add(hPair.first.minute);
                                    tmp.add(hPair.second.hour);
                                    tmp.add(hPair.second.minute);
                                } else {
                                    tmp = null;
                                }
                                intent.putIntegerArrayListExtra(day.toString(), tmp);
                            }
                        }
                        startActivity(intent);
                    }
                    drawerLayout.closeDrawers();
                    return true;
                }
            });
        }

        checkWifiAndLocationPermissions();


        //TODO: do we need to check for other permissions?
        if (ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_COARSE_LOCATION);
        } else {
            setupTimer();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        Log.d("HI", "id: " + getString(R.string.server_client_id));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        // TODO: add something? (YL)
                        Log.d("HI", "Connection failed... connection result: " + connectionResult);
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleApiClient.connect();

    }

    private void checkWifiAndLocationPermissions() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        View warning_message = findViewById(R.id.warning_message_text_view);
        if (wifi.isWifiEnabled()) {
            warning_message.setVisibility(View.GONE);
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M
                    && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.warning_gps_off)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage(R.string.warning_presence_not_identified)
                                        .setNeutralButton(R.string.ok, null)
                                        .show();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        } else {
            warning_message.setVisibility(View.VISIBLE);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(SELECTED_DATE_TIME_KEY, selectedDateTime.getTimeInMillis());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        selectedDateTime.setTimeInMillis(savedInstanceState.getLong(SELECTED_DATE_TIME_KEY));
        updateDate((Button) findViewById(R.id.date_button));
        updateTime((Button) findViewById(R.id.start_time_button));
    }

    private void updateTime(Button timeButton) {
        timeButton.setText(CalendarUtils.getTimeString(selectedDateTime));
    }

    private void updateDate(Button dateButton) {
        dateButton.setText(CalendarUtils.getDateString(selectedDateTime));
    }

    private void updateTileViewViews() {
        if (Data.INSTANCE.library == null) {
            return;
        }

        for (View view : views) {
            tileView.removeView(view);
        }

        for (Map.Entry<String, Seat> entry : Data.INSTANCE.library.getIdToSeat().entrySet()) {
            Seat seat = entry.getValue();
            final Point location = seat.getLocation();
            final String id = entry.getKey();

            Button dateButton = (Button) findViewById(R.id.date_button);
            final String[] date = dateButton.getText().toString().split("\\.");

            Calendar selectedCalendar = (Calendar) selectedDateTime.clone();
            selectedCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
            selectedCalendar.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
            selectedCalendar.set(Calendar.YEAR, Integer.parseInt(date[2]));

            final boolean reservedByUser = (Data.INSTANCE.library.reservationByUser(selectedCalendar, Data.INSTANCE.mail) != null);
            final boolean free = Data.INSTANCE.library.isSeatFree(id, selectedCalendar); // TODO

            HotSpot hotSpot = new HotSpot();
            hotSpot.setTag(this);
            hotSpot.set(location.x - 50, location.y - 50, location.x + 50, location.y + 50);

            tileView.addHotSpot(hotSpot);

            RelativeLayout relativeLayout = new RelativeLayout(this);
            ImageView logo = new ImageView(this);

            final Reservation reservation = Data.INSTANCE.library.reservationByUser(id, selectedCalendar, Data.INSTANCE.mail);

            if (reservation != null) {
                logo.setImageResource(reservation.isOccupied() ?
                        R.drawable.chair_icon_occupied_current :
                        R.drawable.chair_icon_reserved_current);

                hotSpot.setHotSpotTapListener(new HotSpot.HotSpotTapListener() {
                    @Override
                    public void onHotSpotTap(HotSpot hotSpot, int x, int y) {
                        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.free_seat_question)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Data.INSTANCE.library.removeReservation(id,
                                                CalendarUtils.getDateString(selectedDateTime).replace('.', '_'),
                                                reservation);
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .create();

                        dialog.show();
                        doKeepDialog(dialog);
                    }
                });
            } else if (!free) {
                logo.setImageResource(R.drawable.chair_icon_occupied);
            } else if (reservedByUser) {
                logo.setImageResource(R.drawable.chair_unavailable);
            } else {
                logo.setImageResource(R.drawable.chair_icon);

                hotSpot.setHotSpotTapListener(new HotSpot.HotSpotTapListener() {
                    @Override
                    public void onHotSpotTap(HotSpot hotSpot, int x, int y) {
                        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.reserve_a_seat)
                                .setView(R.layout.dialog_reservation)
                                .create();

                        dialog.show();
                        doKeepDialog(dialog);

                        final TimeOfDay endTime = CalendarUtils.getTimeOfDay(selectedDateTime).add(1, 0);

                        ((TextView) dialog.findViewById(R.id.startTimeValue)).setText(CalendarUtils.getTimeString(selectedDateTime));
                        initializeTimeButton(dialog, (Button) dialog.findViewById(R.id.endTimeButton), endTime, id);

                        Button reserveButton = (Button) dialog.findViewById(R.id.reserveButton);
                        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);

                        TextView nextReservationTextView = (TextView) dialog.findViewById(R.id.next_reservation_message);

                        if (reserveButton == null || cancelButton == null || nextReservationTextView == null) {
                            return;
                        }

                        Reservation nearestReservation = Data.INSTANCE.library.getNearestReservation(id,
                                CalendarUtils.getDateString(selectedDateTime).replace(".", "_"),
                                CalendarUtils.getTimeOfDay(selectedDateTime));

                        if (nearestReservation == null) {
                            nextReservationTextView.setText(getString(R.string.next_reservation_not_exists_message));
                        } else {
                            nextReservationTextView.setText(String.format(Locale.US, getString(R.string.next_reservation_exists_message),
                                    nearestReservation.getStart().hour,
                                    nearestReservation.getStart().minute,
                                    nearestReservation.getEnd().hour,
                                    nearestReservation.getEnd().minute));
                        }

                        reserveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Data.INSTANCE.library.reserve(
                                        id,
                                        new User(Data.INSTANCE.mail),
                                        CalendarUtils.getDateString(selectedDateTime),
                                        CalendarUtils.getTimeOfDay(selectedDateTime),
                                        endTime);
                                dialog.dismiss();
                            }
                        });

                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        recalculateInfoLabel(dialog, id);
                    }
                });
            }

            RelativeLayout.LayoutParams logoLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            logoLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
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
        DatePickerDialog mDatePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDateTime.set(year, month, dayOfMonth);
                updateDate(dateButton);
                updateTileViewViews();
            }
        }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH));
        DatePicker dp = mDatePicker.getDatePicker();
        Calendar mCurrentTime = Calendar.getInstance();
        dp.setMinDate(mCurrentTime.getTimeInMillis() - 1000);
        mCurrentTime.add(Calendar.DATE, 6);
        dp.setMaxDate(mCurrentTime.getTimeInMillis());
        mDatePicker.setTitle("Select Date");
        mDatePicker.show();
    }

    public void onStartTimeClick(View view) {
        final Button timeButton = (Button) view;
        TimePickerDialog mTimePicker =
                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                        selectedDateTime.set(Calendar.MINUTE, selectedMinute);
                        updateTime(timeButton);
                        updateTileViewViews();
                    }
                }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    private void initializeStartTimeButton() {
        TimeOfDay startTime = CalendarUtils.getTimeOfDay(selectedDateTime);
        startTime.minute = (int) Math.ceil(startTime.minute / 15.0) * 15;
        startTime.hour = startTime.hour + (startTime.minute > 45 ? 1 : 0);

        // Make sure we don't have time like 14:60, but instead 14:00
        if (startTime.minute >= 60) {
            startTime.minute = 0;
        }

        selectedDateTime.set(Calendar.HOUR_OF_DAY, startTime.hour);
        selectedDateTime.set(Calendar.MINUTE, startTime.minute);

        final Button button = (Button) findViewById(R.id.start_time_button);

        button.setText(CalendarUtils.getTimeString(startTime));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog timePickerDialog = new AlertDialog.Builder(MainActivity.this)
                        .setView(R.layout.time_spinner)
                        .setNegativeButton(R.string.cancel, null)
                        .create();

                timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE, getString(R.string.set), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NumberPicker hoursPicker = (NumberPicker) timePickerDialog.findViewById(R.id.hours);
                        NumberPicker minutesPicker = (NumberPicker) timePickerDialog.findViewById(R.id.minutes);

                        if (hoursPicker == null || minutesPicker == null) {
                            return;
                        }

                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hoursPicker.getValue());
                        selectedDateTime.set(Calendar.MINUTE, minutesPicker.getValue() * 15);
                        button.setText(CalendarUtils.getTimeString(selectedDateTime));
                        updateTileViewViews();
                    }
                });

                timePickerDialog.show();

                NumberPicker minutesPicker = (NumberPicker) timePickerDialog.findViewById(R.id.minutes);
                NumberPicker hoursPicker = (NumberPicker) timePickerDialog.findViewById(R.id.hours);

                if (minutesPicker == null || hoursPicker == null) {
                    return;
                }

                TimeOfDay startTime = CalendarUtils.getTimeOfDay(selectedDateTime);

                int quarter = (int) Math.ceil(startTime.minute / 15.0);
                minutesPicker.setMinValue(0);
                minutesPicker.setMaxValue(3);
                minutesPicker.setDisplayedValues(new String[]{"00", "15", "30", "45"});
                minutesPicker.setValue(quarter == 4 ? 0 : quarter);
                hoursPicker.setMinValue(0);
                hoursPicker.setMaxValue(23);
                hoursPicker.setDisplayedValues(new String[]{"00", "01", "02", "03",
                        "04", "05", "06", "07", "08", "09", "10", "11", "12", "13",
                        "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"});
                hoursPicker.setValue(startTime.hour + (startTime.minute > 45 ? 1 : 0));
            }
        });
    }

    private void initializeTimeButton(final AlertDialog alertDialog, final Button button, final TimeOfDay timeOfDay, final String seatId) {
        timeOfDay.minute = (int) Math.ceil(timeOfDay.minute / 15.0) * 15;
        timeOfDay.hour = timeOfDay.hour + (timeOfDay.minute > 45 ? 1 : 0);

        // Make sure we don't have time like 14:60, but instead 14:00
        if (timeOfDay.minute >= 60) {
            timeOfDay.minute = 0;
        }

        button.setText(CalendarUtils.getTimeString(timeOfDay));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog timePickerDialog = new AlertDialog.Builder(MainActivity.this)
                        .setView(R.layout.time_spinner)
                        .setNegativeButton(R.string.cancel, null)
                        .create();

                timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE, getString(R.string.set), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NumberPicker hoursPicker = (NumberPicker) timePickerDialog.findViewById(R.id.hours);
                        NumberPicker minutesPicker = (NumberPicker) timePickerDialog.findViewById(R.id.minutes);

                        if (hoursPicker == null || minutesPicker == null) {
                            return;
                        }

                        timeOfDay.hour = hoursPicker.getValue();
                        timeOfDay.minute = minutesPicker.getValue() * 15;
                        button.setText(CalendarUtils.getTimeString(timeOfDay));

                        recalculateInfoLabel(alertDialog, seatId);
                    }
                });

                timePickerDialog.show();

                NumberPicker minutesPicker = (NumberPicker) timePickerDialog.findViewById(R.id.minutes);
                NumberPicker hoursPicker = (NumberPicker) timePickerDialog.findViewById(R.id.hours);

                if (minutesPicker == null || hoursPicker == null) {
                    return;
                }

                int quarter = (int) Math.ceil(timeOfDay.minute / 15.0);
                minutesPicker.setMinValue(0);
                minutesPicker.setMaxValue(3);
                minutesPicker.setDisplayedValues(new String[]{"00", "15", "30", "45"});
                minutesPicker.setValue(quarter == 4 ? 0 : quarter);
                hoursPicker.setMinValue(0);
                hoursPicker.setMaxValue(23);
                hoursPicker.setDisplayedValues(new String[]{"00", "01", "02", "03",
                        "04", "05", "06", "07", "08", "09", "10", "11", "12", "13",
                        "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"});
                hoursPicker.setValue(timeOfDay.hour + (timeOfDay.minute > 45 ? 1 : 0));
            }
        });
    }

    private void recalculateInfoLabel(AlertDialog dialog, String seatId) {
        TextView label = (TextView) dialog.findViewById(R.id.infoLabel);

        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
        Button reserveButton = (Button) dialog.findViewById(R.id.reserveButton);

        Button endTimeButton = (Button) dialog.findViewById(R.id.endTimeButton);

        if (label == null || cancelButton == null || reserveButton == null) {
            return;
        }

        String endTime[] = endTimeButton.getText().toString().split(":");

        TimeOfDay startTime = CalendarUtils.getTimeOfDay(selectedDateTime);

        long startMins = startTime.hour * 60 + startTime.minute;
        long endMins = Integer.parseInt(endTime[0]) * 60 + Integer.parseInt(endTime[1]);

        long diff = endMins - startMins;
        long hours = diff / 60;
        long mins = diff % 60;

        Calendar startCalendar = (Calendar) selectedDateTime.clone();
        startCalendar.set(Calendar.HOUR_OF_DAY, startTime.hour);
        startCalendar.set(Calendar.MINUTE, startTime.minute);
        Calendar endCalendar = (Calendar) selectedDateTime.clone();
        endCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));
        endCalendar.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));

        if (diff <= 0) {
            label.setText(R.string.invalid_duration);
            label.setTextColor(Color.RED);
            reserveButton.setEnabled(false);
        } else if (!Data.INSTANCE.library.isSeatFree(seatId, startCalendar, endCalendar)) {
            label.setText(R.string.seat_already_reserved);
            label.setTextColor(Color.RED);
            reserveButton.setEnabled(false);
        } else if (startCalendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() - 1000 * 60) {
            label.setText(R.string.cant_reserve_past);
            label.setTextColor(Color.RED);
            reserveButton.setEnabled(false);
        } else {
            label.setText(String.format(getString(R.string.duration_message), hours, mins));
            label.setTextColor(Color.BLACK);
            reserveButton.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.locationing_disabled_title)
                        .setMessage(R.string.locationing_disabled_message)
                        .setNeutralButton(R.string.ok, null)
                        .create();

                dialog.show();
            } else {
                setupTimer();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setupTimer() {
        trackTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("22222222222222222222");

                ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).startScan();

                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        updateTileViewViews();
                    }
                });
            }
        }, 2000, 3500);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(wifiReceiver);
//        unregisterReceiver(locationService);
        Data.INSTANCE.isInForeground.set(false);
        System.out.println("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH " + Data.INSTANCE.isInForeground);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        registerReceiver(locationService, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        Data.INSTANCE.isInForeground.set(true);
        System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF " + Data.INSTANCE.isInForeground);

        ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).startScan();
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);

        trackTimer.cancel();
        Data.reset();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("notificationStatus")) {
            ((NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE))
                    .cancel(LocationService.NOTIFICATION_ID);
            Library library = Data.INSTANCE.library;

            if (library == null) {
                return;
            }

            final Calendar now = Calendar.getInstance();
            Pair<String, Reservation> reservationPair = library.reservationByUser(now, Data.INSTANCE.mail);
            if (reservationPair == null) {
                getDismissableSnackbar(getString(R.string.reservation_expired)).show();
                return;
            }

            final String seatId = reservationPair.first;
            final Reservation reservation = reservationPair.second;

            switch (intent.getIntExtra("notificationStatus", -1)) {
                case NOTIFICATION_YES:
                    showReservedSnackbar();
                    break;
                case NOTIFICATION_NO:
                    endReservation(now, seatId, reservation);

                    if (LocationService.timer != null) {
                        LocationService.timer.cancel();
                    }

                    break;
                case NOTIFICATION_CLICK:
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.notification_title)
                            .setMessage(R.string.notification_content_short)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showReservedSnackbar();
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    endReservation(now, seatId, reservation);
                                }
                            })
                            .create();

                    dialog.show();
                    doKeepDialog(dialog);
                    break;
                default:
                    break;
            }
        }
    }

    private void endReservation(Calendar now, String seatId, Reservation reservation) {
        reservation.setEnd(CalendarUtils.getTimeOfDay(now));
        Data.INSTANCE.library.updateReservation(seatId, reservation); //TODO: check me
        showFreedSnackbar();
    }

    private Snackbar getDismissableSnackbar(String content) {
        return Snackbar.make(
                findViewById(android.R.id.content),
                content,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.dismiss), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
    }

    private void showFreedSnackbar() {
        getDismissableSnackbar(getString(R.string.seat_freed)).show();
    }

    private void showReservedSnackbar() {
        getDismissableSnackbar(
                String.format(
                        getString(R.string.reserved_for_x_minutes),
                        Data.INSTANCE.library.getIdleLimit()
                )
        ).show();
    }

    private void doKeepDialog(Dialog dialog) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
    }

    public class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkWifiAndLocationPermissions();
        }
    }
}
