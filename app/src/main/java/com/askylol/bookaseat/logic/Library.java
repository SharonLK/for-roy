package com.askylol.bookaseat.logic;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sharon on 22-May-17.
 */
public class Library {
    private List<Seat> seats = new ArrayList<>();
    private DatabaseReference libraryRef;

    public Library() {
    }

    /**
     * Reserves the wanted seat by the given user.
     *
     * @param seatId seat to reserve
     * @param user   user that reserves the seat
     */
    public void reserve(int seatId, User user) {
        if (libraryRef == null) {
            //TODO: handle
            throw new IllegalStateException("No reference to library on db");
        }
        Seat seat = getSeatById(seatId);

        if (seat == null) {
            return;
        }

        seat.setStatus(Seat.Status.RESERVED);
        seat.setUser(user);
        libraryRef
                .child("seats")
                .child(String.valueOf(seatId))
                .setValue(seat, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        //TODO: acknowledge success
                    }
                });
    }

    /**
     * Frees the wanted seat.
     *
     * @param seatId seat to be freed
     */
    public void free(int seatId) {
        if (libraryRef == null) {
            //TODO: handle
            throw new IllegalStateException("No reference to library on db");
        }
        Seat seat = getSeatById(seatId);

        if (seat == null) {
            return;
        }

        seat.setStatus(Seat.Status.FREE);
        seat.setUser(null);
        libraryRef
                .child("seats")
                .child(String.valueOf(seatId))
                .setValue(seat, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        //TODO: acknowledge success
                    }
                });
    }

    /**
     * @param seatId seat to be returned
     * @return a seat
     */
    public Seat getSeat(int seatId) {
        return getSeatById(seatId);
    }

    public List<Seat> getSeats() {
        return seats;
    }

    private Seat getSeatById(int id) {
        for (Seat seat : seats) {
            if (seat.id == id) {
                return seat;
            }
        }

        return null;
    }

    public void setLibraryRef(DatabaseReference libraryRef) {
        this.libraryRef = libraryRef;
    }
}
