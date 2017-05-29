package com.askylol.bookaseat.activities;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.askylol.bookaseat.R;
import com.askylol.bookaseat.logic.Library;
import com.askylol.bookaseat.logic.Seat;
import com.askylol.bookaseat.utils.Point;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qozix.tileview.TileView;
import com.qozix.tileview.hotspots.HotSpot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle mDrawerToggle;
    private Library library;
    private TileView tileView;

    private List<View> views = new ArrayList<>();

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

        final TextView occupancyText = (TextView) findViewById(R.id.occupancy_textview);
        Calendar mCurrentTime = Calendar.getInstance();
        occupancyText.setText(
                String.format(
                        getString(R.string.occupancy_time),
                        mCurrentTime.get(Calendar.HOUR),
                        mCurrentTime.get(Calendar.MINUTE)
                ));
        occupancyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Calendar mCurrentTime = Calendar.getInstance();
                TimePickerDialog mTimePicker =
                        new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                ((TextView) v).setText(String.format(
                                        getString(R.string.occupancy_time), selectedHour, selectedMinute));
                            }
                        }, mCurrentTime.get(Calendar.HOUR_OF_DAY), mCurrentTime.get(Calendar.MINUTE), true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        tileView = (TileView) findViewById(R.id.tile_view);
        tileView.setSize(3484, 2332);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference libraryRef = database.getReference("libraries").child("library5");

        libraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                library = dataSnapshot.getValue(Library.class);
                System.out.println(library.getOpeningHours());
                library.setLibraryRef(libraryRef);
                tileView.addDetailLevel(1.0f, "tile-%d_%d.jpg", 256, 256);
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

    private void updateTileViewViews() {
        for (View view : views) {
            tileView.removeView(view);
        }

        for (Map.Entry<String, Seat> entry : library.getIdToSeat().entrySet()) {
            Seat seat = entry.getValue();
            final Point location = seat.getLocation();
            final String id = entry.getKey();

            final boolean free = seat.getStatus() == Seat.Status.FREE;

            HotSpot hotSpot = new HotSpot();
            hotSpot.setTag(this);
            hotSpot.set(location.x - 50, location.y - 50, location.x + 50, location.y + 50);
            hotSpot.setHotSpotTapListener(new HotSpot.HotSpotTapListener() {
                @Override
                public void onHotSpotTap(HotSpot hotSpot, int x, int y) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Chair Reservation")
                            .setPositiveButton(free ? "RESERVE" : "FREE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (free) {
                                        library.reserve(id, null); // TODO: Update to real user
                                    } else {
                                        library.free(id);
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            });

            tileView.addHotSpot(hotSpot);

            RelativeLayout relativeLayout = new RelativeLayout(this);
            ImageView logo = new ImageView(this);
            logo.setImageResource(library.getSeat(id).getStatus() == Seat.Status.FREE ? R.drawable.chair_icon : R.drawable.chair_icon_occupied);
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
}
