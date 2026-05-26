package com.verav.travelagency.model;

import javafx.beans.property.*;

public class Customer {
    private IntegerProperty customerId;
    private StringProperty firstName;
    private StringProperty lastName;
    private StringProperty email;
    private StringProperty phone;
    private StringProperty country;

    public Customer(int customerId, String firstName, String lastName, String email, String phone, String country) {
        this.customerId = new SimpleIntegerProperty(customerId);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.country = new SimpleStringProperty(country);
    }

    public int getCustomerId() { return customerId.get(); }
    public IntegerProperty customerIdProperty() { return customerId; }

    public String getFirstName() { return firstName.get(); }
    public StringProperty firstNameProperty() { return firstName; }

    public String getLastName() { return lastName.get(); }
    public StringProperty lastNameProperty() { return lastName; }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }

    public String getPhone() { return phone.get(); }
    public StringProperty phoneProperty() { return phone; }

    public String getCountry() { return country.get(); }
    public StringProperty countryProperty() { return country; }
}
