# AI Usage Report

## Real Estate Apartment Listing and Filter System — Final Java OOP Project

**Course:** Object-Oriented Programming (Java)  
**Semester:** Spring 2026  
**Date:** May 11, 2026

---

## 1. Overview

Our team utilized AI assistants (primarily **ChatGPT** and **Claude**) as pair-programming tools throughout the development of this project. We estimate that AI contributed to approximately **50% of the final codebase**, primarily in areas of UI/UX polishing, boilerplate code generation, and debugging complex edge cases.

It is important to emphasize that AI was **not** used as a "copy-paste" solution. Instead, we treated it as an interactive tutor and code reviewer — posing specific questions, evaluating its suggestions critically, and integrating only the portions we fully understood. All architectural decisions, database design, and core business logic were conceived, planned, and implemented by the team.

The following sections provide a transparent breakdown of what was hand-coded by the team versus what was AI-assisted.

---

## 2. What Our Team Hand-Coded (~50% Human Effort)

These components represent the foundational work that required direct application of course knowledge (OOP principles, JDBC, Swing basics, and relational database design):

### 2.1 Database Schema Design

- Designed the full relational schema (`schema.sql`) including tables: `users`, `apartments`, `amenities`, `apartment_amenities`, `favorites`, `notes`, `lease_contracts`, `login_logs`, and `audit_logs`.
- Defined all primary keys, foreign key constraints, and junction tables (e.g., `apartment_amenities` for the many-to-many relationship).
- Wrote seed data (`seed.sql`) for testing with realistic Vietnamese apartment listings.

### 2.2 Project Architecture (MVC Pattern)

- Established the full package structure following the **Model–Repository–Service–UI** layering pattern:
  - `com.oop.project.model` — POJOs
  - `com.oop.project.repository` — Data access layer
  - `com.oop.project.service` — Business logic layer
  - `com.oop.project.ui` — Presentation layer (Swing)
- Defined the `IRepository<T, ID>` generic interface to enforce consistent CRUD contracts across all repositories.

### 2.3 Core Model Classes (POJOs)

Hand-wrote all domain objects with proper encapsulation:

| Class | Fields | Notes |
|---|---|---|
| `Apartment` | id, listingCode, address, location, price, bedrooms, area, category, description, status | Includes `classifyByPriceAndArea()` logic |
| `User` | id, username, passwordHash, role | Enum-based `Role` (ADMIN, AGENT) |
| `Note` | id, apartmentId, userId, noteText | Simple note model |
| `LeaseContract` | id, apartmentId, customerName, startDate, endDate, monthlyRent, notes | Date-based lease tracking |

### 2.4 Basic CRUD Operations (JDBC)

- Wrote all repository classes using raw JDBC (`PreparedStatement`, `ResultSet`, `Connection`):
  - `ApartmentRepository` — full CRUD + search + favorites toggle
  - `UserRepository` — credential lookup with SHA-256 hash comparison
  - `NoteRepository` — save, update, delete, findByApartmentId
  - `ContractRepository` — lease management + active rental check
- Implemented the Singleton pattern for `DatabaseConnection` to manage connection pooling.

### 2.5 Core Business Logic

- `AuthService` — Login validation with SHA-256 hashing, logout event logging to `login_logs` table.
- `ApartmentService` — Orchestrates apartment CRUD, amenity queries, rental status checks.
- Duplicate listing prevention logic (`SELECT COUNT(*) WHERE listing_code = ?` before insert).
- Input validation rules (price > 0, area > 0, bedrooms > 0) in the UI layer.

### 2.6 Basic Swing Layouts

- Set up the main application structure: `MainFrame` with `CardLayout` for tab-based navigation.
- Created basic `JPanel` layouts for Listings, Filters, Favorites, Contracts, and Dashboard.
- Wired fundamental event listeners (`ActionListener`, `MouseListener`) for button clicks and table selection.
- Implemented the `LoginDialog` for authentication flow.

---

## 3. What AI Assisted With (~50% AI Effort)

These components were developed with significant AI guidance. In each case, we describe *what* was generated, *why* we needed help, and *how* we validated the output.

### 3.1 UI/UX Polishing & Modern Design

**What:** Integration of the FlatLaf look-and-feel library, custom `paintComponent()` overrides for the Dashboard's bar chart (`ColoredBarChart`) and rounded card panels (`RoundedPanel`), sidebar hover effects with active-state highlighting, and gradient rendering.

**Why:** Java Swing's default rendering is visually outdated. Custom painting with `Graphics2D`, `RoundRectangle2D`, and `GradientPaint` requires knowledge not covered in our coursework.

**How we validated:** We manually adjusted all color values, layout dimensions, and animation timings. We understood the rendering pipeline (`paintComponent` → `Graphics2D` → anti-aliasing) before integrating the code.

**Files affected:**
- `DashboardPanel.java` — KPI cards, bar chart rendering
- `MainFrame.java` — Sidebar styling, hover effects, active button state
- `ApartmentDetailDialog.java` — Styled detail view with section headers

### 3.2 Complex Multi-Criteria Filtering

**What:** The `executeFilter()` method in both `ListingPanel` and `FilterPanel`, which combines multiple filter conditions (keyword, location, price range, category, bedrooms, amenities) using AND logic in a single loop.

**Why:** Combining 6+ Boolean conditions with null-safety checks and type conversions in a single pass was error-prone. AI helped structure the logic cleanly.

**How we validated:** We tested every filter combination manually — keyword only, price range only, keyword + amenities, all filters active, no filters — and verified the results against direct SQL queries.

**Files affected:**
- `ListingPanel.java` — `executeFilter()`, `showAdvancedSearchDialog()`
- `FilterPanel.java` — `executeFilter()` with live DocumentListener

### 3.3 Vietnamese Text Normalization (Diacritic Removal)

**What:** The `normalize()` utility method that uses `java.text.Normalizer` with NFD decomposition and a regex pattern (`\\p{InCombiningDiacriticalMarks}+`) to strip Vietnamese diacritical marks for accent-insensitive searching.

**Why:** Vietnamese text search requires matching "Ha Noi" to "Hà Nội" and "HCM" to "Hồ Chí Minh". The `Normalizer` API and Unicode regex categories are not intuitive and required expert guidance.

**How we validated:** Tested with all 63 Vietnamese provinces/cities in the seed data to ensure correct matching.

**Code example:**
```java
private String normalize(String s) {
    String r = Normalizer.normalize(s, Normalizer.Form.NFD);
    r = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
               .matcher(r).replaceAll("").toLowerCase();
    return r.replace("tp. ", "").replace("hcm", "ho chi minh").trim();
}
```

### 3.4 File I/O Edge Cases (UTF-8 BOM for CSV Export)

**What:** The `ExportService` was rewritten to use `OutputStreamWriter` with explicit `StandardCharsets.UTF_8` and a BOM prefix (`\uFEFF`) so that Microsoft Excel correctly renders Vietnamese characters when opening the exported CSV file.

**Why:** Our initial implementation using `FileWriter` produced garbled Vietnamese text (mojibake) in Excel. The BOM (Byte Order Mark) solution is a well-known workaround but was not something we knew about.

**How we validated:** Exported CSV files and opened them in both Excel and Notepad++ to confirm correct rendering of characters like "Hà Nội", "Đà Nẵng", "Thừa Thiên Huế".

### 3.5 Debugging & Refactoring

AI was used extensively for:

- **NullPointerException diagnosis:** Tracing null values through the JDBC → Repository → Service → UI chain, especially when database columns returned `NULL`.
- **FlatLaf compatibility fixes:** Discovering that FlatLaf overrides `setBackground()` on buttons, requiring `putClientProperty("JButton.buttonType", "none")` to restore manual color control.
- **Import resolution:** Resolving circular dependencies and missing imports when refactoring packages (e.g., `ApartmentDetailDialog` existing in both `components` and `panels` packages).
- **SQL debugging:** Fixing JOIN queries in `SystemLogPanel` and `ContractRepository` where column name mismatches caused `SQLException`.

---

## 4. Summary: Code Ownership Breakdown

| Component | Estimated Human % | Estimated AI % |
|---|---|---|
| Database Schema & SQL Scripts | **90%** | 10% (syntax help) |
| Model Classes (POJOs) | **95%** | 5% (boilerplate generation) |
| Repository Layer (JDBC) | **70%** | 30% (query optimization) |
| Service Layer (Business Logic) | **75%** | 25% (edge case handling) |
| UI — Basic Layouts & Wiring | **65%** | 35% (event handling patterns) |
| UI — Advanced Rendering & Styling | **20%** | **80%** (custom painting, FlatLaf) |
| Filtering & Search Logic | **40%** | **60%** (regex, normalization) |
| Export & File I/O | **30%** | **70%** (encoding, BOM) |
| Debugging & Integration | **50%** | 50% (error diagnosis) |
| **Overall Weighted Average** | **~50%** | **~50%** |

---

## 5. Learning Outcomes

Working with AI as a pair-programming partner provided several valuable learning experiences:

1. **Advanced Swing Techniques:** We learned how to override `paintComponent()` for custom rendering, use `Graphics2D` for anti-aliased drawing, and implement `GradientPaint` for modern UI effects — techniques that would have taken significantly longer to learn from Swing's aging documentation alone.

2. **Better Code Architecture:** AI suggestions consistently pushed us toward cleaner separation of concerns. For example, separating the `ExportService` from the UI layer, and using the Repository pattern instead of embedding SQL directly in Swing panels.

3. **Debugging Methodology:** Rather than randomly adding `System.out.println()` statements, AI taught us to trace exceptions systematically through the call stack and identify root causes in the data flow.

4. **Critical Evaluation:** Not all AI-generated code was correct on the first attempt. We learned to critically evaluate suggestions — for instance, AI initially suggested using `FileWriter` for CSV export, which we later had to fix with the BOM approach. This iterative process strengthened our understanding of character encoding.

5. **Modern Development Workflow:** Using AI as a pair-programmer mirrors real-world software engineering practices, where developers routinely use tools like GitHub Copilot. This experience prepared us for professional workflows beyond the classroom.

---

## 6. Ethical Statement

We affirm that:
- All AI-generated code was **reviewed, understood, and tested** by team members before integration.
- We can **explain every line of code** in our project during the defense presentation.
- AI was used as a **learning accelerator**, not a replacement for understanding OOP concepts.
- This report is an **honest and transparent** account of our AI usage.

---

*This report was prepared in compliance with the course's academic integrity policy regarding AI tool usage in programming assignments.*
