
package com.payflow.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.payflow.database.DatabaseManager;
import com.payflow.model.Employee;

public class EmployeeRepository {

    // Register a new employee.
    public void save(Employee employee) throws SQLException {

        String sql = """
                INSERT INTO employees (
                    employee_number,
                    first_name,
                    last_name,
                    email,
                    phone,
                    department,
                    job_title,
                    basic_salary
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employee.getEmployeeNumber());
            statement.setString(2, employee.getFirstName());
            statement.setString(3, employee.getLastName());
            statement.setString(4, employee.getEmail());
            statement.setString(5, employee.getPhone());
            statement.setString(6, employee.getDepartment());
            statement.setString(7, employee.getJobTitle());
            statement.setString(8, employee.getBasicSalary().toPlainString());

            statement.executeUpdate();
        }
    }

    // Retrieve all employees, including inactive employees.
    public List<Employee> findAll() throws SQLException {

        String sql = "SELECT * FROM employees ORDER BY id DESC";

        List<Employee> employees = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                employees.add(mapEmployee(resultSet));
            }
        }

        return employees;
    }

    // Retrieve only active employees for payroll processing.
    public List<Employee> findAllActive() throws SQLException {

        String sql = """
                SELECT * FROM employees
                WHERE active = 1
                ORDER BY id DESC
                """;

        List<Employee> employees = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                employees.add(mapEmployee(resultSet));
            }
        }

        return employees;
    }

    // Find one employee using their database ID.
    public Employee findById(int id) throws SQLException {

        String sql = "SELECT * FROM employees WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapEmployee(resultSet);
                }
            }
        }

        return null;
    }

    // Search by employee number, name, email or phone.
    public List<Employee> search(String keyword) throws SQLException {

        String sql = """
                SELECT * FROM employees
                WHERE employee_number LIKE ?
                   OR first_name LIKE ?
                   OR last_name LIKE ?
                   OR email LIKE ?
                   OR phone LIKE ?
                ORDER BY id DESC
                """;

        List<Employee> employees = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchValue = "%" + keyword.trim() + "%";

            for (int i = 1; i <= 5; i++) {
                statement.setString(i, searchValue);
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    employees.add(mapEmployee(resultSet));
                }
            }
        }

        return employees;
    }

    // Update an employee's information without changing their status.
    public boolean update(Employee employee) throws SQLException {

        String sql = """
                UPDATE employees
                SET employee_number = ?,
                    first_name = ?,
                    last_name = ?,
                    email = ?,
                    phone = ?,
                    department = ?,
                    job_title = ?,
                    basic_salary = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employee.getEmployeeNumber());
            statement.setString(2, employee.getFirstName());
            statement.setString(3, employee.getLastName());
            statement.setString(4, employee.getEmail());
            statement.setString(5, employee.getPhone());
            statement.setString(6, employee.getDepartment());
            statement.setString(7, employee.getJobTitle());
            statement.setString(8, employee.getBasicSalary().toPlainString());
            statement.setInt(9, employee.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // Deactivate an employee without deleting their record.
    public boolean deactivate(int id) throws SQLException {

        String sql = """
                UPDATE employees
                SET active = 0
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    // Reactivate an existing employee.
    public boolean activate(int id) throws SQLException {

        String sql = """
                UPDATE employees
                SET active = 1
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    // Permanently delete an employee and all their payroll history.
    public boolean deletePermanently(int employeeId) throws SQLException {

        if (employeeId <= 0) {
            throw new IllegalArgumentException(
                    "Employee ID must be greater than zero."
            );
        }

        String deletePayroll = """
                DELETE FROM payroll
                WHERE employee_id = ?
                """;

        String deleteEmployee = """
                DELETE FROM employees
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Delete the employee's payroll records first.
                try (PreparedStatement payrollStatement =
                             connection.prepareStatement(deletePayroll)) {

                    payrollStatement.setInt(1, employeeId);
                    payrollStatement.executeUpdate();
                }

                // Then delete the employee.
                int deleted;

                try (PreparedStatement employeeStatement =
                             connection.prepareStatement(deleteEmployee)) {

                    employeeStatement.setInt(1, employeeId);
                    deleted = employeeStatement.executeUpdate();
                }

                connection.commit();

                return deleted > 0;

            } catch (SQLException e) {

                connection.rollback();
                throw e;

            } finally {

                connection.setAutoCommit(true);
            }
        }
    }

    // Check whether an employee number or email already exists.
    public boolean exists(String employeeNumber, String email)
            throws SQLException {

        String sql = """
                SELECT COUNT(*) FROM employees
                WHERE employee_number = ? OR email = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employeeNumber);
            statement.setString(2, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    // Convert a database record into an Employee object.
    private Employee mapEmployee(ResultSet resultSet) throws SQLException {

        Employee employee = new Employee(
                resultSet.getString("employee_number"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                resultSet.getString("department"),
                resultSet.getString("job_title"),
                new BigDecimal(resultSet.getString("basic_salary"))
        );

        employee.setId(resultSet.getInt("id"));
        employee.setActive(resultSet.getInt("active") == 1);

        return employee;
    }
}
