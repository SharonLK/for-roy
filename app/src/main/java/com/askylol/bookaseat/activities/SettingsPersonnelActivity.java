package com.askylol.bookaseat.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.askylol.bookaseat.R;
import com.askylol.bookaseat.utils.Data;
import com.askylol.bookaseat.utils.OpeningHours;
import com.askylol.bookaseat.utils.Pair;
import com.askylol.bookaseat.utils.TimeOfDay;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Sharon on 16-Jun-17.
 */

public class SettingsPersonnelActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_personnel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.nav_opening);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setActivityFromOpeningHours(Data.INSTANCE.library.getOpeningHours());
        ((EditText) findViewById(R.id.max_delay_edit_text)).setText(String.valueOf(Data.INSTANCE.library.getMaxDelay()));
        ((EditText) findViewById(R.id.idle_limit_edit_text)).setText(String.valueOf(Data.INSTANCE.library.getIdleLimit()));
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
        Data.INSTANCE.library.updateOpeningHours(openingHoursFromActivity());
        Data.INSTANCE.library.updateMaxDelay(Integer.parseInt(((EditText) findViewById(R.id.max_delay_edit_text)).getText().toString()));
        Data.INSTANCE.library.updateIdleLimit(Integer.parseInt(((EditText) findViewById(R.id.idle_limit_edit_text)).getText().toString()));
    }

    public void revertButtonOnClick(View view) {
        setActivityFromOpeningHours(Data.INSTANCE.library.getOpeningHours());
        ((EditText) findViewById(R.id.max_delay_edit_text)).setText(String.valueOf(Data.INSTANCE.library.getMaxDelay()));
        ((EditText) findViewById(R.id.idle_limit_edit_text)).setText(String.valueOf(Data.INSTANCE.library.getIdleLimit()));
    }

    private Map<OpeningHours.Day, CheckBox> getDayCheckBoxMap() {
        return new HashMap<OpeningHours.Day, CheckBox>() {{
            put(OpeningHours.Day.SUNDAY, (CheckBox) findViewById(R.id.sunday_check_box));
            put(OpeningHours.Day.MONDAY, (CheckBox) findViewById(R.id.monday_check_box));
            put(OpeningHours.Day.TUESDAY, (CheckBox) findViewById(R.id.tuesday_check_box));
            put(OpeningHours.Day.WEDNESDAY, (CheckBox) findViewById(R.id.wednesday_check_box));
            put(OpeningHours.Day.THURSDAY, (CheckBox) findViewById(R.id.thursday_check_box));
            put(OpeningHours.Day.FRIDAY, (CheckBox) findViewById(R.id.friday_check_box));
            put(OpeningHours.Day.SATURDAY, (CheckBox) findViewById(R.id.saturday_check_box));
        }};
    }

    private Map<OpeningHours.Day, EditText> getDayStartTimeHoursMap() {
        return new HashMap<OpeningHours.Day, EditText>() {{
            put(OpeningHours.Day.SUNDAY, (EditText) findViewById(R.id.sunday_start_time_hours_edit_text));
            put(OpeningHours.Day.MONDAY, (EditText) findViewById(R.id.monday_start_time_hours_edit_text));
            put(OpeningHours.Day.TUESDAY, (EditText) findViewById(R.id.tuesday_start_time_hours_edit_text));
            put(OpeningHours.Day.WEDNESDAY, (EditText) findViewById(R.id.wednesday_start_time_hours_edit_text));
            put(OpeningHours.Day.THURSDAY, (EditText) findViewById(R.id.thursday_start_time_hours_edit_text));
            put(OpeningHours.Day.FRIDAY, (EditText) findViewById(R.id.friday_start_time_hours_edit_text));
            put(OpeningHours.Day.SATURDAY, (EditText) findViewById(R.id.saturday_start_time_hours_edit_text));
        }};
    }

    private Map<OpeningHours.Day, EditText> getDayStartTimeMinutesMap() {
        return new HashMap<OpeningHours.Day, EditText>() {{
            put(OpeningHours.Day.SUNDAY, (EditText) findViewById(R.id.sunday_start_time_minutes_edit_text));
            put(OpeningHours.Day.MONDAY, (EditText) findViewById(R.id.monday_start_time_minutes_edit_text));
            put(OpeningHours.Day.TUESDAY, (EditText) findViewById(R.id.tuesday_start_time_minutes_edit_text));
            put(OpeningHours.Day.WEDNESDAY, (EditText) findViewById(R.id.wednesday_start_time_minutes_edit_text));
            put(OpeningHours.Day.THURSDAY, (EditText) findViewById(R.id.thursday_start_time_minutes_edit_text));
            put(OpeningHours.Day.FRIDAY, (EditText) findViewById(R.id.friday_start_time_minutes_edit_text));
            put(OpeningHours.Day.SATURDAY, (EditText) findViewById(R.id.saturday_start_time_minutes_edit_text));
        }};
    }

    private Map<OpeningHours.Day, EditText> getDayEndTimeHoursMap() {
        return new HashMap<OpeningHours.Day, EditText>() {{
            put(OpeningHours.Day.SUNDAY, (EditText) findViewById(R.id.sunday_end_time_hours_edit_text));
            put(OpeningHours.Day.MONDAY, (EditText) findViewById(R.id.monday_end_time_hours_edit_text));
            put(OpeningHours.Day.TUESDAY, (EditText) findViewById(R.id.tuesday_end_time_hours_edit_text));
            put(OpeningHours.Day.WEDNESDAY, (EditText) findViewById(R.id.wednesday_end_time_hours_edit_text));
            put(OpeningHours.Day.THURSDAY, (EditText) findViewById(R.id.thursday_end_time_hours_edit_text));
            put(OpeningHours.Day.FRIDAY, (EditText) findViewById(R.id.friday_end_time_hours_edit_text));
            put(OpeningHours.Day.SATURDAY, (EditText) findViewById(R.id.saturday_end_time_hours_edit_text));
        }};
    }

    private Map<OpeningHours.Day, EditText> getDayEndTimeMinutesMap() {
        return new HashMap<OpeningHours.Day, EditText>() {{
            put(OpeningHours.Day.SUNDAY, (EditText) findViewById(R.id.sunday_end_time_minutes_edit_text));
            put(OpeningHours.Day.MONDAY, (EditText) findViewById(R.id.monday_end_time_minutes_edit_text));
            put(OpeningHours.Day.TUESDAY, (EditText) findViewById(R.id.tuesday_end_time_minutes_edit_text));
            put(OpeningHours.Day.WEDNESDAY, (EditText) findViewById(R.id.wednesday_end_time_minutes_edit_text));
            put(OpeningHours.Day.THURSDAY, (EditText) findViewById(R.id.thursday_end_time_minutes_edit_text));
            put(OpeningHours.Day.FRIDAY, (EditText) findViewById(R.id.friday_end_time_minutes_edit_text));
            put(OpeningHours.Day.SATURDAY, (EditText) findViewById(R.id.saturday_end_time_minutes_edit_text));
        }};
    }

    private void setActivityFromOpeningHours(OpeningHours openingHours) {
        Map<OpeningHours.Day, CheckBox> dayCheckBoxMap = getDayCheckBoxMap();
        Map<OpeningHours.Day, EditText> dayStartTimeHoursMap = getDayStartTimeHoursMap();
        Map<OpeningHours.Day, EditText> dayStartTimeMinutesMap = getDayStartTimeMinutesMap();
        Map<OpeningHours.Day, EditText> dayEndTimeHoursMap = getDayEndTimeHoursMap();
        Map<OpeningHours.Day, EditText> dayEndTimeMinutesMap = getDayEndTimeMinutesMap();

        if (openingHours != null) {
            for (final OpeningHours.Day day : OpeningHours.Day.values()) {
                final CheckBox checkBox = dayCheckBoxMap.get(day);
                final EditText startHoursEditText = dayStartTimeHoursMap.get(day);
                final EditText startMinutesEditText = dayStartTimeMinutesMap.get(day);
                final EditText endHoursEditText = dayEndTimeHoursMap.get(day);
                final EditText endMinutesEditText = dayEndTimeMinutesMap.get(day);

                Pair<TimeOfDay, TimeOfDay> time = openingHours.getOpeningHours(day);

                checkBox.setChecked(time != null);
                startHoursEditText.setEnabled(time != null);
                startMinutesEditText.setEnabled(time != null);
                endHoursEditText.setEnabled(time != null);
                endMinutesEditText.setEnabled(time != null);

                if (time != null && time.first != null && time.second != null) {
                    startHoursEditText.setText(String.format(Locale.US, "%02d", time.first.hour));
                    startMinutesEditText.setText(String.format(Locale.US, "%02d", time.first.minute));
                    endHoursEditText.setText(String.format(Locale.US, "%02d", time.second.hour));
                    endMinutesEditText.setText(String.format(Locale.US, "%02d", time.second.minute));
                } else {
                    startHoursEditText.setText(R.string.zero_formatted_2d);
                    startMinutesEditText.setText(R.string.zero_formatted_2d);
                    endHoursEditText.setText(R.string.zero_formatted_2d);
                    endMinutesEditText.setText(R.string.zero_formatted_2d);
                }

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        startHoursEditText.setEnabled(b);
                        startMinutesEditText.setEnabled(b);
                        endHoursEditText.setEnabled(b);
                        endMinutesEditText.setEnabled(b);
                    }
                });
            }
        }
    }

    private OpeningHours openingHoursFromActivity() {
        OpeningHours openingHours = new OpeningHours();

        Map<OpeningHours.Day, CheckBox> dayCheckBoxMap = getDayCheckBoxMap();
        Map<OpeningHours.Day, EditText> dayStartTimeHoursMap = getDayStartTimeHoursMap();
        Map<OpeningHours.Day, EditText> dayStartTimeMinutesMap = getDayStartTimeMinutesMap();
        Map<OpeningHours.Day, EditText> dayEndTimeHoursMap = getDayEndTimeHoursMap();
        Map<OpeningHours.Day, EditText> dayEndTimeMinutesMap = getDayEndTimeMinutesMap();

        for (OpeningHours.Day day : OpeningHours.Day.values()) {
            CheckBox checkBox = dayCheckBoxMap.get(day);
            EditText startTimeHourEditText = dayStartTimeHoursMap.get(day);
            EditText startTimeMinuteEditText = dayStartTimeMinutesMap.get(day);
            EditText endTimeHourEditText = dayEndTimeHoursMap.get(day);
            EditText endTimeMinuteEditText = dayEndTimeMinutesMap.get(day);

            if (checkBox.isChecked()) {
                TimeOfDay start = new TimeOfDay(Integer.parseInt(startTimeHourEditText.getText().toString()),
                        Integer.parseInt(startTimeMinuteEditText.getText().toString()));
                TimeOfDay end = new TimeOfDay(Integer.parseInt(endTimeHourEditText.getText().toString()),
                        Integer.parseInt(endTimeMinuteEditText.getText().toString()));

                openingHours.setOpeningHours(day, start, end);
            }
        }

        return openingHours;
    }
}
