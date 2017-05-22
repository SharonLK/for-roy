package com.askylol.bookaseat.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.askylol.bookaseat.R;
import com.askylol.bookaseat.logic.Library;
import com.askylol.bookaseat.utils.Point;
import com.qozix.tileview.TileView;
import com.qozix.tileview.hotspots.HotSpot;

public class MainActivity extends AppCompatActivity {

    ActionBarDrawerToggle mDrawerToggle;
    Library library;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        library = new Library();

        TileView tileView = (TileView) findViewById(R.id.tile_view);
        tileView.setSize(3484, 2332);
        tileView.addDetailLevel(1.0f, "tile-%d_%d.jpg", 256, 256);

        for (Point point : library.getSeatLocations()) {
            HotSpot hotSpot = new HotSpot();
            hotSpot.setTag(this);
            hotSpot.set(point.x - 50, point.y - 50, point.x + 50, point.y + 50);
            hotSpot.setHotSpotTapListener(new HotSpot.HotSpotTapListener() {
                @Override
                public void onHotSpotTap(HotSpot hotSpot, int x, int y) {
                    System.out.println("A SKY LOLLOLOLOLOLOLOLOLOLOLOLOLOLOLOLOLL");
                }
            });
            // tileView.addMarker()
            tileView.addHotSpot(hotSpot);

            RelativeLayout relativeLayout = new RelativeLayout(this);
            ImageView logo = new ImageView(this);
            logo.setImageResource(R.drawable.chair_icon);
            RelativeLayout.LayoutParams logoLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            logoLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            logoLayoutParams.leftMargin = point.x;
            logoLayoutParams.topMargin = point.y;
            relativeLayout.addView(logo, logoLayoutParams);
            tileView.addScalingViewGroup(relativeLayout);
        }

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
}