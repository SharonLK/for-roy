package com.askylol.bookaseat.logic;

/**
 * Created by Sharon on 22-May-17.
 */
public class Seat {
    public final int id;
    private Status status;
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

    public enum Status {
        OCCUPIED,
        RESERVED,
        FREE
    }
}
