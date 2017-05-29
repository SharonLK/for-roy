package com.askylol.bookaseat.logic;

import com.askylol.bookaseat.utils.OpeningHours;
import com.askylol.bookaseat.utils.Point;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sharon on 22-May-17.
 */
public class Library {
    private Map<String, Seat> idToSeat = new HashMap<>();
    private OpeningHours openingHours = new OpeningHours();
    private Map<String, User> users = new HashMap<>();
    private Map<String, Map<String, List<Reservation>>> reservations = new HashMap<>();
    private DatabaseReference libraryRef;

    private Library() {

    }

    /**
     * Reserves the wanted seat by the given user.
     * @param seatId seat to reserve
     * @param user   user that reserves the seat
     */
    public void reserve(String seatId, User user) {
        if (libraryRef == null) {
            //TODO: handle, nah
            throw new IllegalStateException("No reference to library on db");
        }

        Seat seat = getSeatById(seatId);
        if (seat == null) {
            return;
        }

        seat.setStatus(Seat.Status.RESERVED);
        seat.setUser(user);
        libraryRef
                .child("idToSeat")
                .child(String.valueOf(seatId))
                .setValue(seat, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        //TODO: acknowledge success, nah
                    }
                });
    }

    /**
     * Frees the wanted seat.
     *
     * @param seatId seat to be freed
     */
    public void free(String seatId) {
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
                .child("idToSeat")
                .child(String.valueOf(seatId))
                .setValue(seat, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        //TODO: acknowledge success, nah
                    }
                });
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    /**
     * @param seatId seat to be returned
     * @return a seat
     */
    public Seat getSeat(String seatId) {
        return getSeatById(seatId);
    }

    @Exclude
    public List<Seat> getSeats() {
        return new ArrayList<>(idToSeat.values());
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Map<String, List<Reservation>>> getReservations() {
        return reservations;
    }

    public Map<String, Seat> getIdToSeat() {
        return idToSeat;
    }

    private Seat getSeatById(String id) {
        return idToSeat.get(id);
    }

    public void setLibraryRef(DatabaseReference libraryRef) {
        this.libraryRef = libraryRef;
    }
}
