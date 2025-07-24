package org.example.CoreCarService.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInit {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInit.class);

    public void initRegAuthDB() {
        ConnectionServerInfo connInfo = new ConnectionServerInfo();
        try (Connection conn = connInfo.getConnectionPostgresql();
             Statement stmt = conn.createStatement()) {

            if (conn != null) {
                System.out.println("Соединение с БД PostgreSQL установлено!");
                logger.info("Соединение с БД PostgreSQL установлено!");

                // Таблица пользователей
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS reg_user_info (
                        id SERIAL PRIMARY KEY,
                        login VARCHAR(50) UNIQUE NOT NULL,
                        username VARCHAR(50) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        phone_number VARCHAR(255) NOT NULL,
                        user_type VARCHAR(50) NOT NULL,
                        registration_user_status VARCHAR(50) NOT NULL,
                        method_communicaton VARCHAR(50) DEFAULT 'telephone',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        role VARCHAR(50) DEFAULT 'client'
                    )
                """);

                // История активности пользователей
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS user_activity_history (
                        id SERIAL PRIMARY KEY,
                        user_login VARCHAR(100) NOT NULL,
                        activity_at VARCHAR(100) NOT NULL,
                        FOREIGN KEY (user_login) REFERENCES reg_user_info(login)
                    )
                """);

                // Клиентские автомобили
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cars_clients (
                        id SERIAL PRIMARY KEY,
                        user_id INT NOT NULL,
                        make VARCHAR(50) NOT NULL,
                        model VARCHAR(50) NOT NULL,
                        car_year INT NOT NULL,
                        vin VARCHAR(17) UNIQUE NOT NULL,
                        mileage INT NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES reg_user_info(id)
                    )
                """);

                // Мастера сервиса
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS workers_service (
                        id SERIAL PRIMARY KEY,
                        user_id INT NOT NULL,
                        username VARCHAR(50) NOT NULL,
                        speciality VARCHAR(100),
                        FOREIGN KEY (user_id) REFERENCES reg_user_info(id)
                    )
                """);

                // Услуги сервиса
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS service_work (
                        id SERIAL PRIMARY KEY,
                        worker_id INT NOT NULL,
                        service_name VARCHAR(100) NOT NULL,
                        type_of_service VARCHAR(100) NOT NULL,
                        service_focus VARCHAR(100) NOT NULL,
                        price DECIMAL(10,2) NOT NULL,
                        FOREIGN KEY (worker_id) REFERENCES workers_service(id)
                    )
                """);

                // Запчасти
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS parts (
                        id SERIAL PRIMARY KEY,
                        parts_name VARCHAR(255),
                        quantity_patrs INT NOT NULL,
                        price_for_one DECIMAL(10,2),
                        price_for_everything DECIMAL(10,2) NOT NULL
                    )
                """);

                // Заказ-наряды
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS orders_clients (
                        id SERIAL PRIMARY KEY,
                        car_id INT NOT NULL,
                        worker_id INT NOT NULL,
                        client_id INT NOT NULL,
                        receipt_date DATE NOT NULL,
                        start_date DATE,
                        finish_date DATE,
                        problem_description TEXT,
                        status VARCHAR(50) DEFAULT 'OPEN',
                        total_cost DECIMAL(12,2) DEFAULT 0.00,
                        payment_status VARCHAR(50) DEFAULT 'UNPAID',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (car_id) REFERENCES cars_clients(id),
                        FOREIGN KEY (worker_id) REFERENCES workers_service(id),
                        FOREIGN KEY (client_id) REFERENCES reg_user_info(id)
                    )
                """);

                // Используемые услуги в заказе
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS used_order_workers (
                        order_id INT NOT NULL,
                        service_id INT NOT NULL,
                        quantity INT DEFAULT 1,
                        price DECIMAL(10,2),
                        PRIMARY KEY (order_id, service_id),
                        FOREIGN KEY (order_id) REFERENCES orders_clients(id),
                        FOREIGN KEY (service_id) REFERENCES service_work(id)
                    )
                """);

                // Используемые запчасти в заказе
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS used_order_parts (
                        order_id INT NOT NULL,
                        part_id INT NOT NULL,
                        quantity_used INT NOT NULL,
                        price DECIMAL(10,2) NOT NULL,
                        PRIMARY KEY (order_id, part_id),
                        FOREIGN KEY (order_id) REFERENCES orders_clients(id),
                        FOREIGN KEY (part_id) REFERENCES parts(id)
                    )
                """);

            }
        } catch (SQLException e) {
            logger.error("Ошибка при инициализации БД", e);
        }
    }
}