package com.askylol.bookaseat.logic;

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
    private DatabaseReference libraryRef;

    public Library() {
        idToSeat.put("0", new Seat(new Point(200, 200)));
        idToSeat.put("1", new Seat(new Point(500, 323)));
        idToSeat.put("3", new Seat(new Point(800, 32)));
        idToSeat.put("65", new Seat(new Point(400, 800)));
    }

    /**
     * Reserves the wanted seat by the given user.
     *
     * @param seatId seat to reserve
     * @param user   user that reserves the seat
     */
    public void reserve(int seatId, User user) {
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
     * @param seatId seat to be returned
     * @return a seat
     */
    public Seat getSeat(int seatId) {
        return getSeatById(seatId);
    }

    @Exclude
    public List<Seat> getSeats() {
        return new ArrayList<>(idToSeat.values());
    }

    public Map<String, Seat> getIdToSeat() {
        return idToSeat;
    }

    private Seat getSeatById(int id) {
        return idToSeat.get(Integer.toString(id));
    }

    public void setLibraryRef(DatabaseReference libraryRef) {
        this.libraryRef = libraryRef;
    }
}
