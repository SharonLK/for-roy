package com.askylol.bookaseat.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Amit on 30/05/2017.
 */

public class CalendarUtils {
    static private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    static private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

    static public String getDateString(Calendar calendar) {
        return dateFormatter.format(calendar.getTimeInMillis());
    }

    static public String getTimeString(Calendar calendar) {
        return timeFormatter.format(calendar.getTimeInMillis());
    }

    static public String getTimeString(TimeOfDay timeOfDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, timeOfDay.hour);
        calendar.set(Calendar.MINUTE, timeOfDay.minute);
        return getTimeString(calendar);
    }

    static public TimeOfDay getTimeOfDay(Calendar calendar) {
        return new TimeOfDay(calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE));
    }
}
