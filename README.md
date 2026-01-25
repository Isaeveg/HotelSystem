# System Zarządzania Hotelami - Dokumentacja Instalacji i Konfiguracji

Niniejszy dokument zawiera szczegółową instrukcję instalacji, konfiguracji oraz uruchomienia Systemu Zarządzania Hotelami. System oparty jest na architekturze klient-serwer, wykorzystując język Java (JavaFX) oraz relacyjną bazę danych PostgreSQL.

## 1. Wymagania Systemowe

Do poprawnego działania systemu wymagane jest zainstalowanie następującego oprogramowania:

*   **Java Development Kit (JDK) 21** lub nowsza
*   **Apache Maven** (narzędzie do budowania projektu)
*   **PostgreSQL** (system zarządzania bazą danych)

## 2. Inicjalizacja Bazy Danych

Przed uruchomieniem aplikacji konieczne jest przygotowanie środowiska bazy danych.

### Krok 2.1: Utworzenie Bazy Danych
Należy utworzyć nową bazę danych w systemie PostgreSQL, na przykład o nazwie `hotel_db`.

### Krok 2.2: Wykonanie Skryptu SQL
W utworzonej bazie danych należy wykonać poniższy skrypt SQL, który odpowiada za strukturę tabel oraz dane inicjalne. Można to zrobić za pomocą narzędzia pgAdmin lub konsoli `psql`.

```sql
DROP TABLE IF EXISTS booking_amenities CASCADE;
DROP TABLE IF EXISTS amenities CASCADE;
DROP TABLE IF EXISTS favorite_rooms CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS rooms CASCADE;
DROP TABLE IF EXISTS hotels CASCADE;
DROP TABLE IF EXISTS users CASCADE;

DROP TYPE IF EXISTS booking_status;
DROP TYPE IF EXISTS user_role;
DROP TYPE IF EXISTS room_status;

CREATE TYPE room_status AS ENUM ('FREE', 'OCCUPIED', 'CLEANING');
CREATE TYPE user_role AS ENUM ('ADMIN', 'CLIENT');
CREATE TYPE booking_status AS ENUM ('CONFIRMED', 'CANCELLED', 'PENDING');

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(200) NOT NULL,
    role user_role NOT NULL DEFAULT 'CLIENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE clients (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20)
);

CREATE TABLE hotels (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    address VARCHAR(200) NOT NULL,
    rating INT DEFAULT 3 CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE rooms (
    id SERIAL PRIMARY KEY,
    hotel_id INT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    room_number VARCHAR(10) NOT NULL,
    type VARCHAR(50) NOT NULL,
    capacity INT NOT NULL DEFAULT 2 CHECK (capacity > 0),
    price_per_night DECIMAL(10, 2) NOT NULL CHECK (price_per_night >= 0),
    floor INT NOT NULL,
    status room_status NOT NULL DEFAULT 'FREE',
    description TEXT,
    UNIQUE (hotel_id, room_number)
);

CREATE TABLE bookings (
    id SERIAL PRIMARY KEY,
    client_id INT REFERENCES clients(id) ON DELETE CASCADE,
    room_id INT REFERENCES rooms(id) ON DELETE CASCADE,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    status booking_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_dates CHECK (check_out_date > check_in_date)
);

CREATE TABLE favorite_rooms (
    client_id INT NOT NULL,
    room_id INT NOT NULL,
    PRIMARY KEY (client_id, room_id),
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

CREATE TABLE amenities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 CHECK (price >= 0)
);

CREATE TABLE booking_amenities (
    booking_id INT REFERENCES bookings(id) ON DELETE CASCADE,
    amenity_id INT REFERENCES amenities(id) ON DELETE CASCADE,
    PRIMARY KEY (booking_id, amenity_id)
);

CREATE OR REPLACE PROCEDURE register_client(
    p_email VARCHAR, 
    p_password VARCHAR, 
    p_first_name VARCHAR, 
    p_last_name VARCHAR, 
    p_phone VARCHAR
)
LANGUAGE plpgsql
AS $$
DECLARE
    new_user_id INT;
BEGIN
    INSERT INTO users (email, password, role)
    VALUES (p_email, p_password, 'CLIENT')
    RETURNING id INTO new_user_id;
    INSERT INTO clients (user_id, first_name, last_name, phone)
    VALUES (new_user_id, p_first_name, p_last_name, p_phone);
    
END;
$$;

CREATE OR REPLACE FUNCTION get_monthly_income(p_month INT, p_year INT)
RETURNS DECIMAL(10, 2)
LANGUAGE plpgsql
AS $$
DECLARE
    income DECIMAL(10, 2);
BEGIN
    SELECT COALESCE(SUM(total_price), 0) 
    INTO income
    FROM bookings 
    WHERE EXTRACT(MONTH FROM created_at) = p_month 
      AND EXTRACT(YEAR FROM created_at) = p_year
      AND status != 'CANCELLED';
      
    RETURN income;
END;
$$;

DELETE FROM booking_amenities;
DELETE FROM amenities;
DELETE FROM favorite_rooms;
DELETE FROM bookings;
DELETE FROM rooms;
DELETE FROM hotels;
DELETE FROM clients;
DELETE FROM users;

ALTER SEQUENCE booking_amenities_booking_id_seq RESTART WITH 1;
ALTER SEQUENCE booking_amenities_amenity_id_seq RESTART WITH 1;
ALTER SEQUENCE amenities_id_seq RESTART WITH 1;
ALTER SEQUENCE bookings_id_seq RESTART WITH 1;
ALTER SEQUENCE rooms_id_seq RESTART WITH 1;
ALTER SEQUENCE hotels_id_seq RESTART WITH 1;
ALTER SEQUENCE clients_id_seq RESTART WITH 1;
ALTER SEQUENCE users_id_seq RESTART WITH 1;

INSERT INTO users (email, password, role) VALUES 
('admin@hotel.com', '$2a$12$sbw59MGJ32aKCJ4vqMM/ZO9fNR.siatNldmc5yFoxwmQCXJLnfhbq', 'ADMIN');

INSERT INTO hotels (name, city, address) VALUES
('Hotel Warsaw Central', 'Warszawa', 'Aleje Jerozolimskie 1'),
('Krakow Old Town Inn', 'Kraków', 'Rynek Główny 5');

INSERT INTO rooms (hotel_id, room_number, type, capacity, price_per_night, floor, status, description) VALUES 
(1, '101', 'Standard', 1, 350.00, 1, 'FREE', 'Przytulny pokój z widokiem na patio'),
(1, '102', 'Double', 2, 500.00, 1, 'OCCUPIED', 'Przestronny pokój z dużym łóżkiem'),
(2, '101', 'Standard', 1, 320.00, 1, 'FREE', 'Cichy pokój w stylu vintage'),
(2, '201', 'Suite', 4, 1200.00, 2, 'CLEANING', 'Luksusowy apartament z dwoma sypialniami');

INSERT INTO amenities (name, price) VALUES 
('Śniadanie (Breakfast)', 40.00),
('Parking Podziemny', 25.00),
('Dostęp do SPA', 100.00),
('Późne wymeldowanie (Late Check-out)', 50.00),
('Łóżeczko dziecięce', 0.00);
```

## 3. Konfiguracja Połączenia z Bazą Danych

Aplikacja wymaga poprawnej konfiguracji pliku właściwości, aby nawiązać połączenie z bazą danych.

1.  Należy przejść do katalogu: `hotel-system/src/main/resources`.
2.  Znajduje się tam plik `database.properties`.
3.  Plik `database.properties` należy edytować, wprowadzając poprawne dane uwierzytelniające:

```properties
# Konfiguracja połączenia z bazą danych PostgreSQL
db.url=jdbc:postgresql://localhost:5432/hotel_db
db.username=postgres
db.password=twoje_haslo
```
> **Uwaga**: Wartość `db.url` musi zawierać poprawną nazwę bazy danych podaną w kroku 2 (tutaj: `hotel_db`).

## 4. Budowanie Projektu

W celu zbudowania projektu i pobrania wszystkich zależności, należy wykonać następujące polecenie w głównym katalogu projektu:

```bash
cd hotel-system
mvn clean install
```

## 5. Uruchomienie Systemu

System składa się z dwóch niezależnych modułów: **Serwera** oraz **Klienta**. Należy uruchomić je w dwóch osobnych terminalach w podanej kolejności.

### Krok 5.1: Uruchomienie Serwera

W pierwszym oknie terminala należy wykonać polecenie uruchamiające serwer aplikacji:

```bash
mvn javafx:run "-Dmain.class=com.hotel.server.ServerApp"
```

Po poprawnym uruchomieniu, pojawi się okno konsoli serwera. Należy kliknąć przycisk **"Start Server"**, aby rozpocząć nasłuchiwanie na połączenia przychodzące.

### Krok 5.2: Uruchomienie Klienta

W drugim oknie terminala (nie zamykając pierwszego) należy uruchomić aplikację kliencką:

```bash
mvn javafx:run
```

Spowoduje to otwarcie okna logowania do systemu.
