package com.askylol.bookaseat.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sharon on 22-May-17.
 */
public class Library {
    private List<Seat> seats = new ArrayList<>();

    public Library() {
        seats.add(new Seat(0));
        seats.add(new Seat(1));
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

    private Seat getSeatById(int id) {
        for (Seat seat : seats) {
            if (seat.id == id) {
                return seat;
            }
        }

        return null;
    }
}
