USE apartment_listing_system;

INSERT INTO users (username, password_hash, role) VALUES
('admin', '$2a$10$adminhashedpassword', 'ADMIN'),
('agent_john', '$2a$10$johnhashedpassword', 'AGENT'),
('agent_mary', '$2a$10$maryhashedpassword', 'AGENT');

INSERT INTO amenities (amenity_name) VALUES
('WiFi'),
('Air Conditioning'),
('Parking'),
('Swimming Pool'),
('Gym'),
('Elevator'),
('Pet Friendly');

INSERT INTO apartments
(listing_code, address, location, price, bedrooms, size_sqft, category, created_by)
VALUES
('APT-001', '123 Main Street', 'Downtown', 1500.00, 2, 850, 'Standard', 2),
('APT-002', '45 Riverside Ave', 'Riverside', 2800.00, 3, 1400, 'Luxury', 2),
('APT-003', '78 Maple Road', 'Suburb', 950.00, 1, 550, 'Budget', 3),
('APT-004', '9 Lake View', 'Downtown', 2200.00, 2, 1100, 'Luxury', 3);

INSERT INTO favorites (user_id, apartment_id) VALUES
(2, 2),
(2, 4),
(3, 1);

INSERT INTO notes (apartment_id, user_id, note_text) VALUES
(1, 2, 'Client interested, scheduled viewing next week.'),
(2, 2, 'High-end client, prefers long-term lease.'),
(3, 3, 'Budget option, popular among students.'),
(4, 3, 'Excellent location, strong demand.');

INSERT INTO login_logs (user_id, action) VALUES
(1, 'LOGIN'),
(1, 'LOGOUT'),
(2, 'LOGIN'),
(3, 'LOGIN');

INSERT INTO audit_logs (user_id, apartment_id, action, details) VALUES
(2, 1, 'CREATE', 'Initial apartment listing created'),
(2, 2, 'CREATE', 'Luxury apartment added'),
(3, 3, 'CREATE', 'Budget apartment added'),
(3, 4, 'UPDATE', 'Price updated after market review');
