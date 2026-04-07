# Design Document

## 1. Danh sách các lớp và vai trò (Class List & Responsibilities)

Liệt kê các class chính trong hệ thống và mô tả ngắn gọn vai trò của từng class.

| Class | Package | Vai trò |
|Main|com.oop.project|Điểm bắt đầu của ứng dụng, thiết lập Look & Feel và khởi chạy |
|Login|Dialog.Apartmentcom.oop.project.model |Đại diện cho thực thể căn hộ, chứa logic tự động phân loại (Luxury, Standard, Budget) |
|User |com.oop.project.model |Lưu trữ thông tin tài khoản, mật khẩu (hash) và vai trò người dùng |
|ApartmentRepository |com.oop.project.repository |Thực hiện các truy vấn CRUD (Thêm, Sửa, Xóa, Lấy dữ liệu) với bảng apartments trong MySQL |
|AuthService |com.oop.project.service |Xử lý logic đăng nhập, kiểm tra thông tin xác thực từ database |
|MainFrame |com.oop.project.ui |Cửa sổ giao diện chính chứa các Panel chức năng sau khi đăng nhập thành công |
|DatabaseConnection |com.oop.project.util |Quản lý kết nối JDBC đến cơ sở dữ liệu MySQL apartment_listing_system. |

---

## 2. Áp dụng các nguyên lý OOP

Mô tả rõ **từng nguyên lý OOP được áp dụng ở đâu trong hệ thống**.

### 2.1. Encapsulation
Mô tả: Tất cả các thuộc tính trong các lớp Model như Apartment, User, Note đều được khai báo là private (ví dụ: private double price, private String username).


Truy cập: Thông qua các phương thức Getter/Setter công khai như getPrice(), setPrice(), getUsername().

Lý do: Bảo vệ tính toàn vẹn của dữ liệu. Ví dụ, trong Apartment.java, khi gọi setPrice(), hệ thống tự động gọi lại hàm classifyByPriceAndArea() để cập nhật lại phân loại căn hộ ngay lập tức.

---

### 2.2. Inheritance
Mô tả: Sử dụng mạnh mẽ trong phần UI (Swing).


Chi tiết: MainFrame kế thừa từ JFrame, các Panel như DashboardPanel, ListingPanel kế thừa từ JPanel, và các Dialog như LoginDialog kế thừa từ JDialog.

Lý do: Tái sử dụng các thuộc tính và phương thức xây dựng giao diện của thư viện Java Swing.

---

### 2.3. Polymorphism

Mô tả: Thể hiện qua việc Override các phương thức.

Chi tiết: Các lớp Repository cụ thể (như ApartmentRepository) hiện thực hóa (implement) các phương thức được định nghĩa trong Interface IRepository.

Lý do: Giúp mã nguồn linh hoạt, có thể gọi phương thức lưu trữ thông qua tham chiếu kiểu Interface mà không cần quan tâm lớp thực thi cụ thể là gì.

---

### 2.4. Interface

Mô tả: Sử dụng IRepository trong package repository.
Vai trò: Định nghĩa một quy tắc chung cho các thao tác dữ liệu (như findAll, findById, save). Điều này giúp tách biệt logic nghiệp vụ khỏi chi tiết cài đặt database.

---

### 2.5. Abstraction

Mô tả: Hệ thống ẩn đi sự phức tạp của việc truy vấn SQL đằng sau các phương thức của lớp Service và Repository.
Chi tiết: Người dùng ở tầng UI chỉ cần gọi apartmentService.getAllApartments(), họ không cần biết bên dưới đang thực hiện SELECT * FROM apartments JOIN ....

---

## 3. Design Patterns được sử dụng

Liệt kê các design pattern (nếu có) và giải thích ngắn gọn cách áp dụng.

| Design Pattern | Áp dụng ở đâu | Mục đích |
|Repository Pattern|Package repository|Tách biệt logic truy cập dữ liệu khỏi logic nghiệp vụ, giúp dễ dàng thay đổi database nếu cần|
|Singleton |DatabaseConnection |Đảm bảo chỉ có một kết nối duy nhất đến database được khởi tạo để tiết kiệm tài nguyên |
|MVC (Model-View-Controller |Toàn bộ dự án |Phân chia project thành 3 phần: Model (Dữ liệu), View (UI), và Controller/Service (Điều hướng & Logic). |


---

## 4. Luồng hoạt động chính (Main Application Flows)

Mô tả các luồng xử lý chính của hệ thống theo dạng từng bước.

### 4.1. Login
1. gười dùng nhập username và password vào LoginDialog.
2. LoginDialog gọi AuthService để xác thực.
3. AuthService dùng PasswordUtil để băm mật khẩu người dùng nhập và so sánh với password_hash trong bảng users.
4. Nếu khớp, MainFrame được hiển thị và lưu thông tin Role để phân quyền.

---

### 4.2. Luồng Phân loại Căn hộ Tự động (Auto-Classification)
1. Khi Agent thêm mới hoặc cập nhật một căn hộ thông qua UI.
2. Đối tượng Apartment được khởi tạo với các tham số price và area.
3. Constructor của Apartment tự động gọi classifyByPriceAndArea().
4. Kết quả (Luxury/Standard/Budget) được lưu vào thuộc tính category và đồng bộ vào database

---

## 5. Class Diagram

- Vẽ **class diagram** cho hệ thống bằng **draw.io**.
- Sơ đồ phải thể hiện:
  - Quan hệ kế thừa
  - Quan hệ association / composition (nếu có)
  - Interface và class implement

📌 **Yêu cầu:**
- Xuất sơ đồ thành file ảnh (PNG hoặc JPG).
- Lưu tại: `docs/class-diagram.png`

---

## 6. Thiết kế lưu trữ dữ liệu (Database / File Design)

Mô tả cách hệ thống lưu trữ dữ liệu.

### 6.1. Hình thức lưu trữ Database (MySQL)
- [ ] In-memory
- [ ] File (txt / csv / json)
- [ ] Database (MySQL, SQLite, ...)

**Mô tả lý do lựa chọn:**
Lý do: Dự án cần quản lý quan hệ phức tạp (như căn hộ và tiện ích), yêu cầu tính nhất quán dữ liệu cao thông qua Foreign Keys và hỗ trợ truy vấn báo cáo nhanh chóng bằng SQL.

---

### 6.2. Cấu trúc dữ liệu lưu trữ

Mô tả các bảng / file chính và dữ liệu được lưu trữ.

| Tên bảng / file | Mô tả | Dữ liệu chính |
|users|Thông tin tài khoản|username, password_hash, role|
|apartments |Thông tin căn hộ |listing_code, price, area, category |
|amenities |Danh mục tiện ích |amenity_name (WiFi, Gym, Pool,...) |
|notes |Ghi chú nội bộ |note_text, user_id, apartment_id |
|audit_logs |Nhật ký thay đổi |action (CREATE/UPDATE/DELETE), timestamp |

---

## 7. Nhận xét về thiết kế (Optional)

7.1. Ưu điểm của thiết kế hiện tại
Phân lớp rõ ràng (Layered Architecture): Việc tách biệt giữa UI, Service, Repository và Model giúp mã nguồn cực kỳ dễ đọc. Logic xử lý dữ liệu không bị trộn lẫn với giao diện, cho phép nhiều thành viên trong nhóm có thể làm việc trên các phần khác nhau mà không gây xung đột (ví dụ: một người làm GUI, một người tối ưu SQL).

Tính đóng gói (Encapsulation) cao: Các Model như Apartment.java không chỉ đơn thuần là chứa dữ liệu mà còn chứa cả logic nghiệp vụ (như tự động phân loại). Điều này đảm bảo dữ liệu luôn ở trạng thái hợp lệ ngay khi đối tượng được tạo ra.

Quản lý dữ liệu chặt chẽ (Data Integrity): Hệ thống Database sử dụng đầy đủ các ràng buộc khóa ngoại (Foreign Keys) và ON DELETE CASCADE. Điều này ngăn chặn việc tồn tại "dữ liệu rác" (ví dụ: một ghi chú vẫn tồn tại khi căn hộ đã bị xóa).

Khả năng truy vết (Auditability): Việc thiết kế bảng audit_logs và login_logs là một điểm cộng lớn, giúp hệ thống kiểm soát được ai đã thay đổi nội dung gì, vào lúc nào—một yêu cầu bắt buộc đối với các phần mềm quản lý doanh nghiệp.

7.2. Hạn chế
Sự phụ thuộc vào Swing: Việc kế thừa trực tiếp từ các lớp của Java Swing trong package ui khiến giao diện bị gắn chặt với công nghệ Desktop cũ. Nếu muốn chuyển sang nền tảng Web hoặc Mobile trong tương lai, phần lớn mã nguồn ở tầng UI sẽ phải viết lại hoàn toàn.

Xử lý ngoại lệ (Exception Handling): Hiện tại, hệ thống chủ yếu in ra lỗi bằng e.printStackTrace(). Trong môi trường thực tế, việc này không thân thiện với người dùng và có thể gây rò rỉ thông tin kỹ thuật của hệ thống.

Hiệu năng khi dữ liệu lớn: Các danh sách căn hộ đang được tải toàn bộ vào ApartmentTable. Khi số lượng căn hộ lên đến hàng chục nghìn, ứng dụng sẽ gặp hiện tượng giật lag do tốn bộ nhớ RAM để render giao diện.

7.3. Hướng cải tiến trong tương lai
Áp dụng Dependency Injection (DI): Thay vì để các Service tự khởi tạo Repository (ví dụ: new ApartmentRepository()), nên sử dụng DI để "tiêm" các phụ thuộc vào. Điều này giúp việc viết Unit Test trở nên dễ dàng hơn bằng cách sử dụng các đối tượng giả (Mock objects).

Phân trang dữ liệu (Pagination): Cải tiến ApartmentRepository để hỗ trợ truy vấn theo trang (ví dụ: LIMIT 20 OFFSET 0) để tối ưu tốc độ tải dữ liệu trên giao diện.

Chuyển đổi sang kiến trúc Client-Server: Tách phần logic xử lý ra một REST API (sử dụng Spring Boot) và phần giao diện có thể là React hoặc Flutter. Điều này giúp hệ thống có khả năng mở rộng (Scalability) cực tốt.

Bảo mật nâng cao: Thay vì chỉ băm mật khẩu bằng SHA-256 (có thể bị tấn công bằng bảng cầu vồng), nên sử dụng các thuật toán hiện đại hơn như BCrypt với cơ chế thêm muối (Salt) để bảo vệ tài khoản người dùng tốt hơn.
---

## 8. Kết luận

Dự án được thiết kế bài bản theo mô hình hướng đối tượng (OOP). Việc áp dụng Repository Pattern và Encapsulation giúp hệ thống có tính bảo mật và khả năng bảo trì cao. Logic phân loại tự động tích hợp ngay trong Model đảm bảo tính nhất quán của dữ liệu nghiệp vụ.
