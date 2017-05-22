package com.askylol.bookaseat.logic;

import com.askylol.bookaseat.utils.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Sharon on 22-May-17.
 */
public class Library {
    private List<Seat> seats = new ArrayList<>();
    private Map<Integer, Point> locationById = new HashMap<>();

    public static final Random r = new Random();

    public Library() {
        for (int i = 0; i < 1000; i++) {
            seats.add(new Seat(i));
            locationById.put(i, new Point(r.nextInt(3000), r.nextInt(2000)));
        }
    }

    /**
     * Reserves the wanted seat by the given user.
     *
     * @param seatId seat to reserve
     * @param user   user that reserves the seat
     */
    public void reserve(int seatId, User user) {
        Seat seat = getSeatById(seatId);

        if (seat == null) {
            return;
        }

        seat.setStatus(Seat.Status.RESERVED);
        seat.setUser(user);
    }

    /**
     * Frees the wanted seat.
     *
     * @param seatId seat to be freed
     */
    public void free(int seatId) {
        Seat seat = getSeatById(seatId);

        if (seat == null) {
            return;
        }

        seat.setStatus(Seat.Status.FREE);
        seat.setUser(null);
    }

    /**
     * @param seatId seat to be returned
     * @return a seat
     */
    public Seat getSeat(int seatId) {
        return getSeatById(seatId);
    }

    public Point getSeatLocation(int seatId) {
        return locationById.get(seatId);
    }

    public Collection<Point> getSeatLocations() {
        return locationById.values();
    }

    private Seat getSeatById(int id) {
        for (Seat seat : seats) {
            if (seat.id == id) {
                return seat;
            }
        }

        return null;
    }
}