package com.askylol.bookaseat.utils;

/**
 * Created by Sharon on 28-May-17.
 */

public class TimeOfDay {
    public int hour;
    public int minute;

    public TimeOfDay() {
        hour = 0;
        minute = 0;
    }

    public TimeOfDay(TimeOfDay other) {
        hour = other.hour;
        minute = other.minute;
    }

    public TimeOfDay(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public boolean isBefore(TimeOfDay other) {
        return hour < other.hour || (hour == other.hour && minute < other.minute);
    }

    public boolean isBeforeOrSame(TimeOfDay other) {
        return isBefore(other) || equals(other);
    }

    public boolean isAfter(TimeOfDay other) {
        return !isBefore(other) && !this.equals(other);
    }

    public boolean isAfterOrSame(TimeOfDay other) {
        return isAfter(other) || equals(other);
    }

    public TimeOfDay add(int additionHours, int additionMinutes) {
        return new TimeOfDay(hour + additionHours, minute + additionMinutes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeOfDay timeOfDay = (TimeOfDay) o;

        if (hour != timeOfDay.hour) return false;
        return minute == timeOfDay.minute;

    }

    @Override
    public int hashCode() {
        int result = hour;
        result = 31 * result + minute;
        return result;
    }

    @Override
    public String toString() {
        return "TimeOfDay{" +
                "hour=" + hour +
                ", minute=" + minute +
                '}';
    }
}
