package com.askylol.bookaseat.logic;

import com.askylol.bookaseat.utils.TimeOfDay;

/**
 * Created by Sharon on 22-May-17.
 */
public class Reservation {
    private TimeOfDay start;
    private TimeOfDay end;
    private String user;

    private Reservation() {

    }

    public Reservation(TimeOfDay start, TimeOfDay end, String user) {
        this.start = start;
        this.end = end;
        this.user = user;
    }

    public TimeOfDay getStart() {
        return start;
    }

    public void setStart(TimeOfDay start) {
        this.start = start;
    }

    public TimeOfDay getEnd() {
        return end;
    }

    public void setEnd(TimeOfDay end) {
        this.end = end;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "start=" + start +
                ", end=" + end +
                ", user='" + user + '\'' +
                '}';
    }
}
