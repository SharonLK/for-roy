package com.askylol.bookaseat.utils;

/**
 * Created by Sharon on 10-Jun-17.
 */
public class DateAndTime {
    public Date date;
    public TimeOfDay time;

    public DateAndTime(Date date, TimeOfDay time) {
        this.date = date;
        this.time = time;
    }

    public boolean before(DateAndTime other) {
        if (date.before(other.date)) {
            return true;
        }

        return date.equals(other.date) && time.isBefore(other.time);
    }

    public boolean after(DateAndTime other) {
        return !equals(other) && !before(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateAndTime that = (DateAndTime) o;

        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        return time != null ? time.equals(that.time) : that.time == null;

    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DateAndTime{" +
                "date=" + date +
                ", time=" + time +
                '}';
    }
}
