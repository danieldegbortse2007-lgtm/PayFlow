
package com.payflow.repository;

import com.payflow.database.DatabaseManager;
import com.payflow.service.PasswordService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class AdministratorRepository {

    // Check whether an administrator account already exists.
    public boolean administratorExists() throws SQLException {

        String sql = "SELECT COUNT(*) FROM administrators";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            return result.next() && result.getInt(1) > 0;
        }
    }

    // Register the first administrator.
    public void createAdministrator(
            String username,
            char[] password) throws SQLException {

        validateUsername(username);

        if (password == null || password.length < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        try {
            if (administratorExists()) {
                throw new IllegalStateException(
                        "An administrator account already exists."
                );
            }

            String passwordHash =
                    PasswordService.hashPassword(password);

            String sql = """
                    INSERT INTO administrators
                    (username, password_hash)
                    VALUES (?, ?)
                    """;

            try (Connection connection =
                         DatabaseManager.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, username.trim());
                statement.setString(2, passwordHash);

                statement.executeUpdate();
            }

        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    // Authenticate an administrator.
    public boolean authenticate(
            String username,
            char[] password) throws SQLException {

        if (username == null || username.trim().isEmpty()
                || password == null) {
            return false;
        }

        String sql = """
                SELECT password_hash
                FROM administrators
                WHERE username = ?
                """;

        try {
            try (Connection connection =
                         DatabaseManager.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, username.trim());

                try (ResultSet result = statement.executeQuery()) {

                    if (!result.next()) {
                        return false;
                    }

                    String storedHash =
                            result.getString("password_hash");

                    return PasswordService.verifyPassword(
                            password,
                            storedHash
                    );
                }
            }

        } finally {
            Arrays.fill(password, '\0');
        }
    }

    // Validate administrator username.
    private void validateUsername(String username) {

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Username cannot be empty."
            );
        }

        if (username.trim().length() < 3) {
            throw new IllegalArgumentException(
                    "Username must contain at least 3 characters."
            );
        }
    }
}
