package com.askylol.bookaseat.logic;

import com.askylol.bookaseat.utils.Point;

/**
 * Created by Sharon on 22-May-17.
 */
public class Seat {
    private Status status = Status.FREE;
    private User user;
    private Point location;

    private Seat() {
        // Need for Firebase
    }

    public Seat(Point location) {
        this.location = location;
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
     * @return this seat's location in
     */
    public Point getLocation() {
        return location;
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
