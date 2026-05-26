package com.verav.travelagency.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Trip {
    private final IntegerProperty tripId;
    private final StringProperty destination;
    private final ObjectProperty<LocalDate> departureDate;
    private final ObjectProperty<LocalDate> returnDate;
    private final DoubleProperty price;
    private final IntegerProperty availableSeats;

    public Trip(int tripId, String destination, LocalDate departureDate, LocalDate returnDate, double price, int availableSeats) {
        this.tripId = new SimpleIntegerProperty(tripId);
        this.destination = new SimpleStringProperty(destination);
        this.departureDate = new SimpleObjectProperty<>(departureDate);
        this.returnDate = new SimpleObjectProperty<>(returnDate);
        this.price = new SimpleDoubleProperty(price);
        this.availableSeats = new SimpleIntegerProperty(availableSeats);
    }

    // Getters
    public int getTripId() { return tripId.get(); }
    public String getDestination() { return destination.get(); }
    public LocalDate getDepartureDate() { return departureDate.get(); }
    public LocalDate getReturnDate() { return returnDate.get(); }
    public double getPrice() { return price.get(); }
    public int getAvailableSeats() { return availableSeats.get(); }

    // Properties
    public IntegerProperty tripIdProperty() { return tripId; }
    public StringProperty destinationProperty() { return destination; }
    public ObjectProperty<LocalDate> departureDateProperty() { return departureDate; }
    public ObjectProperty<LocalDate> returnDateProperty() { return returnDate; }
    public DoubleProperty priceProperty() { return price; }
    public IntegerProperty availableSeatsProperty() { return availableSeats; }


}
