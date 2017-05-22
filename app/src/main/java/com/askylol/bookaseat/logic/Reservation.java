package com.askylol.bookaseat.logic;

import java.util.Date;

/**
 * Created by Sharon on 22-May-17.
 */
public class Reservation {
    private int seatId;
    private Date startDate;
    private Date endDate;
    private User user;

    public Reservation(int seatId, Date startDate, Date endDate, User user) {
        this.seatId = seatId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
