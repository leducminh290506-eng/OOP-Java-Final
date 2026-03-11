# Project 8: Real Estate Apartment Listing and Filter System

## 8.1 Problem Description
A real estate company maintains a growing list of available apartments for rent, including price, number of bedrooms, location, and amenities. Currently, listings are kept in spreadsheets and messaging apps, causing inconsistent records, difficulty performing searches, and slow response when matching apartments to customer criteria. There is also no login system, making it difficult to restrict access to listing modifications or track who performed changes.

The upgraded system must operate as a complete apartment listing and filtering platform and provide:
* Secure login with role-based access
* Apartment listing management
* Advanced filtering based on tenant preferences
* Favorites/bookmarks for frequently viewed apartments
* Search, sorting, and dashboard analytics
* Listing notes and agent comments
* Persistent storage using sequential files
* A full Java Swing GUI for all workflows



## 8.2 Functional Requirements

### FR-0: Login and Authentication
* **FR-0.1:** The system shall provide a login screen requiring username and password.
* **FR-0.2:** The system shall validate credentials against a stored user file.
* **FR-0.3:** The system shall support multiple roles (Admin, Agent).
* **FR-0.4:** Admin users shall be allowed to delete listings and view logs.
* **FR-0.5:** The system shall log all login and logout events.

### FR-1: Apartment Listing Management
* **FR-1.1:** The system shall allow creating, updating, and deleting apartment listings.
* **FR-1.2:** Each listing shall store price, number of bedrooms, location, size, and available amenities.
* **FR-1.3:** The system shall prevent duplicate listing IDs.
* **FR-1.4:** The system shall allow searching listings by listing ID or address.

### FR-2: Filtering and Matching
* **FR-2.1:** The system shall filter apartments based on:
    * Maximum price
    * Minimum bedrooms
    * Location
    * Amenities
* **FR-2.2:** The system shall support compound Boolean expressions for multi-criteria filtering.
* **FR-2.3:** The system shall update filter results dynamically as filter inputs change.
* **FR-2.4:** The system shall display matched results in a sortable list.

### FR-3: Favorites and Agent Notes
* **FR-3.1:** The system shall allow agents to mark apartments as “Favorite” for quick access.
* **FR-3.2:** The system shall allow adding internal notes to each listing (e.g., viewing history, client remarks).
* **FR-3.3:** The system shall display all favorited listings in a dedicated view.
* **FR-3.4:** The system shall allow editing and removing notes for each listing.

### FR-4: Additional Core Features
* **FR-4.1:** The system shall allow exporting filtered listings to CSV.
* **FR-4.2:** The system shall generate a listing detail summary (price breakdown, amenity list).
* **FR-4.3:** The system shall classify apartments into categories (Luxury, Standard, Budget) based on price and size.
* **FR-4.4:** The system shall log all listing modifications in an audit file.

### FR-5: Dashboard and Search
* **FR-5.1:** The system shall display all listings in a sortable table (price, bedrooms, location).
* **FR-5.2:** The system shall filter listings by category (Luxury, Standard, Budget).
* **FR-5.3:** The system shall provide keyword search across address, amenities, and location.
* **FR-5.4:** The system shall display summary analytics such as:
    * Average price
    * Number of listings per location
    * Number of favorites

### FR-6: Persistence and GUI Requirements
* **FR-6.1:** The system shall store apartment listings and notes using Sequential File I/O.
* **FR-6.2:** The system shall store user credentials in an encoded file.
* **FR-6.3:** The GUI shall include tabs for Listings, Filters, Favorites, and Dashboard.
* **FR-6.4:** The GUI shall update filter results in real time.
* **FR-6.5:** The GUI shall display validation and error messages using JOptionPane.
