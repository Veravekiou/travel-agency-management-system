package com.verav.travelagency.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.verav.travelagency.common.ValidationUtils;
import com.verav.travelagency.model.Trip;
import com.verav.travelagency.services.DBService;

import java.sql.*;
import java.time.LocalDate;

public class TripsController extends BaseController {

    // UI elements for the trips table and columns
    @FXML private TableView<Trip> tripsTable;
    @FXML private TableColumn<Trip, Integer> tripIdColumn;
    @FXML private TableColumn<Trip, String> destinationColumn;
    @FXML private TableColumn<Trip, LocalDate> departureColumn;
    @FXML private TableColumn<Trip, LocalDate> returnColumn;
    @FXML private TableColumn<Trip, Double> priceColumn;
    @FXML private TableColumn<Trip, Integer> seatsColumn;

    // Buttons for adding, editing, and deleting trips
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    // List to hold all trips data
    private final ObservableList<Trip> trips = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up cell value factories for each column
        tripIdColumn.setCellValueFactory(c -> c.getValue().tripIdProperty().asObject());
        destinationColumn.setCellValueFactory(c -> c.getValue().destinationProperty());
        departureColumn.setCellValueFactory(c -> c.getValue().departureDateProperty());
        returnColumn.setCellValueFactory(c -> c.getValue().returnDateProperty());
        priceColumn.setCellValueFactory(c -> c.getValue().priceProperty().asObject());
        seatsColumn.setCellValueFactory(c -> c.getValue().availableSeatsProperty().asObject());

        // Load trips from the database and set them to the table
        loadTripsFromDB();
        tripsTable.setItems(trips);

        // Set button actions
        addButton.setOnAction(e -> handleAdd());
        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());
    }

    // Load all trips from the database
    private void loadTripsFromDB() {
        trips.clear();
        try {
            ResultSet rs = DBService.executeQuery("SELECT * FROM trips");
            while (rs.next()) {
                trips.add(new Trip(
                        rs.getInt("trip_id"),
                        rs.getString("destination"),
                        rs.getDate("departure_date").toLocalDate(),
                        rs.getDate("return_date").toLocalDate(),
                        rs.getDouble("price"),
                        rs.getInt("available_seats")
                ));
            }
        } catch (SQLException e) {
            showError("Could not load trips from the database.", e);
        }
    }

    // Handle adding a new trip
    private void handleAdd() {
        Trip newTrip = showTripDialog(null);
        if (newTrip == null) return;

        try {
            String sql = "INSERT INTO trips (destination, departure_date, return_date, price, available_seats) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
            stmt.setString(1, newTrip.getDestination());
            stmt.setDate(2, Date.valueOf(newTrip.getDepartureDate()));
            stmt.setDate(3, Date.valueOf(newTrip.getReturnDate()));
            stmt.setDouble(4, newTrip.getPrice());
            stmt.setInt(5, newTrip.getAvailableSeats());
            stmt.executeUpdate();
            loadTripsFromDB(); // Refresh the table after adding
        } catch (SQLException e) {
            showError("Could not add the trip.", e);
        }
    }

    // Handle editing an existing trip
    private void handleEdit() {
        Trip selected = tripsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a trip to edit.");
            return;
        }

        Trip updated = showTripDialog(selected);
        if (updated == null) return;

        try {
            String sql = "UPDATE trips SET destination=?, departure_date=?, return_date=?, price=?, available_seats=? WHERE trip_id=?";
            PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
            stmt.setString(1, updated.getDestination());
            stmt.setDate(2, Date.valueOf(updated.getDepartureDate()));
            stmt.setDate(3, Date.valueOf(updated.getReturnDate()));
            stmt.setDouble(4, updated.getPrice());
            stmt.setInt(5, updated.getAvailableSeats());
            stmt.setInt(6, updated.getTripId());
            stmt.executeUpdate();
            loadTripsFromDB(); // Refresh the table after editing
        } catch (SQLException e) {
            showError("Could not update the trip.", e);
        }
    }

    // Handle deleting a trip
    private void handleDelete() {
        Trip selected = tripsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a trip to delete.");
            return;
        }

        try {
            // checking foor bookings
            String checkSQL = "SELECT COUNT(*) FROM bookings WHERE trip_id = ?";
            PreparedStatement checkStmt = DBService.getConnection().prepareStatement(checkSQL);
            checkStmt.setInt(1, selected.getTripId());
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int bookingCount = rs.getInt(1);

            if (bookingCount > 0) {
                // Ask confirm from the user
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Confirm Trip Deletion");
                confirmDialog.setHeaderText("This trip has " + bookingCount + " associated booking(s).");
                confirmDialog.setContentText("Do you want to delete the trip and all related bookings?");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirmDialog.getButtonTypes().setAll(yesButton, noButton);

                confirmDialog.showAndWait().ifPresent(response -> {
                    if (response == yesButton) {
                        try {
                            // 1. delete booking
                            String deleteBookingsSQL = "DELETE FROM bookings WHERE trip_id = ?";
                            PreparedStatement deleteBookingsStmt = DBService.getConnection().prepareStatement(deleteBookingsSQL);
                            deleteBookingsStmt.setInt(1, selected.getTripId());
                            deleteBookingsStmt.executeUpdate();

                            // 2. delete trip
                            String deleteTripSQL = "DELETE FROM trips WHERE trip_id = ?";
                            PreparedStatement deleteTripStmt = DBService.getConnection().prepareStatement(deleteTripSQL);
                            deleteTripStmt.setInt(1, selected.getTripId());
                            deleteTripStmt.executeUpdate();

                            loadTripsFromDB();
                            showAlert("Trip and related bookings deleted successfully.");
                        } catch (SQLException e) {
                            showError("Error deleting trip and bookings.", e);
                        }
                    }
                });
            } else {

                String deleteTripSQL = "DELETE FROM trips WHERE trip_id = ?";
                PreparedStatement deleteTripStmt = DBService.getConnection().prepareStatement(deleteTripSQL);
                deleteTripStmt.setInt(1, selected.getTripId());
                deleteTripStmt.executeUpdate();
                loadTripsFromDB();
                showAlert("Trip deleted successfully.");
            }

        } catch (SQLException e) {
            showError("An error occurred while checking or deleting the trip.", e);
        }
    }


    // Show a dialog for adding or editing a trip
    private Trip showTripDialog(Trip existingTrip) {
        Dialog<Trip> dialog = new Dialog<>();
        dialog.setTitle(existingTrip == null ? "Add Trip" : "Edit Trip");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField destinationField = new TextField();
        DatePicker departurePicker = new DatePicker();
        DatePicker returnPicker = new DatePicker();
        TextField priceField = new TextField();
        TextField seatsField = new TextField();

        if (existingTrip != null) {
            destinationField.setText(existingTrip.getDestination());
            departurePicker.setValue(existingTrip.getDepartureDate());
            returnPicker.setValue(existingTrip.getReturnDate());
            priceField.setText(String.valueOf(existingTrip.getPrice()));
            seatsField.setText(String.valueOf(existingTrip.getAvailableSeats()));
        }

        grid.addRow(0, new Label("Destination:"), destinationField);
        grid.addRow(1, new Label("Departure Date:"), departurePicker);
        grid.addRow(2, new Label("Return Date:"), returnPicker);
        grid.addRow(3, new Label("Price:"), priceField);
        grid.addRow(4, new Label("Available Seats:"), seatsField);

        dialog.getDialogPane().setContent(grid);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String validationError = ValidationUtils.validateTripInput(
                    destinationField.getText(),
                    departurePicker.getValue(),
                    returnPicker.getValue(),
                    priceField.getText(),
                    seatsField.getText()
            );
            if (validationError != null) {
                showAlert(validationError);
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new Trip(
                        existingTrip == null ? 0 : existingTrip.getTripId(),
                        destinationField.getText().trim(),
                        departurePicker.getValue(),
                        returnPicker.getValue(),
                        Double.parseDouble(priceField.getText().trim()),
                        Integer.parseInt(seatsField.getText().trim())
                );
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    // Show an alert message to the user
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        showAlert(message);
    }
}
