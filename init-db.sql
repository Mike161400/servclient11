-- init-db.sql
SET client_encoding = 'UTF8';
DROP TABLE IF EXISTS completed_services CASCADE;
DROP TABLE IF EXISTS required_services CASCADE;
DROP TABLE IF EXISTS order_parts CASCADE;
DROP TABLE IF EXISTS service_orders CASCADE;
DROP TABLE IF EXISTS parts CASCADE;
DROP TABLE IF EXISTS vehicles CASCADE;
DROP TABLE IF EXISTS mechanics CASCADE;
DROP TABLE IF EXISTS customers CASCADE;

-- Таблица customers
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица vehicles
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    license_plate VARCHAR(15) NOT NULL UNIQUE,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL CHECK (year >= 1900),
    vin VARCHAR(17) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Таблица mechanics
CREATE TABLE mechanics (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    specialization VARCHAR(100),
    experience_years INTEGER DEFAULT 0 CHECK (experience_years >= 0),
    hourly_rate DECIMAL(10,2) DEFAULT 0.00 CHECK (hourly_rate >= 0),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица parts
CREATE TABLE parts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    part_number VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    quantity_in_stock INTEGER NOT NULL DEFAULT 0 CHECK (quantity_in_stock >= 0),
    min_quantity INTEGER DEFAULT 5 CHECK (min_quantity >= 0),
    supplier VARCHAR(100),
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица service_orders
CREATE TABLE service_orders (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    mechanic_id BIGINT NOT NULL,
    creation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    completion_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    labor_cost DECIMAL(10,2) DEFAULT 0.00 CHECK (labor_cost >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    FOREIGN KEY (mechanic_id) REFERENCES mechanics(id) ON DELETE RESTRICT
);

-- Таблица order_parts (для связи многие-ко-многим)
CREATE TABLE order_parts (
    order_id BIGINT NOT NULL,
    part_id BIGINT NOT NULL,
    quantity INTEGER DEFAULT 1 CHECK (quantity > 0),
    PRIMARY KEY (order_id, part_id),
    FOREIGN KEY (order_id) REFERENCES service_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (part_id) REFERENCES parts(id) ON DELETE RESTRICT
);

-- Таблица required_services
CREATE TABLE required_services (
    order_id BIGINT NOT NULL,
    service TEXT NOT NULL,
    PRIMARY KEY (order_id, service),
    FOREIGN KEY (order_id) REFERENCES service_orders(id) ON DELETE CASCADE
);

-- Таблица completed_services
CREATE TABLE completed_services (
    order_id BIGINT NOT NULL,
    service TEXT NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id, service),
    FOREIGN KEY (order_id) REFERENCES service_orders(id) ON DELETE CASCADE
);

-- Индексы для ускорения поиска
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_phone ON customers(phone_number);
CREATE INDEX idx_vehicles_license ON vehicles(license_plate);
CREATE INDEX idx_vehicles_vin ON vehicles(vin);
CREATE INDEX idx_vehicles_customer ON vehicles(customer_id);
CREATE INDEX idx_mechanics_email ON mechanics(email);
CREATE INDEX idx_mechanics_active ON mechanics(is_active);
CREATE INDEX idx_parts_number ON parts(part_number);
CREATE INDEX idx_parts_category ON parts(category);
CREATE INDEX idx_service_orders_status ON service_orders(status);
CREATE INDEX idx_service_orders_mechanic ON service_orders(mechanic_id);
CREATE INDEX idx_service_orders_vehicle ON service_orders(vehicle_id);
CREATE INDEX idx_service_orders_dates ON service_orders(creation_date, completion_date);

-- Тестовые данные
INSERT INTO customers (name, phone_number, email, address) VALUES
('Иванов Иван Иванович', '+79161234567', 'ivanov@mail.com', 'Москва, ул. Ленина, 10'),
('Петров Петр Петрович', '+79162345678', 'petrov@mail.com', 'Санкт-Петербург, Невский пр., 25'),
('Сидорова Анна Сергеевна', '+79163456789', 'sidorova@mail.com', 'Казань, ул. Баумана, 15')
ON CONFLICT (email) DO NOTHING;

INSERT INTO vehicles (license_plate, brand, model, year, vin, customer_id) VALUES
('А123БВ777', 'Toyota', 'Camry', 2020, 'JTDKBRFU904567890', 1),
('В456ГН777', 'Honda', 'Civic', 2019, '19XFC2F59GE123456', 2),
('Е789КМ777', 'BMW', 'X5', 2021, 'WBANF51000C987654', 3),
('О321РС777', 'Lada', 'Vesta', 2022, 'XTA210990Y1234567', 1),
('Т654УХ777', 'Kia', 'Rio', 2020, 'KNAFU8112L5678901', 2)
ON CONFLICT (license_plate) DO NOTHING;

INSERT INTO mechanics (name, phone_number, email, specialization, experience_years, hourly_rate) VALUES
('Смирнов Алексей Викторович', '+79164567890', 'smirnov@autoservice.com', 'Двигатель', 8, 1500.00),
('Кузнецов Дмитрий Сергеевич', '+79165678901', 'kuznetsov@autoservice.com', 'Электрика', 5, 1200.00),
('Попова Ольга Игоревна', '+79166789012', 'popova@autoservice.com', 'Кузовной ремонт', 10, 1800.00),
('Васильев Михаил Петрович', '+79167890123', 'vasiliev@autoservice.com', 'Диагностика', 3, 1000.00),
('Николаев Сергей Андреевич', '+79168901234', 'nikolaev@autoservice.com', 'Трансмиссия', 12, 2000.00)
ON CONFLICT (email) DO NOTHING;

INSERT INTO parts (name, part_number, description, price, quantity_in_stock, category, supplier) VALUES
('Масляный фильтр', 'FILTER-OIL-001', 'Фильтр масляный синтетический', 1200.00, 50, 'Фильтры', 'MANN Filter'),
('Воздушный фильтр', 'FILTER-AIR-002', 'Фильтр воздушный салонный', 800.00, 30, 'Фильтры', 'Bosch'),
('Топливный фильтр', 'FILTER-FUEL-003', 'Фильтр топливный тонкой очистки', 1500.00, 20, 'Фильтры', 'Mahle'),
('Тормозные колодки', 'BRAKE-PADS-101', 'Колодки тормозные передние керамические', 3500.00, 15, 'Тормоза', 'Brembo'),
('Тормозные диски', 'BRAKE-DISCS-102', 'Диски тормозные вентилируемые', 6500.00, 8, 'Тормоза', 'ATE'),
('Аккумулятор', 'BATTERY-201', 'Аккумулятор 60Ah AGM', 7000.00, 10, 'Электрика', 'Varta'),
('Свечи зажигания', 'SPARK-PLUG-301', 'Свечи иридиевые комплект 4шт', 2500.00, 25, 'Двигатель', 'NGK'),
('Масло моторное', 'OIL-401', 'Масло синтетическое 5W-40 5л', 3500.00, 40, 'Жидкости', 'Castrol'),
('Антифриз', 'COOLANT-402', 'Антифриз концентрат G12 5л', 1800.00, 35, 'Жидкости', 'Febi'),
('Шина летняя', 'TIRE-501', 'Шина 205/55 R16 91H', 4500.00, 20, 'Шины', 'Michelin')
ON CONFLICT (part_number) DO NOTHING;

INSERT INTO service_orders (vehicle_id, mechanic_id, creation_date, status, labor_cost) VALUES
(1, 1, '2024-01-15', 'COMPLETED', 3000.00),
(2, 2, '2024-01-20', 'IN_PROGRESS', 2400.00),
(3, 3, '2024-01-25', 'CREATED', 3600.00),
(4, 1, '2024-02-01', 'COMPLETED', 1500.00),
(5, 4, '2024-02-05', 'IN_PROGRESS', 2000.00)
ON CONFLICT DO NOTHING;

INSERT INTO order_parts (order_id, part_id, quantity) VALUES
(1, 1, 1),
(1, 8, 1),
(2, 2, 1),
(2, 6, 1),
(3, 3, 1),
(3, 7, 1),
(4, 4, 2),
(5, 5, 2),
(5, 10, 4)
ON CONFLICT DO NOTHING;

INSERT INTO required_services (order_id, service) VALUES
(1, 'Замена масла и фильтра'),
(1, 'Диагностика двигателя'),
(2, 'Замена воздушного фильтра'),
(2, 'Диагностика электрики'),
(3, 'Замена топливного фильтра'),
(3, 'Замена свечей'),
(4, 'Замена тормозных колодок'),
(5, 'Замена тормозных дисков'),
(5, 'Замена шин')
ON CONFLICT DO NOTHING;

INSERT INTO completed_services (order_id, service) VALUES
(1, 'Замена масла и фильтра'),
(1, 'Диагностика двигателя'),
(4, 'Замена тормозных колодок')
ON CONFLICT DO NOTHING;

-- Создаем функцию для обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Триггеры для автоматического обновления updated_at
CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vehicles_updated_at BEFORE UPDATE ON vehicles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_mechanics_updated_at BEFORE UPDATE ON mechanics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_parts_updated_at BEFORE UPDATE ON parts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_service_orders_updated_at BEFORE UPDATE ON service_orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();