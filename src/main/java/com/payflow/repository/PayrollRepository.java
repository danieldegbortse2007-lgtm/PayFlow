
package com.payflow.repository;

import com.payflow.database.DatabaseManager;
import com.payflow.model.Payroll;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PayrollRepository {

    // Save a new payroll record.
    public void save(Payroll payroll) throws SQLException {

        validatePayroll(payroll);

        String sql = """
                INSERT INTO payroll (
                    employee_id,
                    pay_month,
                    pay_year,
                    basic_salary,
                    allowances,
                    overtime,
                    gross_salary,
                    tax_deduction,
                    ssnit_deduction,
                    other_deductions,
                    total_deductions,
                    net_salary,
                    payment_status,
                    payment_date
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, payroll.getEmployeeId());
            statement.setInt(2, payroll.getPayMonth());
            statement.setInt(3, payroll.getPayYear());

            statement.setString(4, payroll.getBasicSalary().toPlainString());
            statement.setString(5, payroll.getAllowances().toPlainString());
            statement.setString(6, payroll.getOvertime().toPlainString());
            statement.setString(7, payroll.getGrossSalary().toPlainString());

            statement.setString(8, payroll.getTaxDeduction().toPlainString());
            statement.setString(9, payroll.getSsnitDeduction().toPlainString());
            statement.setString(10, payroll.getOtherDeductions().toPlainString());
            statement.setString(11, payroll.getTotalDeductions().toPlainString());
            statement.setString(12, payroll.getNetSalary().toPlainString());

            statement.setString(13, payroll.getPaymentStatus());

            statement.setString(
                    14,
                    payroll.getPaymentDate() == null
                            ? null
                            : payroll.getPaymentDate().toString()
            );

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    payroll.setId(keys.getInt(1));
                }
            }
        }
    }

    // Retrieve all payroll records.
    public List<Payroll> findAll() throws SQLException {

        String sql = """
                SELECT p.*, e.employee_number,
                       e.first_name, e.last_name
                FROM payroll p
                JOIN employees e ON p.employee_id = e.id
                ORDER BY p.pay_year DESC, p.pay_month DESC
                """;

        return executeQuery(sql);
    }

    // Retrieve payroll for a particular month and year.
    public List<Payroll> findByPeriod(int month, int year)
            throws SQLException {

        validatePeriod(month, year);

        String sql = """
                SELECT p.*, e.employee_number,
                       e.first_name, e.last_name
                FROM payroll p
                JOIN employees e ON p.employee_id = e.id
                WHERE p.pay_month = ? AND p.pay_year = ?
                ORDER BY e.first_name, e.last_name
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, month);
            statement.setInt(2, year);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapResults(resultSet);
            }
        }
    }

    // Retrieve the payroll history of one employee.
    public List<Payroll> findByEmployeeId(int employeeId)
            throws SQLException {

        if (employeeId <= 0) {
            throw new IllegalArgumentException(
                    "Employee ID must be greater than zero."
            );
        }

        String sql = """
                SELECT p.*, e.employee_number,
                       e.first_name, e.last_name
                FROM payroll p
                JOIN employees e ON p.employee_id = e.id
                WHERE p.employee_id = ?
                ORDER BY p.pay_year DESC, p.pay_month DESC
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapResults(resultSet);
            }
        }
    }

    // Update payroll payment status.
    public void updatePaymentStatus(
            int payrollId,
            String status,
            LocalDate paymentDate
    ) throws SQLException {

        if (payrollId <= 0) {
            throw new IllegalArgumentException(
                    "Payroll ID must be greater than zero."
            );
        }

        if (!"PENDING".equals(status) && !"PAID".equals(status)) {
            throw new IllegalArgumentException(
                    "Payment status must be PENDING or PAID."
            );
        }

        if ("PAID".equals(status) && paymentDate == null) {
            throw new IllegalArgumentException(
                    "A payment date is required when marking payroll as PAID."
            );
        }

        if ("PENDING".equals(status)) {
            paymentDate = null;
        }

        String sql = """
                UPDATE payroll
                SET payment_status = ?, payment_date = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);

            statement.setString(
                    2,
                    paymentDate == null ? null : paymentDate.toString()
            );

            statement.setInt(3, payrollId);

            int updated = statement.executeUpdate();

            if (updated == 0) {
                throw new SQLException(
                        "Payroll record was not found."
                );
            }
        }
    }

    // Check whether an employee already has payroll for a period.
    public boolean existsForPeriod(
            int employeeId,
            int month,
            int year
    ) throws SQLException {

        if (employeeId <= 0) {
            throw new IllegalArgumentException(
                    "Employee ID must be greater than zero."
            );
        }

        validatePeriod(month, year);

        String sql = """
                SELECT COUNT(*)
                FROM payroll
                WHERE employee_id = ?
                  AND pay_month = ?
                  AND pay_year = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);
            statement.setInt(2, month);
            statement.setInt(3, year);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    // NEW: Check whether an employee has any saved payroll history.
    public boolean hasPayrollHistory(int employeeId) throws SQLException {

        if (employeeId <= 0) {
            throw new IllegalArgumentException(
                    "Employee ID must be greater than zero."
            );
        }

        String sql = """
                SELECT COUNT(*)
                FROM payroll
                WHERE employee_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    // Execute queries that do not require additional parameters.
    private List<Payroll> executeQuery(String sql) throws SQLException {

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return mapResults(resultSet);
        }
    }

    // Convert database results into Payroll objects.
    private List<Payroll> mapResults(ResultSet resultSet)
            throws SQLException {

        List<Payroll> payrollList = new ArrayList<>();

        while (resultSet.next()) {

            Payroll payroll = new Payroll();

            payroll.setId(resultSet.getInt("id"));
            payroll.setEmployeeId(resultSet.getInt("employee_id"));

            payroll.setEmployeeNumber(
                    resultSet.getString("employee_number")
            );

            payroll.setEmployeeName(
                    resultSet.getString("first_name") + " "
                    + resultSet.getString("last_name")
            );

            payroll.setPayMonth(resultSet.getInt("pay_month"));
            payroll.setPayYear(resultSet.getInt("pay_year"));

            payroll.setBasicSalary(
                    new BigDecimal(resultSet.getString("basic_salary"))
            );

            payroll.setAllowances(
                    new BigDecimal(resultSet.getString("allowances"))
            );

            payroll.setOvertime(
                    new BigDecimal(resultSet.getString("overtime"))
            );

            payroll.setGrossSalary(
                    new BigDecimal(resultSet.getString("gross_salary"))
            );

            payroll.setTaxDeduction(
                    new BigDecimal(resultSet.getString("tax_deduction"))
            );

            payroll.setSsnitDeduction(
                    new BigDecimal(resultSet.getString("ssnit_deduction"))
            );

            payroll.setOtherDeductions(
                    new BigDecimal(resultSet.getString("other_deductions"))
            );

            payroll.setTotalDeductions(
                    new BigDecimal(resultSet.getString("total_deductions"))
            );

            payroll.setNetSalary(
                    new BigDecimal(resultSet.getString("net_salary"))
            );

            payroll.setPaymentStatus(
                    resultSet.getString("payment_status")
            );

            String paymentDate = resultSet.getString("payment_date");

            payroll.setPaymentDate(
                    paymentDate == null
                            ? null
                            : LocalDate.parse(paymentDate)
            );

            payrollList.add(payroll);
        }

        return payrollList;
    }

    // Validate payroll information before saving.
    private void validatePayroll(Payroll payroll) {

        if (payroll == null) {
            throw new IllegalArgumentException(
                    "Payroll information cannot be null."
            );
        }

        if (payroll.getEmployeeId() <= 0) {
            throw new IllegalArgumentException(
                    "A valid employee must be selected."
            );
        }

        validatePeriod(payroll.getPayMonth(), payroll.getPayYear());

        validateAmount("Basic salary", payroll.getBasicSalary());
        validateAmount("Allowances", payroll.getAllowances());
        validateAmount("Overtime", payroll.getOvertime());
        validateAmount("Gross salary", payroll.getGrossSalary());
        validateAmount("Tax deduction", payroll.getTaxDeduction());
        validateAmount("SSNIT deduction", payroll.getSsnitDeduction());
        validateAmount("Other deductions", payroll.getOtherDeductions());
        validateAmount("Total deductions", payroll.getTotalDeductions());
        validateAmount("Net salary", payroll.getNetSalary());

        if (!"PENDING".equals(payroll.getPaymentStatus())
                && !"PAID".equals(payroll.getPaymentStatus())) {

            throw new IllegalArgumentException(
                    "Invalid payroll payment status."
            );
        }

        if ("PAID".equals(payroll.getPaymentStatus())
                && payroll.getPaymentDate() == null) {

            throw new IllegalArgumentException(
                    "A payment date is required for paid payroll."
            );
        }
    }

    // Validate monetary amounts.
    private void validateAmount(String field, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    field + " cannot be null or negative."
            );
        }
    }

    // Validate payroll month and year.
    private void validatePeriod(int month, int year) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Payroll month must be between 1 and 12."
            );
        }

        if (year < 2000 || year > 9999) {
            throw new IllegalArgumentException(
                    "Enter a valid payroll year."
            );
        }
    }
}
