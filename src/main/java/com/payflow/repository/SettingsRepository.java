
package com.payflow.repository;

import com.payflow.database.DatabaseManager;
import com.payflow.model.AppSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsRepository {

    // Retrieve saved application settings.
    public AppSettings findSettings() throws SQLException {

        String sql = """
                SELECT company_name,
                       company_address,
                       company_phone,
                       company_email,
                       currency,
                       working_days,
                       default_tax_rate,
                       default_ssnit_rate
                FROM app_settings
                WHERE id = 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {

                return new AppSettings(
                        result.getString("company_name"),
                        result.getString("company_address"),
                        result.getString("company_phone"),
                        result.getString("company_email"),
                        result.getString("currency"),
                        result.getInt("working_days"),
                        result.getDouble("default_tax_rate"),
                        result.getDouble("default_ssnit_rate")
                );
            }
        }

        return new AppSettings(
                "My Company",
                "",
                "",
                "",
                "GHS",
                22,
                0.0,
                0.0
        );
    }

    // Save application settings.
    public void saveSettings(AppSettings settings) throws SQLException {

        String sql = """
                UPDATE app_settings
                SET company_name = ?,
                    company_address = ?,
                    company_phone = ?,
                    company_email = ?,
                    currency = ?,
                    working_days = ?,
                    default_tax_rate = ?,
                    default_ssnit_rate = ?
                WHERE id = 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, settings.getCompanyName());
            statement.setString(2, settings.getCompanyAddress());
            statement.setString(3, settings.getCompanyPhone());
            statement.setString(4, settings.getCompanyEmail());
            statement.setString(5, settings.getCurrency());
            statement.setInt(6, settings.getWorkingDays());
            statement.setDouble(7, settings.getDefaultTaxRate());
            statement.setDouble(8, settings.getDefaultSsnitRate());

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException(
                        "Settings could not be saved. Default record is missing."
                );
            }
        }
    }
}
