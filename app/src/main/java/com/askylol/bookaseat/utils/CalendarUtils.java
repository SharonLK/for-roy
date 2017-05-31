package com.askylol.bookaseat.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class CalendarUtils {
    static private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    static private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

    /**
     *
     * @param calendar holds the date to be converted
     * @return date in format dd.MM.yyyy
     */
    static public String getDateString(Calendar calendar) {
        return dateFormatter.format(calendar.getTimeInMillis());
    }

    /**
     *
     * @param calendar holds the time to be converted
     * @return time in format HH:mm (24 hours)
     */
    static public String getTimeString(Calendar calendar) {
        return timeFormatter.format(calendar.getTimeInMillis());
    }

    /**
     *
     * @param timeOfDay holds the time to be converted
     * @return time in format HH:mm (24 hours)
     */
    static public String getTimeString(TimeOfDay timeOfDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timeOfDay.hour);
        calendar.set(Calendar.MINUTE, timeOfDay.minute);
        return getTimeString(calendar);
    }

    static public TimeOfDay getTimeOfDay(Calendar calendar) {
        return new TimeOfDay(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }
}
