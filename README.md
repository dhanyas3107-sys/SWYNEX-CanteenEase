🍽️ CanteenEase – Smart Canteen Order Management System

📌 Project Overview

CanteenEase is a console-based Java application designed to simplify food ordering and order management in college canteens.

The application allows users to browse food items, place orders, generate bills, track order statuses and manage cancellations through an interactive menu-driven interface.

🎯 Problem Statement

College canteens often handle multiple orders simultaneously. Managing these orders manually can lead to calculation errors, order confusion and delays.

CanteenEase aims to provide a simple digital solution to organize food orders and make the ordering process more efficient.

💡 Proposed Solution

CanteenEase provides a centralized console-based system where users can:

* Browse available food items.
* Place orders containing multiple items.
* Generate unique order IDs.
* Calculate bills automatically.
* Search and track existing orders.
* Update order statuses.
* Cancel eligible orders.
* View order summaries and revenue.

✨ Key Features

1. Food Menu: Displays food items, categories, prices and availability.
2. Order Placement: Allows users to select multiple food items and quantities.
3. Automatic Order ID: Generates unique identification numbers for orders.
4. Bill Generation: Calculates item-wise subtotals and the final bill.
5. Order Management: Displays and searches existing orders.
6. Order Tracking: Supports PLACED, PREPARING, READY and COMPLETED statuses.
7. Order Cancellation: Allows eligible orders to be cancelled.
8. Order Summary: Displays order statistics and revenue information.
9. Input Validation: Handles invalid inputs and incorrect operations.

🛠️ Technologies Used

* Programming Language: Java
* IDE: Visual Studio Code
* Version Control: Git
* Code Hosting: GitHub

📚 Java Concepts Demonstrated

This project demonstrates fundamental Object-Oriented Programming and Java concepts:

Concept	Application
Classes and Objects	Represent food items and orders
Constructors	Initialize objects
Encapsulation	Protect data using private fields
Methods	Perform individual operations
ArrayList	Store food items and orders
HashMap	Manage food item quantities
Enums	Represent order statuses and food categories
Exception Handling	Handle runtime errors and invalid operations
Custom Exceptions	Handle specific order-related errors
BigDecimal	Perform accurate monetary calculations
Input Validation	Prevent invalid user inputs

📂 Project Structure

CanteenEase/
│
├── CanteenEase.java
└── README.md

The application uses multiple classes within a single Java source file to maintain a simple and organized structure.

⚙️ Installation and Execution

Prerequisites

* Java Development Kit (JDK) 24 or compatible version.
* Visual Studio Code or any Java-supported IDE.

Steps to Run

1. Clone the repository

git clone https://github.com/dhanyas3107-sys/CanteenEase.git

2. Navigate to the project directory

cd CanteenEase

3. Compile the Java source code

javac CanteenEase.java

4. Run the application

java CanteenEase

💻 Application Menu

The application provides a menu-driven interface with options to:

1. View Food Menu
2. Place New Order
3. View All Orders
4. Search Order
5. Update Order Status
6. Cancel Order
7. Generate Bill
8. View Order Summary
9. Exit

🔮 Future Enhancements

* Database integration for permanent data storage.
* Graphical user interface.
* Online payment integration.
* Customer registration and authentication.
* Real-time order notifications.
* Separate admin and customer interfaces.

⚠️ Limitations

The current version stores information using Java collections in memory. Therefore, order details are not permanently saved and are reset when the application terminates.

👩‍💻 Developer

Dhanya Sri Mohandass

Second-Year Computer Science and Engineering Student

Developed as part of a Java internship assignment.

📄 License

This project is developed for educational and learning purposes.