-- database schema for the project

CREATE DATABASE IF NOT EXISTS vehicle_service_db;
USE vehicle_service_db;

-- making sure there are no pior tables 
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS receipts;
DROP TABLE IF EXISTS fuel_sales;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS service_stock_requirements;
DROP TABLE IF EXISTS stock_items;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS branches;
DROP TABLE IF EXISTS users;

-- users connects with branch table for branch id
-- and the roles are pre defined

CREATE TABLE users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100)  NOT NULL,
    email      VARCHAR(150)  NOT NULL UNIQUE,
    password   VARCHAR(255)  NOT NULL,
    phone      VARCHAR(20)   NOT NULL,
    role       ENUM('CUSTOMER','CASHIER','MECHANIC','INVENTORY_SERVICE_MANAGER','BRANCH_MANAGER','MAIN_ADMIN') NOT NULL,
    branch_id  BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- branch connects with users for manager id
CREATE TABLE branches (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    city       VARCHAR(100) NOT NULL,
    address    VARCHAR(255) NOT NULL,
    status     ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    manager_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_branches_manager FOREIGN KEY (manager_id) REFERENCES users(id)
);

-- connecting users and branch
ALTER TABLE users
    ADD CONSTRAINT fk_users_branch FOREIGN KEY (branch_id) REFERENCES branches(id);


-- connects with users for customer (vehicle owner) id
CREATE TABLE vehicles (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id  BIGINT NOT NULL,
    plate_number VARCHAR(20) NOT NULL,
    make         VARCHAR(50) NOT NULL,
    model        VARCHAR(50) NOT NULL,
    year         INT NOT NULL,
    CONSTRAINT fk_vehicles_customer FOREIGN KEY (customer_id) REFERENCES users(id)
);


CREATE TABLE services (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    price       DECIMAL(10,2) NOT NULL
);

-- connects with branches for branch id
-- and types only have 2 catagories
CREATE TABLE stock_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id           BIGINT NOT NULL,
    name                VARCHAR(100) NOT NULL,
    type                ENUM('FUEL','EQUIPMENT') NOT NULL,
    quantity            DECIMAL(10,2) NOT NULL,
    unit                VARCHAR(20) NOT NULL,
    low_stock_threshold DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_stock_items_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);


-- connects with servuces and stock_items
CREATE TABLE service_stock_requirements (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id         BIGINT NOT NULL,
    stock_item_id      BIGINT NOT NULL,
    quantity_required  DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_ssr_service    FOREIGN KEY (service_id) REFERENCES services(id),
    CONSTRAINT fk_ssr_stock_item FOREIGN KEY (stock_item_id) REFERENCES stock_items(id)
);


-- connects with 5 tables
-- users, vehicles, branches, services, users
-- and there are 5 types of booking status
CREATE TABLE bookings (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id  BIGINT NOT NULL,
    vehicle_id   BIGINT NOT NULL,
    branch_id    BIGINT NOT NULL,
    service_id   BIGINT NOT NULL,
    mechanic_id  BIGINT NULL,
    status       ENUM('PENDING','ACCEPTED','ASSIGNED','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'PENDING',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME NULL,
    CONSTRAINT fk_bookings_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_vehicle  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_bookings_branch   FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_bookings_service  FOREIGN KEY (service_id) REFERENCES services(id),
    CONSTRAINT fk_bookings_mechanic FOREIGN KEY (mechanic_id) REFERENCES users(id)
);


-- connects with users and branches 
CREATE TABLE fuel_sales (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    cashier_id BIGINT NOT NULL,
    branch_id  BIGINT NOT NULL,
    fuel_type  VARCHAR(100) NOT NULL,
    quantity   DECIMAL(10,2) NOT NULL,
    amount     DECIMAL(10,2) NOT NULL,
    sale_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fuel_sales_cashier FOREIGN KEY (cashier_id) REFERENCES users(id),
    CONSTRAINT fk_fuel_sales_branch  FOREIGN KEY (branch_id) REFERENCES branches(id)
);


-- connects with fuel_sales and bookings
CREATE TABLE receipts (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    fuel_sale_id BIGINT NULL,
    booking_id   BIGINT NULL,
    amount       DECIMAL(10,2) NOT NULL,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_receipts_fuel_sale FOREIGN KEY (fuel_sale_id) REFERENCES fuel_sales(id),
    CONSTRAINT fk_receipts_booking   FOREIGN KEY (booking_id) REFERENCES bookings(id)
);


-- connects with bookings and users
-- there is a check for rating number
CREATE TABLE feedback (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id  BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    rating      INT NOT NULL,
    comment     VARCHAR(1000) NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_booking  FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_feedback_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT chk_feedback_rating CHECK (rating BETWEEN 1 AND 5)
);

-- Main Admin has no self registration
INSERT INTO users (id, name, email, password, phone, role, branch_id, created_at) VALUES
(1, 'Main Admin', 'admin@autocare.lk', '$2b$10$yBSi8Q0XQro7MSq/qvig3.4NqPlfWE1JXCvlNoftu/FzfEsJ1w1dS', '0110000000', 'MAIN_ADMIN', NULL, NOW());
-- password: admin123 (used PasswordTest in the util directory) 


