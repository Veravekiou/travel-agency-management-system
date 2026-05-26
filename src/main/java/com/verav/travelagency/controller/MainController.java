package com.verav.travelagency.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController extends BaseController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to our travel agency!");
    }

    @FXML
    protected void loadHome() {
        loadModule("home");
    }

    @FXML
    protected void loadcustomers() {
        loadModule("customers");
    }

    @FXML
    protected void loadtrips() {
        loadModule("Trips");
    }

    @FXML
    protected void loadbookings() {
        loadModule("Bookings");
    }




    @Override
    public void initialize() {
        super.initialize();
        loadModule("home");  // default home page
    }


}