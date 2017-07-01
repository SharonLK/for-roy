package com.askylol.bookaseat.logic;

import com.askylol.bookaseat.utils.TimeOfDay;

/**
 * Created by Sharon on 22-May-17.
 */
public class Reservation {
    private TimeOfDay start;
    private TimeOfDay end;
    private String user;
    private boolean occupied = false;
    private TimeOfDay lastSeen;

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

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public TimeOfDay getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(TimeOfDay lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "start=" + start +
                ", end=" + end +
                ", user='" + user + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reservation that = (Reservation) o;

        if (start != null ? !start.equals(that.start) : that.start != null) return false;
        if (end != null ? !end.equals(that.end) : that.end != null) return false;
        return user != null ? user.equals(that.user) : that.user == null;

    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
