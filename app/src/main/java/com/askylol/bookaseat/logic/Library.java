package com.askylol.bookaseat.logic;

import com.askylol.bookaseat.utils.CalendarUtils;
import com.askylol.bookaseat.utils.OpeningHours;
import com.askylol.bookaseat.utils.Pair;
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
    private Map<String, Map<String, Map<String, Reservation>>> reservations = new HashMap<>();
    private DatabaseReference libraryRef;
    private Map<String, String> admins = new HashMap<>();
    private int maxDelay;
    private int idleLimit;

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
     * @return current library opening hours
     */
    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    /**
     * Sets libraries' opening hours to the given parameter. This method also updates the opening
     * hours object in the database.
     *
     * @param openingHours new opening hours
     */
    public void updateOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;

        libraryRef.child("openingHours")
                .setValue(openingHours, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        System.out.println("Opening Hours updates successfully");
                    }
                });
    }

    /**
     * @return idle limit
     */
    public int getIdleLimit() {
        return idleLimit;
    }

    /**
     * Updates the current idle limit to the given value and notifies the database.
     *
     * @param idleLimit new idle limit
     */
    public void updateIdleLimit(int idleLimit) {
        this.idleLimit = idleLimit;

        libraryRef.child("idleLimit")
                .setValue(idleLimit, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        System.out.println("Idle limit updated successfully");
                    }
                });
    }

    /**
     * @return for how long the user can be late for the reservation
     */
    public int getMaxDelay() {
        return maxDelay;
    }

    /**
     * Updates the current max delay to the given value and notifies the database.
     *
     * @param maxDelay new max delay
     */
    public void updateMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;

        libraryRef.child("maxDelay")
                .setValue(maxDelay, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        System.out.println("Max delay updated successfully");
                    }
                });
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

    public Map<String, Map<String, Map<String, Reservation>>> getReservations() {
        return reservations;
    }

    public Map<String, Seat> getIdToSeat() {
        return idToSeat;
    }

    /**
     * Checks whether the requested seat is free at the selected time
     *
     * @param seatId           seat to check
     * @param selectedDateTime date and time of reservation
     * @return <code>true</code> if seat is free, <code>false</code> otherwise
     */
    public boolean isSeatFree(String seatId, Calendar selectedDateTime) {
        if (!reservations.containsKey(seatId)) {
            return true;
        }

        String date = CalendarUtils.getDateString(selectedDateTime).replace('.', '_');

        if (!reservations.get(seatId).containsKey(date)) {
            return true;
        }

        Calendar now = Calendar.getInstance();
        String dateNow = CalendarUtils.getDateString(now).replace('.', '_');
        TimeOfDay timeNow = CalendarUtils.getTimeOfDay(now);

        TimeOfDay time = CalendarUtils.getTimeOfDay(selectedDateTime);

        for (Reservation reservation : reservations.get(seatId).get(date).values()) {
            // If seat not occupied and idle time has passed, the reservation is invalid
            if (reservation.getLastSeen() == null &&
                    dateNow.equals(date) &&
                    !reservation.isOccupied() &&
                    timeNow.isAfter(reservation.getStart().add(0, maxDelay))) {
                continue;
            }

            // If user has been outside the library for too long, the reservation is invalid
            if (reservation.getLastSeen() != null &&
                    dateNow.equals(date) &&
                    !reservation.isOccupied() &&
                    timeNow.isAfter(reservation.getLastSeen().add(0, idleLimit))) {
                continue;
            }

            if (reservation.getStart().isBeforeOrSame(time) && reservation.getEnd().isAfter(time)) {
                return false;
            }
        }

        return true;
    }

    public boolean isSeatFree(String seatId, Calendar startDateTime, Calendar endDateTime) {
        TimeOfDay start = CalendarUtils.getTimeOfDay(startDateTime);
        TimeOfDay end = CalendarUtils.getTimeOfDay(endDateTime);

        String date = CalendarUtils.getDateString(startDateTime).replaceAll("\\.", "_");

        if (!reservations.containsKey(seatId)) {
            return true;
        }

        Calendar now = Calendar.getInstance();
        String dateNow = CalendarUtils.getDateString(now).replace('.', '_');
        TimeOfDay timeNow = CalendarUtils.getTimeOfDay(now);

        for (Map.Entry<String, Map<String, Reservation>> seatReservations : reservations.get(seatId).entrySet()) {
            String resDate = seatReservations.getKey();

            for (Reservation reservation : seatReservations.getValue().values()) {
                if (!date.equals(resDate)) {
                    continue;
                }

                // If seat not occupied and idle time has passed, the reservation is invalid
                if (reservation.getLastSeen() == null &&
                        dateNow.equals(date) &&
                        !reservation.isOccupied() &&
                        timeNow.isAfter(reservation.getStart().add(0, maxDelay))) {
                    continue;
                }

                // If user has been outside the library for too long, the reservation is invalid
                if (reservation.getLastSeen() != null &&
                        dateNow.equals(date) &&
                        !reservation.isOccupied() &&
                        timeNow.isAfter(reservation.getLastSeen().add(0, idleLimit))) {
                    continue;
                }

                TimeOfDay resStart = reservation.getStart();
                TimeOfDay resEnd = reservation.getEnd();

                if (start.isBefore(resEnd) && resStart.isBefore(end)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns a reservations made by the given user for the requested seat at the given time and
     * date.
     *
     * @param seatId           seat id
     * @param selectedDateTime time and date of reservation
     * @param username         user who reserved the seat
     * @return reservation if it was found, <code>null</code> otherwise
     */
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
            Calendar now = Calendar.getInstance();
            String dateNow = CalendarUtils.getDateString(now).replace('.', '_');
            TimeOfDay timeNow = CalendarUtils.getTimeOfDay(now);

            // If seat not occupied and idle time has passed, the reservation is invalid
            if (reservation.getLastSeen() == null &&
                    dateNow.equals(date) &&
                    !reservation.isOccupied() &&
                    timeNow.isAfter(reservation.getStart().add(0, maxDelay))) {
                continue;
            }

            // If user has been outside the library for too long, the reservation is invalid
            if (reservation.getLastSeen() != null &&
                    dateNow.equals(date) &&
                    !reservation.isOccupied() &&
                    timeNow.isAfter(reservation.getLastSeen().add(0, idleLimit))) {
                continue;
            }

            if (reservation.getStart().isBeforeOrSame(time) && reservation.getEnd().isAfter(time) &&
                    reservation.getUser().toLowerCase().equals(username.toLowerCase())) {
                return reservation;
            }
        }

        return null;
    }

    /**
     * Get a list of all reservations made by the given user. Each reservation is paired with the
     * date it was made for.
     *
     * @param username username of the user
     * @return list of dates and reservations the user made
     */
    public List<Pair<String, Reservation>> reservationsByUser(String username) {
        List<Pair<String, Reservation>> r = new ArrayList<>();

        for (Map<String, Map<String, Reservation>> stringMapMap : reservations.values()) {
            for (Map.Entry<String, Map<String, Reservation>> stringMapEntry : stringMapMap.entrySet()) {
                String date = stringMapEntry.getKey();

                for (Reservation reservation : stringMapEntry.getValue().values()) {
                    if (reservation.getUser().equals(username)) {
                        r.add(new Pair<>(date, reservation));
                    }
                }
            }
        }

        return r;
    }

    /**
     * Checks whether there is a seat reserved by the user at the given date and time.
     * If there is, returns a pair of seatId and Reservation. Otherwise null.
     *
     * @param selectedDateTime date and time of reservation
     * @param username         username of the user
     * @return Pair of seatId and Reservation if user has a reservation, <code>null</code> otherwise
     */
    public Pair<String, Reservation> reservationByUser(Calendar selectedDateTime, String username) {
        for (String seatId : reservations.keySet()) {
            Reservation reservation = reservationByUser(seatId, selectedDateTime, username);
            if (reservation != null) {
                return new Pair<>(seatId, reservation);
            }
        }

        return null;
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

    /**
     * @param id id of the seat
     * @return seat object represented by the given id
     */
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

    /**
     * Checks whether or not the given user is an admin of this library.
     *
     * @param username username to check
     * @return <code>true</code> if the user is an admin, <code>false</code> otherwise
     */
    public boolean isAdmin(String username) {
        for (String s : admins.values()) {
            if (s.equals(username)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add a new admin to the list of admin of the library and update the database accordingly.
     *
     * @param mail mail of the admin to add
     */
    public void addAdmin(final String mail) {
        if (libraryRef == null) {
            throw new IllegalStateException("No reference to library on db");
        }

        libraryRef.getDatabase()
                .getReference(String.format("libraries/%s/admins", libraryRef.getKey()))
                .push()
                .setValue(mail);
    }

    /**
     * Remove an admin from the library, and update the database accordingly.
     *
     * @param mail mail of the admin to remove
     */
    public void removeAdmin(final String mail) {
        if (libraryRef == null) {
            throw new IllegalStateException("No reference to library on db");
        }

        libraryRef.getDatabase()
                .getReference(String.format("libraries/%s/admins", libraryRef.getKey()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, String> adminsMap = (Map<String, String>) dataSnapshot.getValue();

                        for (Map.Entry<String, String> entry : adminsMap.entrySet()) {
                            if (entry.getValue().equals(mail)) {
                                dataSnapshot.getRef().child(entry.getKey()).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void updateReservation(String seatId, final Reservation reservation) {
        getReservationsReferenceAt(seatId, CalendarUtils.getDateString(Calendar.getInstance()).replace('.', '_')).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Reservation> reservationsMap = (Map<String, Reservation>) dataSnapshot.getValue();
                for (Map.Entry<String, Reservation> entry : reservationsMap.entrySet()) {
                    if (dataSnapshot.child(entry.getKey()).getValue(Reservation.class).equals(reservation))
                        dataSnapshot.getRef().child(entry.getKey()).setValue(reservation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
