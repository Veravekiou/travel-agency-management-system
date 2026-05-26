package com.verav.travelagency.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.verav.travelagency.common.ValidationUtils;
import com.verav.travelagency.model.Customer;
import com.verav.travelagency.services.DBService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomersController extends BaseController {

    // UI components for the customers table and columns
    @FXML private TableView<Customer> customersTable;
    @FXML private TableColumn<Customer, Integer> idColumn;
    @FXML private TableColumn<Customer, String> firstNameColumn;
    @FXML private TableColumn<Customer, String> lastNameColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> countryColumn;

    // Buttons for adding, editing, and deleting customers
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    // List to hold all customers data
    private ObservableList<Customer> customers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up cell value factories for each column
        idColumn.setCellValueFactory(cellData -> cellData.getValue().customerIdProperty().asObject());
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        countryColumn.setCellValueFactory(cellData -> cellData.getValue().countryProperty());

        // Load customers from the database and set them to the table
        loadCustomersFromDB();
        customersTable.setItems(customers);

        // Set button actions
        addButton.setOnAction(e -> handleAdd());
        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());
    }

    // Load all customers from the database
    private void loadCustomersFromDB() {
        customers.clear();
        String sql = "SELECT * FROM customers";
        try (PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("customer_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String country = rs.getString("country");
                customers.add(new Customer(id, firstName, lastName, email, phone, country));
            }
        } catch (SQLException e) {
            showError("Could not load customers from the database.", e);
        }
    }

    // Handle adding a new customer
    private void handleAdd() {
        Customer newCustomer = showCustomerDialog(null);
        if (newCustomer == null) return;

        try {
            String sql = "INSERT INTO customers (first_name, last_name, email, phone, country) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
            stmt.setString(1, newCustomer.getFirstName());
            stmt.setString(2, newCustomer.getLastName());
            stmt.setString(3, newCustomer.getEmail());
            stmt.setString(4, newCustomer.getPhone());
            stmt.setString(5, newCustomer.getCountry());
            stmt.executeUpdate();
            loadCustomersFromDB(); // Refresh the table after adding
        } catch (SQLException e) {
            showError("Could not add the customer.", e);
        }
    }

    // Handle editing an existing customer
    private void handleEdit() {
        Customer selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a customer to edit.");
            return;
        }

        Customer updatedCustomer = showCustomerDialog(selected);
        if (updatedCustomer == null) return;

        try {
            String sql = "UPDATE customers SET first_name=?, last_name=?, email=?, phone=?, country=? WHERE customer_id=?";
            PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
            stmt.setString(1, updatedCustomer.getFirstName());
            stmt.setString(2, updatedCustomer.getLastName());
            stmt.setString(3, updatedCustomer.getEmail());
            stmt.setString(4, updatedCustomer.getPhone());
            stmt.setString(5, updatedCustomer.getCountry());
            stmt.setInt(6, updatedCustomer.getCustomerId());
            stmt.executeUpdate();
            loadCustomersFromDB(); // Refresh the table after editing
        } catch (SQLException e) {
            showError("Could not update the customer.", e);
        }
    }

    // Handle deleting a customer
    private void handleDelete() {
        Customer selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a customer to delete.");
            return;
        }

        try {
            // checking for bookings
            String checkBookingsSQL = "SELECT COUNT(*) FROM bookings WHERE customer_id = ?";
            PreparedStatement checkStmt = DBService.getConnection().prepareStatement(checkBookingsSQL);
            checkStmt.setInt(1, selected.getCustomerId());
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int bookingCount = rs.getInt(1);

            if (bookingCount > 0) {

                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Confirm Deletion");
                confirmDialog.setHeaderText("This customer has " + bookingCount + " booking(s).");
                confirmDialog.setContentText("Do you want to delete the customer and all their bookings?");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirmDialog.getButtonTypes().setAll(yesButton, noButton);

                confirmDialog.showAndWait().ifPresent(response -> {
                    if (response == yesButton) {
                        try {
                            restoreSeatsForConfirmedBookings(selected.getCustomerId());

                            // 1. delete bookings
                            String deleteBookingsSQL = "DELETE FROM bookings WHERE customer_id = ?";
                            PreparedStatement deleteBookingsStmt = DBService.getConnection().prepareStatement(deleteBookingsSQL);
                            deleteBookingsStmt.setInt(1, selected.getCustomerId());
                            deleteBookingsStmt.executeUpdate();

                            // 2. delete customer
                            String deleteCustomerSQL = "DELETE FROM customers WHERE customer_id = ?";
                            PreparedStatement deleteCustomerStmt = DBService.getConnection().prepareStatement(deleteCustomerSQL);
                            deleteCustomerStmt.setInt(1, selected.getCustomerId());
                            deleteCustomerStmt.executeUpdate();

                            loadCustomersFromDB();
                            showAlert("Customer and their bookings were deleted successfully.");
                        } catch (SQLException e) {
                            showError("Error while deleting customer and bookings.", e);
                        }
                    }
                });
            } else {
                // customer without bookings -> usual delete
                String deleteSQL = "DELETE FROM customers WHERE customer_id = ?";
                PreparedStatement stmt = DBService.getConnection().prepareStatement(deleteSQL);
                stmt.setInt(1, selected.getCustomerId());
                stmt.executeUpdate();
                loadCustomersFromDB();
                showAlert("Customer deleted successfully.");
            }

        } catch (SQLException e) {
            showError("An error occurred while checking or deleting the customer.", e);
        }
    }


    // Show a dialog for adding or editing a customer
    private Customer showCustomerDialog(Customer existingCustomer) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle(existingCustomer == null ? "Add Customer" : "Edit Customer");
        dialog.setHeaderText(null);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        TextField countryField = new TextField();

        if (existingCustomer != null) {
            firstNameField.setText(existingCustomer.getFirstName());
            lastNameField.setText(existingCustomer.getLastName());
            emailField.setText(existingCustomer.getEmail());
            phoneField.setText(existingCustomer.getPhone());
            countryField.setText(existingCustomer.getCountry());
        }

        grid.addRow(0, new Label("First Name:"), firstNameField);
        grid.addRow(1, new Label("Last Name:"), lastNameField);
        grid.addRow(2, new Label("Email:"), emailField);
        grid.addRow(3, new Label("Phone:"), phoneField);
        grid.addRow(4, new Label("Country:"), countryField);

        dialog.getDialogPane().setContent(grid);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String validationError = ValidationUtils.validateCustomerInput(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    countryField.getText()
            );
            if (validationError != null) {
                showAlert(validationError);
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Customer(
                        existingCustomer == null ? 0 : existingCustomer.getCustomerId(),
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        countryField.getText().trim()
                );
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private void restoreSeatsForConfirmedBookings(int customerId) throws SQLException {
        String sql = """
                SELECT trip_id, COUNT(*) AS confirmed_count
                FROM bookings
                WHERE customer_id = ? AND status = 'confirmed'
                GROUP BY trip_id
                """;
        PreparedStatement stmt = DBService.getConnection().prepareStatement(sql);
        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String updateSql = "UPDATE trips SET available_seats = available_seats + ? WHERE trip_id = ?";
            PreparedStatement updateStmt = DBService.getConnection().prepareStatement(updateSql);
            updateStmt.setInt(1, rs.getInt("confirmed_count"));
            updateStmt.setInt(2, rs.getInt("trip_id"));
            updateStmt.executeUpdate();
        }
    }

    // Show an alert message to the user
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        showAlert(message);
    }
}
