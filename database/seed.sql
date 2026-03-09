USE apartment_listing_system;

-- 1. Turn off foreign key checks to clean up old data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE login_logs;
TRUNCATE TABLE notes;
TRUNCATE TABLE favorites;
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
('Hoang_Thi_Ngoc_Han', '8add439a140c5e33a3298d18bd8cdaf5d99bd44bc0d1f8346b59d74d093ef232', 'ADMIN'),
('Nguyen_Thanh_Tung', '17eff95a3821a5dbaded74a5ed7a447ed3e5548d6431a8c9bae9dc3c9c363963', 'AGENT'),
('Tran_Thu_Huong', '25630959122eefd58539ca5f945d528d4d97ea4507d7a147aa292c66e5fa788e', 'AGENT'),
('Le_Hai_Dang', 'c3644845ebefe2498f2ed7e8c75a724e0bbed3dfd986624ebab31683ae2cdf21', 'AGENT'),
('Pham_Minh_Anh', 'a63f9104ecee5876f9cc7e6f5e53adf845007a2457f648ff6d26721a335d94f2', 'AGENT'),
('Vu_Duc_Thang', 'a37daa6d648fb55d1f664327415934c7a75ae74e03af6c9986b53e8d0d51b5e9', 'AGENT');
-- 3. Amenities
INSERT INTO amenities (amenity_name) VALUES
('WiFi'), ('Air Conditioning'), ('Parking'), ('Swimming Pool'), 
('Gym'), ('Elevator'), ('Park'), ('School');

-- 4. Add apartments
INSERT INTO apartments (listing_code, address, location, price, bedrooms, size_sqft, category, created_by) VALUES
-- STUDIO and BUDGET (Suitable for students)
('APT-005', 'Ngõ 207 Giải Phóng, Đồng Tâm', 'Hai Bà Trưng, Hà Nội', 250.00, 1, 300, 'BUDGET', 6),
('APT-006', 'Ngõ 175 Xuân Thủy, Dịch Vọng Hậu', 'Cầu Giấy, Hà Nội', 300.00, 1, 350, 'STUDIO', 7),
('APT-007', 'Ngõ 104 Lê Thanh Nghị, Bách Khoa', 'Hai Bà Trưng, Hà Nội', 200.00, 1, 250, 'BUDGET', 8),
('APT-008', 'Vinhomes Smart City, Tây Mỗ', 'Nam Từ Liêm, Hà Nội', 450.00, 1, 400, 'STUDIO', 9),

-- ONE_BEDROOM (One bedroom apartment)
('APT-009', '15 Đào Duy Từ, Hàng Buồm', 'Hoàn Kiếm, Hà Nội', 650.00, 1, 550, 'ONE_BEDROOM', 10),
('APT-010', 'Vinhomes Ocean Park, Đa Tốn', 'Gia Lâm, Hà Nội', 500.00, 1, 500, 'ONE_BEDROOM', 6),
('APT-011', 'Masteri Centre Point, Quận 9', 'TP. Thủ Đức, HCM', 550.00, 1, 520, 'ONE_BEDROOM', 7),

-- STANDARD & TWO_BEDROOM (Standard family apartment)
('APT-012', 'Times City, 458 Minh Khai', 'Hai Bà Trưng, Hà Nội', 900.00, 2, 850, 'STANDARD', 8),
('APT-013', 'Royal City, 72A Nguyễn Trãi', 'Thanh Xuân, Hà Nội', 1100.00, 2, 1050, 'TWO_BEDROOM', 9),
('APT-014', 'Goldmark City, 136 Hồ Tùng Mậu', 'Bắc Từ Liêm, Hà Nội', 750.00, 2, 800, 'STANDARD', 10),
('APT-015', 'Masteri Thảo Điền, Xa lộ Hà Nội', 'Quận 2, TP.HCM', 1200.00, 2, 900, 'TWO_BEDROOM', 6),
('APT-016', 'Mipec Riverside, Ngọc Lâm', 'Long Biên, Hà Nội', 850.00, 2, 850, 'STANDARD', 7),
('APT-017', '54 Nguyễn Chí Thanh, Láng Thượng', 'Đống Đa, Hà Nội', 800.00, 2, 750, 'TWO_BEDROOM', 8),

-- LUXURY (Luxurious apartment)
('APT-018', 'Vinhomes Metropolis, 29 Liễu Giai', 'Ba Đình, Hà Nội', 2500.00, 3, 1300, 'LUXURY', 9),
('APT-019', 'Sun Grand City, 69B Thụy Khuê', 'Tây Hồ, Hà Nội', 1800.00, 2, 1000, 'LUXURY', 10),
('APT-020', 'Pacific Place, 83 Lý Thường Kiệt', 'Hoàn Kiếm, Hà Nội', 2800.00, 3, 1400, 'LUXURY', 6),

-- DUPLEX & PENTHOUSE (Duplex apartment & super luxurious)
('APT-021', 'Keangnam Landmark 72, Phạm Hùng', 'Nam Từ Liêm, Hà Nội', 2200.00, 3, 1600, 'DUPLEX', 7),
('APT-022', 'Indochina Plaza, 241 Xuân Thủy', 'Cầu Giấy, Hà Nội', 1900.00, 3, 1500, 'DUPLEX', 8),
('APT-023', 'Landmark 81, Vinhomes Central Park', 'Bình Thạnh, TP.HCM', 4500.00, 4, 2500, 'PENTHOUSE', 9),
('APT-024', 'Đảo Kim Cương (Diamond Island)', 'Quận 2, TP.HCM', 4000.00, 4, 2200, 'PENTHOUSE', 10);


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
