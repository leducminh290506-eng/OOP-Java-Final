USE apartment_listing_system;

-- 1. Turn off foreign key checks to clean up old data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE login_logs;
TRUNCATE TABLE notes;
TRUNCATE TABLE favorites;
TRUNCATE TABLE apartment_amenities;
TRUNCATE TABLE lease_contracts; 
TRUNCATE TABLE apartments;
TRUNCATE TABLE amenities;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 2.Users
INSERT INTO users (username, password_hash, role) VALUES
('admin', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 'ADMIN'),
('Le_Duc_Minh', '00e71b6c592a74c8cd300c6daba5d5baeda7661a50cc0c62e5c3de21866c3a64', 'ADMIN'),
('Tran_Tue_Khang', '756669c5cff1c9d7cb728ee9a579fa5498600da6cea1fca59ed36a606e60cf1f', 'ADMIN'),
('Nguyen_Bao_Tai', '9efa074e64a3093c644d49404c839a81ab6efeffac81ab387a18d10e257ddedd', 'ADMIN'),
('Hoang_Thi_Ngoc_Han', '8add439a140c5e33a3298d18bd8cdaf5d99bd44bc0d1f8346b59d74d093ef232', 'ADMIN');

-- 3. Amenities
INSERT INTO amenities (amenity_name) VALUES
('WiFi'), ('Air Conditioning'), ('Parking'), ('Swimming Pool'), 
('Gym'), ('Elevator'), ('Park'), ('School');

-- 4. Add apartments (Mặc định status là AVAILABLE)
-- ĐÃ FIX: Đổi created_by (số cuối cùng) thành 1 hoặc 2 để không bị lỗi Khóa ngoại
INSERT INTO apartments (listing_code, address, location, price, bedrooms, size_m2, category, created_by) VALUES
-- STUDIO and BUDGET (Suitable for students)
('APT-005', 'Ngõ 207 Giải Phóng, Đồng Tâm', 'Hai Bà Trưng, Hà Nội', 250.00, 1, 300, 'BUDGET', 1),
('APT-006', 'Ngõ 175 Xuân Thủy, Dịch Vọng Hậu', 'Cầu Giấy, Hà Nội', 300.00, 1, 350, 'STUDIO', 2),
('APT-007', 'Ngõ 104 Lê Thanh Nghị, Bách Khoa', 'Hai Bà Trưng, Hà Nội', 200.00, 1, 250, 'BUDGET', 1),
('APT-008', 'Vinhomes Smart City, Tây Mỗ', 'Nam Từ Liêm, Hà Nội', 450.00, 1, 400, 'STUDIO', 2),

-- ONE_BEDROOM (One bedroom apartment)
('APT-009', '15 Đào Duy Từ, Hàng Buồm', 'Hoàn Kiếm, Hà Nội', 650.00, 1, 550, 'ONE_BEDROOM', 1),
('APT-010', 'Vinhomes Ocean Park, Đa Tốn', 'Gia Lâm, Hà Nội', 500.00, 1, 500, 'ONE_BEDROOM', 2),
('APT-011', 'Masteri Centre Point, Quận 9', 'TP. Thủ Đức, HCM', 550.00, 1, 520, 'ONE_BEDROOM', 1),

-- STANDARD & TWO_BEDROOM (Standard family apartment)
('APT-012', 'Times City, 458 Minh Khai', 'Hai Bà Trưng, Hà Nội', 900.00, 2, 850, 'STANDARD', 2),
('APT-013', 'Royal City, 72A Nguyễn Trãi', 'Thanh Xuân, Hà Nội', 1100.00, 2, 1050, 'TWO_BEDROOM', 1),
('APT-014', 'Goldmark City, 136 Hồ Tùng Mậu', 'Bắc Từ Liêm, Hà Nội', 750.00, 2, 800, 'STANDARD', 2),
('APT-015', 'Masteri Thảo Điền, Xa lộ Hà Nội', 'Quận 2, TP.HCM', 1200.00, 2, 900, 'TWO_BEDROOM', 1),
('APT-016', 'Mipec Riverside, Ngọc Lâm', 'Long Biên, Hà Nội', 850.00, 2, 850, 'STANDARD', 2),
('APT-017', '54 Nguyễn Chí Thanh, Láng Thượng', 'Đống Đa, Hà Nội', 800.00, 2, 750, 'TWO_BEDROOM', 1),

-- LUXURY (Luxurious apartment)
('APT-018', 'Vinhomes Metropolis, 29 Liễu Giai', 'Ba Đình, Hà Nội', 2500.00, 3, 1300, 'LUXURY', 2),
('APT-019', 'Sun Grand City, 69B Thụy Khuê', 'Tây Hồ, Hà Nội', 1800.00, 2, 1000, 'LUXURY', 1),
('APT-020', 'Pacific Place, 83 Lý Thường Kiệt', 'Hoàn Kiếm, Hà Nội', 2800.00, 3, 1400, 'LUXURY', 2),

-- DUPLEX & PENTHOUSE (Duplex apartment & super luxurious)
('APT-021', 'Keangnam Landmark 72, Phạm Hùng', 'Nam Từ Liêm, Hà Nội', 2200.00, 3, 1600, 'DUPLEX', 1),
('APT-022', 'Indochina Plaza, 241 Xuân Thủy', 'Cầu Giấy, Hà Nội', 1900.00, 3, 1500, 'DUPLEX', 2),
('APT-023', 'Landmark 81, Vinhomes Central Park', 'Bình Thạnh, TP.HCM', 4500.00, 4, 2500, 'PENTHOUSE', 1),
('APT-024', 'Đảo Kim Cương (Diamond Island)', 'Quận 2, TP.HCM', 4000.00, 4, 2200, 'PENTHOUSE', 2);

-- 5. Map amenities to apartments (fix filter-by-amenities returning empty)
INSERT INTO apartment_amenities (apartment_id, amenity_id) VALUES
-- APT-005 .. APT-008 (ids 1..4): budget/studio
(1, 1), (1, 3),
(2, 1), (2, 2), (2, 3),
(3, 1),
(4, 1), (4, 3), (4, 6), (4, 4),

-- APT-009 .. APT-011 (ids 5..7): one bedroom
(5, 1), (5, 2), (5, 6),
(6, 1), (6, 3), (6, 4), (6, 5),
(7, 1), (7, 2), (7, 5),

-- APT-012 .. APT-017 (ids 8..13): standard/two bedroom
(8, 1), (8, 3), (8, 6), (8, 7),
(9, 1), (9, 2), (9, 3), (9, 4), (9, 5), (9, 6),
(10, 1), (10, 3), (10, 6),
(11, 1), (11, 2), (11, 4), (11, 5), (11, 6),
(12, 1), (12, 3), (12, 6),
(13, 1), (13, 2), (13, 3), (13, 6),

-- APT-018 .. APT-024 (ids 14..20): luxury/duplex/penthouse
(14, 1), (14, 2), (14, 3), (14, 4), (14, 5), (14, 6),
(15, 1), (15, 2), (15, 4), (15, 5), (15, 6),
(16, 1), (16, 2), (16, 3), (16, 4), (16, 6),
(17, 1), (17, 2), (17, 3), (17, 5), (17, 6),
(18, 1), (18, 2), (18, 3), (18, 4), (18, 5), (18, 6),
(19, 1), (19, 2), (19, 3), (19, 4), (19, 5), (19, 6),
(20, 1), (20, 2), (20, 3), (20, 4), (20, 5), (20, 6);

INSERT INTO favorites (user_id, apartment_id) VALUES
(2, 2), (2, 4), (3, 1);

INSERT INTO notes (apartment_id, user_id, note_text) VALUES
(1, 2, 'Client interested, scheduled viewing next week.'),
(2, 2, 'High-end client, prefers long-term lease.'),
(3, 3, 'Budget option, popular among students.'),
(4, 3, 'Excellent location, strong demand.');

INSERT INTO login_logs (user_id, action) VALUES
(1, 'LOGIN'), (1, 'LOGOUT'), (2, 'LOGIN'), (3, 'LOGIN');

INSERT INTO audit_logs (user_id, apartment_id, action, details) VALUES
(2, 1, 'CREATE', 'Initial apartment listing created'),
(2, 2, 'CREATE', 'Luxury apartment added'),
(3, 3, 'CREATE', 'Budget apartment added'),
(3, 4, 'UPDATE', 'Price updated after market review');

-- Đổi trạng thái một vài căn hộ để vẽ Pie Chart cho đẹp
UPDATE apartments SET status = 'RENTED' WHERE apartment_id IN (1, 2, 5, 8, 12, 18);
UPDATE apartments SET status = 'MAINTENANCE' WHERE apartment_id IN (3, 10);