package com.verav.travelagency.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import com.verav.travelagency.common.ValidationUtils;
import com.verav.travelagency.model.Booking;
import com.verav.travelagency.services.DBService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BookingsController extends BaseController {

    // UI controls for booking form and table
    @FXML private ComboBox<String> customerCombo;
    @FXML private ComboBox<String> tripCombo;
    @FXML private DatePicker bookingDatePicker;

    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> bookingIdColumn;
    @FXML private TableColumn<Booking, String> customerNameColumn, tripDescColumn, bookingDateColumn, statusColumn;
    @FXML private TableColumn<Booking, Void> actionsColumn;

    // Observable list for bookings
    private ObservableList<Booking> bookings = FXCollections.observableArrayList();

    // Maps for quick lookup of customer and trip names by ID
    private Map<Integer, String> customerNames = new HashMap<>();
    private Map<Integer, String> tripDescriptions = new HashMap<>();

    @FXML
    public void initialize() {
        bookingsTable.setEditable(true);

        // Set up table columns
        bookingIdColumn.setCellValueFactory(cellData -> cellData.getValue().bookingIdProperty().asObject());

        loadCustomersMap();
        loadTripsMap();

        // Populate ComboBoxes with customer and trip names
        customerCombo.setItems(FXCollections.observableArrayList(customerNames.values()));
        tripCombo.setItems(FXCollections.observableArrayList(tripDescriptions.values()));

        // Display customer and trip names in the table (not just IDs)
        customerNameColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(customerNames.getOrDefault(cellData.getValue().getCustomerId(), "Unknown"))
        );
        tripDescColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(tripDescriptions.getOrDefault(cellData.getValue().getTripId(), "Unknown"))
        );

        // Make booking date column editable
        bookingDateColumn.setCellValueFactory(cellData -> cellData.getValue().bookingDateProperty());
        bookingDateColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        bookingDateColumn.setOnEditCommit(event -> {
            Booking booking = event.getRowValue();
            if (!ValidationUtils.isValidIsoDate(event.getNewValue())) {
                showAlert("Please enter a valid booking date in YYYY-MM-DD format.");
                bookingsTable.refresh();
                return;
            }
            booking.setBookingDate(event.getNewValue());
            updateBookingInDB(booking);
        });

        customerCombo.setEditable(true);
        customerCombo.getEditor().setPromptText("Select Customer");
        tripCombo.setEditable(true);
        tripCombo.getEditor().setPromptText("Select Trip");

        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Add action buttons (Confirm, Cancel) to each row
        actionsColumn.setCellFactory(getActionsCellFactory());

        bookingsTable.setItems(bookings);

        loadBookingsFromDB();
    }

    // Load customer names from database into the map
    private void loadCustomersMap() {
        customerNames.clear();
        try {
            ResultSet rs = DBService.executeQuery("SELECT customer_id, first_name, last_name FROM customers");
            while (rs.next()) {
                int id = rs.getInt("customer_id");
                String name = rs.getString("first_name") + " " + rs.getString("last_name");
                customerNames.put(id, name);
            }
        } catch (SQLException e) {
            showError("Could not load customers from the database.", e);
        }
    }

    // Load trip descriptions from database into the map
    private void loadTripsMap() {
        tripDescriptions.clear();
        try {
            ResultSet rs = DBService.executeQuery("SELECT trip_id, destination FROM trips");
            while (rs.next()) {
                int id = rs.getInt("trip_id");
                String desc = rs.getString("destination");
                tripDescriptions.put(id, desc);
            }
        } catch (SQLException e) {
            showError("Could not load trips from the database.", e);
        }
    }

    // Add a new booking to the database and refresh the table
    @FXML
    private void addBooking() {
        try {
            String customerName = customerCombo.getValue();
            String tripDestination = tripCombo.getValue();
            LocalDate bookingDate = bookingDatePicker.getValue();

            if (customerName == null || tripDestination == null || bookingDate == null) {
                showAlert("Please select customer, destination and date.");
                return;
            }

            // Find customerId and tripId based on selected names
            Integer customerId = customerNames.entrySet().stream()
                    .filter(e -> e.getValue().equals(customerName))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);

            Integer tripId = tripDescriptions.entrySet().stream()
                    .filter(e -> e.getValue().equals(tripDestination))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);

            if (customerId == null || tripId == null) {
                showAlert("Please choose an existing customer and trip.");
                return;
            }

            if (!hasAvailableSeats(tripId)) {
                showAlert("Not available: No seats left for this trip.");
                return;
            }


            String sql = "INSERT INTO bookings (customer_id, trip_id, booking_date, status) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
            stmt.setInt(1, customerId);
            stmt.setInt(2, tripId);
            stmt.setString(3, bookingDate.toString());
            stmt.setString(4, "pending");
            stmt.executeUpdate();

            // Clear input fields after adding
            customerCombo.setValue(null);
            tripCombo.setValue(null);
            bookingDatePicker.setValue(null);
            customerCombo.getEditor().clear();
            tripCombo.getEditor().clear();

            // Reload maps and ComboBoxes in case new customer/trip was added elsewhere
            loadCustomersMap();
            loadTripsMap();
            customerCombo.setItems(FXCollections.observableArrayList(customerNames.values()));
            tripCombo.setItems(FXCollections.observableArrayList(tripDescriptions.values()));

            loadBookingsFromDB();
            bookingsTable.refresh();

        } catch (SQLException e) {
            showError("Error during adding booking.", e);
        }
    }

    // Delete the selected booking from the database and table
    @FXML
    private void deleteBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a booking to delete.");
            return;
        }

        try {
            String sql = "DELETE FROM bookings WHERE booking_id=?";
            PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
            stmt.setInt(1, selected.getBookingId());
            stmt.executeUpdate();
            if ("confirmed".equalsIgnoreCase(selected.getStatus())) {
                updateTripSeats(selected.getTripId(), +1);
            }

            bookings.remove(selected);
            bookingsTable.refresh();

        } catch (SQLException e) {
            showError("Error during deleting booking.", e);
        }
    }

    // Load all bookings from the database into the observable list
    private void loadBookingsFromDB() {
        bookings.clear();
        try {
            ResultSet rs = DBService.executeQuery("SELECT * FROM bookings");
            while (rs.next()) {
                bookings.add(new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("trip_id"),
                        rs.getString("booking_date"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            showError("Could not load bookings from the database.", e);
        }
    }

    // Update the booking date in the database
    private void updateBookingInDB(Booking booking) {
        try {
            String sql = "UPDATE bookings SET booking_date=? WHERE booking_id=?";
            PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
            stmt.setString(1, booking.getBookingDate());
            stmt.setInt(2, booking.getBookingId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("Could not update the booking date.", e);
        }
    }

    // Create cell factory for Confirm and Cancel buttons in the actions column
    private Callback<TableColumn<Booking, Void>, TableCell<Booking, Void>> getActionsCellFactory() {
        return param -> new TableCell<Booking, Void>() {
            private final Button confirmBtn = new Button("Confirm");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox box = new HBox(5, confirmBtn, cancelBtn);

            {
                // Confirm booking
                confirmBtn.setOnAction(event -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    if (updateBookingStatusInDB(booking, "confirmed")) {
                        booking.setStatus("confirmed");
                        bookingsTable.refresh();
                    }
                });
                // Cancel booking
                cancelBtn.setOnAction(event -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    if (updateBookingStatusInDB(booking, "cancelled")) {
                        booking.setStatus("cancelled");
                        bookingsTable.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Booking booking = getTableView().getItems().get(getIndex());
                    // Show buttons only for pending bookings
                    if ("pending".equalsIgnoreCase(booking.getStatus())) {
                        setGraphic(box);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        };
    }

    // Update the status of a booking in the database
    private boolean updateBookingStatusInDB(Booking booking, String newStatus) {
        try {
            String oldStatus = booking.getStatus();
            if ("confirmed".equalsIgnoreCase(newStatus)
                    && !"confirmed".equalsIgnoreCase(oldStatus)
                    && !hasAvailableSeats(booking.getTripId())) {
                showAlert("Not available: No seats left for this trip.");
                return false;
            }

            String sql = "UPDATE bookings SET status=? WHERE booking_id=?";
            PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setInt(2, booking.getBookingId());
            stmt.executeUpdate();

            if ("confirmed".equalsIgnoreCase(newStatus) && !"confirmed".equalsIgnoreCase(oldStatus)) {
                updateTripSeats(booking.getTripId(), -1);
            } else if ("cancelled".equalsIgnoreCase(newStatus) && "confirmed".equalsIgnoreCase(oldStatus)) {
                updateTripSeats(booking.getTripId(), +1);
            }

            return true;
        } catch (SQLException e) {
            showError("Could not update the booking status.", e);
            return false;
        }
    }

    // Show an informational alert to the user
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateTripSeats(int tripId, int changeBy) {
        try {
            String sql = "UPDATE trips SET available_seats = available_seats + ? WHERE trip_id = ?";
            PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
            stmt.setInt(1, changeBy);
            stmt.setInt(2, tripId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("Could not update trip seats.", e);
        }
    }

    private boolean hasAvailableSeats(int tripId) {
        try {
            String sql = "SELECT available_seats FROM trips WHERE trip_id = ?";
            PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
            stmt.setInt(1, tripId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("available_seats") > 0;
            }
        } catch (SQLException e) {
            showError("Could not check available seats.", e);
        }
        return false;
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        showAlert(message);
    }


}
