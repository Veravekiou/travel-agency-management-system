CREATE DATABASE IF NOT EXISTS agency_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE agency_db;

CREATE TABLE IF NOT EXISTS customers (
  customer_id INT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  phone VARCHAR(50) NOT NULL,
  country VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS trips (
  trip_id INT AUTO_INCREMENT PRIMARY KEY,
  destination VARCHAR(150) NOT NULL,
  departure_date DATE NOT NULL,
  return_date DATE NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  available_seats INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS bookings (
  booking_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  trip_id INT NOT NULL,
  booking_date DATE NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'pending',
  CONSTRAINT fk_bookings_customer
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_bookings_trip
    FOREIGN KEY (trip_id) REFERENCES trips(trip_id)
    ON DELETE CASCADE
);
