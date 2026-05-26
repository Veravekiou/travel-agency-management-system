package com.verav.travelagency.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import com.verav.travelagency.model.Booking;
import com.verav.travelagency.services.DBService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class HomeController extends BaseController {

    // UI components for displaying data
    @FXML private Label dateLabel, userLabel, pendingBookingsLabel, confirmedBookingsLabel, popularDestinationsLabel;
    @FXML private AreaChart<String, Number> popularDestinationsChart;

    // Data collections
    private ObservableList<Booking> bookings = FXCollections.observableArrayList();
    private Map<Integer, String> tripDestinations = new HashMap<>();

    @FXML
    public void initialize() {
        // Set current date and admin user label
        dateLabel.setText(LocalDate.now().toString());
        userLabel.setText("Admin");

        // Load data and update UI
        loadTripsMap();
        loadBookings();
        setStats();
        fillCharts();
    }

    // Load all bookings from the database into the bookings list
    private void loadBookings() {
        bookings.clear();
        String sql = "SELECT * FROM bookings";
        try (PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
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
            e.printStackTrace();
        }
    }

    // Load trip destinations from the database into a map (trip_id -> destination)
    private void loadTripsMap() {
        tripDestinations.clear();
        String sql = "SELECT trip_id, destination FROM trips";
        try (PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tripDestinations.put(rs.getInt("trip_id"), rs.getString("destination"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Calculate and display statistics (pending, confirmed bookings, popular destinations)
    private void setStats() {
        // Count pending bookings
        long pendingCount = bookings.stream()
                .filter(b -> "pending".equalsIgnoreCase(b.getStatus()))
                .count();
        pendingBookingsLabel.setText(String.valueOf(pendingCount));

        // Count confirmed bookings
        long confirmedCount = bookings.stream()
                .filter(b -> "confirmed".equalsIgnoreCase(b.getStatus()))
                .count();
        confirmedBookingsLabel.setText(String.valueOf(confirmedCount));

        // Find and display top 3 most popular destinations
        Map<String, Integer> destCount = new HashMap<>();
        for (Booking b : bookings) {
            String dest = tripDestinations.getOrDefault(b.getTripId(), "Unknown");
            destCount.put(dest, destCount.getOrDefault(dest, 0) + 1);
        }
        List<Map.Entry<String, Integer>> topDest = new ArrayList<>(destCount.entrySet());
        topDest.sort((a, b) -> b.getValue() - a.getValue());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(3, topDest.size()); i++) {
            if (i > 0) sb.append(", ");
            sb.append(topDest.get(i).getKey());
        }
        popularDestinationsLabel.setText(sb.toString());
    }

    // Update the chart with the most popular destinations and their booking counts
    private void fillCharts() {
        Map<String, Integer> destCount = new HashMap<>();
        for (Booking b : bookings) {
            String dest = tripDestinations.getOrDefault(b.getTripId(), "Unknown");
            destCount.put(dest, destCount.getOrDefault(dest, 0) + 1);
        }

        popularDestinationsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bookings");

        // Sort destinations by booking count and add to chart (excluding "Unknown")
        destCount.entrySet().stream()
                .filter(e -> !"Unknown".equals(e.getKey()))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        popularDestinationsChart.getData().add(series);
    }
}
