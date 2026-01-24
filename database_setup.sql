-- Cryptocurrency Trading Demo Database Script
-- MySQL Database Setup

-- Create Database
CREATE DATABASE IF NOT EXISTS crypto_trading_db;
USE crypto_trading_db;

-- Create Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 1000.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Wallet Items Table
CREATE TABLE IF NOT EXISTS wallet_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    quantity DECIMAL(20, 8) NOT NULL,
    average_price DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_symbol (user_id, symbol)
);

-- Create Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    type VARCHAR(10) NOT NULL,
    side VARCHAR(10) NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    quantity DECIMAL(20, 8) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    stop_loss DECIMAL(15, 2),
    take_profit DECIMAL(15, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);

-- Create Trades Table
CREATE TABLE IF NOT EXISTS trades (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    side VARCHAR(10) NOT NULL,
    executed_price DECIMAL(15, 2) NOT NULL,
    quantity DECIMAL(20, 8) NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_symbol (symbol)
);

-- Create News Table
CREATE TABLE IF NOT EXISTS news (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    source VARCHAR(100),
    published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_published (published_at)
);

-- Create Predictions Table
CREATE TABLE IF NOT EXISTS predictions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    symbol VARCHAR(10) NOT NULL,
    `signal` VARCHAR(10) NOT NULL,
    confidence DECIMAL(3, 2) NOT NULL,
    target_price DECIMAL(15, 2) NOT NULL,
    analysis LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_symbol (symbol)
);

-- Create Reclamations Table
CREATE TABLE IF NOT EXISTS reclamations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);

-- Insert sample data
INSERT INTO news (title, content, source) VALUES
('Bitcoin Reaches New All-Time High', 'Bitcoin has surpassed previous records as institutional adoption increases worldwide. Analysts suggest continued bullish momentum.', 'CryptoNews Daily'),
('Ethereum 2.0 Updates Show Promise', 'Latest Ethereum network upgrades demonstrate significant improvements in scalability and energy efficiency.', 'Blockchain Times'),
('SEC Approves First Spot Bitcoin ETF', 'In a landmark decision, the SEC has approved the first spot Bitcoin ETF, making crypto more accessible to retail investors.', 'Finance Weekly'),
('DeFi Platform Reaches $100B TVL', 'Major decentralized finance protocol announces reaching $100 billion in total value locked, marking significant growth.', 'DeFi Pulse'),
('Cryptocurrency Market Sees Strong Recovery', 'Following recent market volatility, cryptocurrencies show signs of recovery with major coins gaining momentum.', 'Market Watch');

-- Insert sample predictions
INSERT INTO predictions (symbol, `signal`, confidence, target_price, analysis) VALUES
('BTC', 'BUY', 0.78, 48000, 'Strong upward trend detected. Market momentum favors buying.'),
('ETH', 'HOLD', 0.65, 2650, 'Market consolidation phase. Recommend holding current positions.'),
('BNB', 'SELL', 0.72, 320, 'Downward pressure observed. Consider reducing positions.');

-- Display created tables
SHOW TABLES;
DESCRIBE users;
