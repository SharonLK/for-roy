package com.askylol.bookaseat.logic;

/**
 * Created by Sharon on 22-May-17.
 */
public class Seat {
    public final int id;
    private Status status = Status.FREE;
    private User user;

    public Seat(int id) {
        this.id = id;
    }

    /**
     * @return status of this seat
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Changes the status of this seat.
     *
     * @param status new status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return user that occupies this seat
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets a new user as the occupying user of this seat.
     *
     * @param user new user that occupies this seat
     */
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Seat seat = (Seat) o;

        return id == seat.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    public enum Status {
        OCCUPIED,
        RESERVED,
        FREE
    }
}
