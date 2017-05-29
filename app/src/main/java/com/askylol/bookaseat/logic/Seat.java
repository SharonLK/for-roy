package com.askylol.bookaseat.logic;

import com.askylol.bookaseat.utils.Point;

/**
 * Created by Sharon on 22-May-17.
 */
public class Seat {
    private Point location;

    private Seat() {
        // Need for Firebase
    }

    public Seat(Point location) {
        this.location = location;
    }

    /**
     * @return this seat's location in
     */
    public Point getLocation() {
        return location;
    }

    public enum Status {
        OCCUPIED,
        RESERVED,
        FREE
    }
}
