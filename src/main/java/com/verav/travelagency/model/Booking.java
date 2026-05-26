package com.verav.travelagency.model;

import javafx.beans.property.*;

public class Booking {
    private IntegerProperty bookingId;
    private IntegerProperty customerId;
    private IntegerProperty tripId;
    private StringProperty bookingDate;
    private StringProperty status;

    public Booking(int bookingId, int customerId, int tripId, String bookingDate, String status) {
        this.bookingId = new SimpleIntegerProperty(bookingId);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.tripId = new SimpleIntegerProperty(tripId);
        this.bookingDate = new SimpleStringProperty(bookingDate);
        this.status = new SimpleStringProperty(status);
    }

    // Getters & Properties
    public int getBookingId() { return bookingId.get(); }
    public IntegerProperty bookingIdProperty() { return bookingId; }
    public int getCustomerId() { return customerId.get(); }
    public IntegerProperty customerIdProperty() { return customerId; }
    public int getTripId() { return tripId.get(); }
    public IntegerProperty tripIdProperty() { return tripId; }
    public String getBookingDate() { return bookingDate.get(); }
    public StringProperty bookingDateProperty() { return bookingDate; }
    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }


    public void setBookingDate(String bookingDate) {
        this.bookingDate.set(bookingDate);
    }

}
