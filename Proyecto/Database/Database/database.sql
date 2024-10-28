-- Create the database
CREATE DATABASE IF NOT EXISTS inventory_system;

-- Use the database
USE inventory_system;

-- Create the products table
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    code VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    subvariant VARCHAR(50),
    color VARCHAR(50) NOT NULL,
    costPerUnit DECIMAL(10, 2),
    costPerGram DECIMAL(10, 2),
    totalCost DECIMAL(10, 2)
);

-- Create the sales table
CREATE TABLE IF NOT EXISTS sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoiceNumber VARCHAR(50) NOT NULL,
    customerName VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    gramsUsed DECIMAL(10, 2),
    price DECIMAL(10, 2),
    date DATE,
    type VARCHAR(50),
    productCode VARCHAR(50)
);

-- Create the code_master table with constraints
CREATE TABLE IF NOT EXISTS code_master (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE CHECK (code <> ''),
    type VARCHAR(50) NOT NULL CHECK (type <> ''),
    subvariant VARCHAR(50) NULL,
    color VARCHAR(50) NOT NULL CHECK (color <> '')
);

-- Sample Data for products and sales
INSERT INTO products (date, code, type, subvariant, color, costPerUnit, costPerGram, totalCost)
VALUES 
('2024-10-23', 'MP0001', 'PLA', 'Standard', 'Red', 25.00, 0.025, 25.00),
('2024-10-24', 'MP0002', 'ABS', 'Flexible', 'Blue', 30.00, 0.030, 30.00);

INSERT INTO sales (invoiceNumber, customerName, description, gramsUsed, price, date, type, productCode)
VALUES 
('F0001', 'Juan', 'Dragon flexible', 30.51, 17.00, '2024-10-23', 'PLA', 'MP0001'),
('F0002', 'Maria', 'Flexible holder', 50.00, 25.00, '2024-10-24', 'ABS', 'MP0002');

-- Sample Data for code_master table
INSERT INTO code_master (code, type, subvariant, color) VALUES
('MP0001', 'PLA', 'Standard', 'Red'),
('MP0002', 'ABS', 'Flexible', 'Blue'),
('MP0003', 'TPU', 'Impact-resistant', 'Green');
