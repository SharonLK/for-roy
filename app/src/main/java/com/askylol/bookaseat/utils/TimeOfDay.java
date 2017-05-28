package com.askylol.bookaseat.utils;

/**
 * Created by Sharon on 28-May-17.
 */

public class TimeOfDay {
    public final int hour;
    public final int minute;

    public TimeOfDay() {
        hour = 0;
        minute = 0;
    }

    public TimeOfDay(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public String toString() {
        return "TimeOfDay{" +
                "hour=" + hour +
                ", minute=" + minute +
                '}';
    }
}
