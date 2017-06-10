package com.askylol.bookaseat.utils;

/**
 * Created by Sharon on 10-Jun-17.
 */

public class Date {
    public int day;
    public int month;
    public int year;

    public Date(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    /**
     * Constructs a new date given a string representing the date, i.e.:
     * <p>
     * 6.6.17
     * 25/4/16
     * 3_4_15
     * <p>
     * Please note that days must appear before months, and months before years.
     *
     * @param date      string representing the date
     * @param separator string that separates the different elements of the date
     */
    public Date(String date, String separator) {
        String[] elements = date.split(separator);

        day = Integer.parseInt(elements[0]);
        month = Integer.parseInt(elements[1]);
        year = Integer.parseInt(elements[2]);
    }

    public boolean before(Date other) {
        if (equals(other)) {
            return false;
        }

        if (year < other.year) {
            return true;
        }

        if (year == other.year && month < other.month) {
            return true;
        }

        return year == other.year && month == other.month && day < other.day;
    }

    public boolean after(Date other) {
        return !equals(other) && !before(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Date date = (Date) o;

        if (day != date.day) return false;
        if (month != date.month) return false;
        return year == date.year;

    }

    @Override
    public int hashCode() {
        int result = day;
        result = 31 * result + month;
        result = 31 * result + year;
        return result;
    }

    @Override
    public String toString() {
        return "Date{" +
                "day=" + day +
                ", month=" + month +
                ", year=" + year +
                '}';
    }
}
