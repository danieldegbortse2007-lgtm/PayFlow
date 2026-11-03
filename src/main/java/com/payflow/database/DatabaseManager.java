
package com.payflow.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseManager {

    private static final String DATABASE_FOLDER =
            System.getProperty("user.home") + "/.payflow";

    private static final String DATABASE_URL =
            "jdbc:sqlite:" + DATABASE_FOLDER + "/payflow.db";

    // Establish a connection and enable SQLite foreign keys.
    public static Connection getConnection() throws SQLException {

        Connection connection =
                DriverManager.getConnection(DATABASE_URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            return connection;

        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }

    // Create the database folder and required tables.
    public static void initializeDatabase() {

        try {
            Path folderPath = Paths.get(DATABASE_FOLDER);
            Files.createDirectories(folderPath);

            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {

                // EMPLOYEES TABLE
                String createEmployeesTable = """
                        CREATE TABLE IF NOT EXISTS employees (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            employee_number TEXT NOT NULL UNIQUE,
                            first_name TEXT NOT NULL,
                            last_name TEXT NOT NULL,
                            email TEXT NOT NULL UNIQUE,
                            phone TEXT NOT NULL,
                            department TEXT NOT NULL,
                            job_title TEXT NOT NULL,
                            basic_salary TEXT NOT NULL,
                            active INTEGER NOT NULL DEFAULT 1,
                            created_at TEXT DEFAULT CURRENT_TIMESTAMP
                        )
                        """;

                statement.execute(createEmployeesTable);

                // PAYROLL TABLE
                String createPayrollTable = """
                        CREATE TABLE IF NOT EXISTS payroll (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            employee_id INTEGER NOT NULL,
                            pay_month INTEGER NOT NULL,
                            pay_year INTEGER NOT NULL,
                            basic_salary TEXT NOT NULL,
                            allowances TEXT NOT NULL DEFAULT '0.00',
                            overtime TEXT NOT NULL DEFAULT '0.00',
                            gross_salary TEXT NOT NULL,
                            tax_deduction TEXT NOT NULL DEFAULT '0.00',
                            ssnit_deduction TEXT NOT NULL DEFAULT '0.00',
                            other_deductions TEXT NOT NULL DEFAULT '0.00',
                            total_deductions TEXT NOT NULL,
                            net_salary TEXT NOT NULL,
                            payment_status TEXT NOT NULL DEFAULT 'PENDING',
                            payment_date TEXT,
                            created_at TEXT DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY (employee_id)
                                REFERENCES employees(id),

                            UNIQUE (employee_id, pay_month, pay_year)
                        )
                        """;

                statement.execute(createPayrollTable);

                // APPLICATION SETTINGS TABLE
                String createSettingsTable = """
                        CREATE TABLE IF NOT EXISTS app_settings (
                            id INTEGER PRIMARY KEY CHECK (id = 1),
                            company_name TEXT NOT NULL DEFAULT 'My Company',
                            company_address TEXT DEFAULT '',
                            company_phone TEXT DEFAULT '',
                            company_email TEXT DEFAULT '',
                            currency TEXT NOT NULL DEFAULT 'GHS',
                            working_days INTEGER NOT NULL DEFAULT 22,
                            default_tax_rate REAL NOT NULL DEFAULT 0.0,
                            default_ssnit_rate REAL NOT NULL DEFAULT 0.0
                        )
                        """;

                statement.execute(createSettingsTable);

                // INSERT DEFAULT SETTINGS IF THEY DO NOT EXIST
                statement.execute("""
                        INSERT OR IGNORE INTO app_settings (id)
                        VALUES (1)
                        """);

                // ADMINISTRATORS TABLE
                String createAdministratorsTable = """
                        CREATE TABLE IF NOT EXISTS administrators (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL UNIQUE,
                            password_hash TEXT NOT NULL,
                            created_at TEXT DEFAULT CURRENT_TIMESTAMP
                        )
                        """;

                statement.execute(createAdministratorsTable);

                System.out.println(
                        "PayFlow database initialized successfully."
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Database initialization failed: " + e.getMessage()
            );

            throw new IllegalStateException(
                    "Could not initialize PayFlow database.", e
            );
        }
    }
}
