USE agency_db;

INSERT INTO customers (first_name, last_name, email, phone, country) VALUES
  ('Maria', 'Papadopoulou', 'maria.papadopoulou@example.com', '+30 210 555 0101', 'Greece'),
  ('Nikos', 'Georgiou', 'nikos.georgiou@example.com', '+30 210 555 0102', 'Greece'),
  ('Eleni', 'Dimitriou', 'eleni.dimitriou@example.com', '+30 210 555 0103', 'Cyprus'),
  ('Andreas', 'Ioannou', 'andreas.ioannou@example.com', '+357 22 555 010', 'Cyprus'),
  ('Sofia', 'Markou', 'sofia.markou@example.com', '+30 2310 555 104', 'Greece');

INSERT INTO trips (destination, departure_date, return_date, price, available_seats) VALUES
  ('Santorini', '2026-06-15', '2026-06-20', 680.00, 18),
  ('Rome', '2026-07-03', '2026-07-08', 540.00, 22),
  ('Paris', '2026-08-10', '2026-08-17', 920.00, 14),
  ('Barcelona', '2026-09-05', '2026-09-11', 750.00, 16),
  ('Vienna', '2026-12-18', '2026-12-23', 610.00, 20);

INSERT INTO bookings (customer_id, trip_id, booking_date, status) VALUES
  (1, 1, '2026-05-10', 'confirmed'),
  (2, 2, '2026-05-12', 'pending'),
  (3, 3, '2026-05-15', 'confirmed'),
  (4, 1, '2026-05-18', 'cancelled'),
  (5, 4, '2026-05-20', 'pending'),
  (1, 5, '2026-05-22', 'confirmed');
