package com.askylol.bookaseat.logic;

import com.askylol.bookaseat.utils.CalendarUtils;
import com.askylol.bookaseat.utils.Data;
import com.askylol.bookaseat.utils.OpeningHours;
import com.askylol.bookaseat.utils.TimeOfDay;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Library {
    private Map<String, Seat> idToSeat = new HashMap<>();
    private OpeningHours openingHours = new OpeningHours();
    private Map<String, User> users = new HashMap<>();
    private Map<String, Map<String, Map<String, Reservation>>> reservations = new HashMap<>();
    private DatabaseReference libraryRef;

    private Library() {

    }

    /**
     * Reserves the wanted seat by the given user.
     *
     * @param seatId seat to reserve
     * @param user   user that reserves the seat
     * @param date   date to reserve
     * @param start  reservation start time
     * @param end    reservation end time
     */
    public void reserve(String seatId, User user, String date, TimeOfDay start, TimeOfDay end) {
        if (libraryRef == null) {
            //TODO: handle, nah
            throw new IllegalStateException("No reference to library on db");
        }

        Seat seat = getSeatById(seatId);
        if (seat == null) {
            return;
        }

        getReservationsReferenceAt(seatId, date).push().setValue(new Reservation(start, end, user.getName()));
    }

    /**
     * Get the reference to a reservation's path.
     * Works even if path doesn't exist.
     *
     * @param seatId id of the seat
     * @param date   date string in the format dd/MM/yyyy
     * @return reference to reservation's path
     */
    private DatabaseReference getReservationsReferenceAt(String seatId, String date) {
        return libraryRef.getDatabase().getReference(
                String.format(
                        "libraries/%s/reservations/%s/%s",
                        libraryRef.getKey(), seatId, date.replace('.', '_')
                ));
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

    public Map<String, Map<String, Map<String, Reservation>>> getReservations() {
        return reservations;
    }

    public Map<String, Seat> getIdToSeat() {
        return idToSeat;
    }

    public boolean isSeatFree(String seatId, Calendar selectedDateTime) {
        if (!reservations.containsKey(seatId)) {
            return true;
        }

        String date = CalendarUtils.getDateString(selectedDateTime).replace('.', '_');

        if (!reservations.get(seatId).containsKey(date)) {
            return true;
        }

        TimeOfDay time = CalendarUtils.getTimeOfDay(selectedDateTime);

        for (Reservation reservation : reservations.get(seatId).get(date).values()) {
            if (reservation.getStart().isBeforeOrSame(time) && reservation.getEnd().isAfter(time)) {
                return false;
            }
        }

        return true;
    }

    public boolean isSeatFree(Calendar startDateTime, Calendar endDateTime) {
        TimeOfDay start = CalendarUtils.getTimeOfDay(startDateTime);
        TimeOfDay end = CalendarUtils.getTimeOfDay(endDateTime);

        for (Map<String, Map<String, Reservation>> seatReservations : reservations.values()) {
            for (Map<String, Reservation> dateReservations : seatReservations.values()) {
                for (Reservation reservation : dateReservations.values()) {
                    TimeOfDay resStart = reservation.getStart();
                    TimeOfDay resEnd = reservation.getEnd();

                    if (start.isBefore(resStart) && resEnd.isBefore(end)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public Reservation reservationByUser(String seatId, Calendar selectedDateTime, String username) {
        if (!reservations.containsKey(seatId)) {
            return null;
        }

        String date = CalendarUtils.getDateString(selectedDateTime).replace('.', '_');

        if (!reservations.get(seatId).containsKey(date)) {
            return null;
        }

        TimeOfDay time = CalendarUtils.getTimeOfDay(selectedDateTime);

        for (Reservation reservation : reservations.get(seatId).get(date).values()) {
            if (reservation.getStart().isBeforeOrSame(time) && reservation.getEnd().isAfter(time) &&
                    reservation.getUser().toLowerCase().equals(username.toLowerCase())) {
                return reservation;
            }
        }

        return null;
    }

    public List<Reservation> reservationsByUser(String username) {
        List<Reservation> r = new ArrayList<>();

        for (Map<String, Map<String, Reservation>> stringMapMap : reservations.values()) {
            for (Map<String, Reservation> stringReservationMap : stringMapMap.values()) {
                for (Reservation reservation : stringReservationMap.values()) {
                    if (reservation.getUser().equals(username)) {
                        r.add(reservation);
                    }
                }
            }
        }

        return r;
    }

    public boolean reservationByUser(Calendar selectedDateTime, String username) {
        for (String seatId : reservations.keySet()) {
            String date = CalendarUtils.getDateString(selectedDateTime).replace('.', '_');

            if (!reservations.get(seatId).containsKey(date)) {
                return false;
            }

            TimeOfDay time = CalendarUtils.getTimeOfDay(selectedDateTime);

            for (Reservation reservation : reservations.get(seatId).get(date).values()) {
                System.out.println(reservation.getUser());
                if (reservation.getStart().isBeforeOrSame(time) && reservation.getEnd().isAfter(time) &&
                        reservation.getUser().toLowerCase().equals(username.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    public Reservation getNearestReservation(String seatId, String date, TimeOfDay time) {
        if (!reservations.containsKey(seatId)) {
            return null;
        }

        if (!reservations.get(seatId).containsKey(date)) {
            return null;
        }

        Reservation nearest = null;

        for (Reservation reservation : reservations.get(seatId).get(date).values()) {
            if (reservation.getStart().isAfter(time) &&
                    (nearest == null || reservation.getStart().isBefore(nearest.getStart()))) {
                nearest = reservation;
            }
        }

        return nearest;
    }

    private Seat getSeatById(String id) {
        return idToSeat.get(id);
    }

    public void setLibraryRef(DatabaseReference libraryRef) {
        this.libraryRef = libraryRef;
    }

    public void removeReservation(String seatId, String date, final Reservation reservation) {
        getReservationsReferenceAt(seatId, date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Reservation> reservationsMap = (Map<String, Reservation>) dataSnapshot.getValue();
                for (Map.Entry<String, Reservation> entry : reservationsMap.entrySet()) {
                    if (dataSnapshot.child(entry.getKey()).getValue(Reservation.class).equals(reservation))
                        dataSnapshot.getRef().child(entry.getKey()).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
