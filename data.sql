USE campus_facility_db;

-- Test record: STUDENT receives no discount.
INSERT INTO reservations
(user_name, user_role, item_category, quantity, booking_days,
 daily_rate, subtotal, net_total)
VALUES
('TEST STUDENT', 'STUDENT', 'PROJECTOR', 1, 2, 80.00, 160.00, 160.00);

-- Test record: FACULTY receives a 15% discount.
INSERT INTO reservations
(user_name, user_role, item_category, quantity, booking_days,
 daily_rate, subtotal, net_total)
VALUES
('TEST FACULTY', 'FACULTY', 'LAPTOP', 2, 3, 50.00, 300.00, 255.00);
