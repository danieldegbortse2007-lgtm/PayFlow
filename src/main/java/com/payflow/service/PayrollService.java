
package com.payflow.service;

import com.payflow.model.Payroll;
import com.payflow.model.AppSettings;
import com.payflow.repository.SettingsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

public class PayrollService {

    private final SettingsRepository settingsRepository =
            new SettingsRepository();

    // Automatically calculate payroll using saved settings.
    public Payroll calculatePayroll(
            Payroll payroll,
            BigDecimal otherDeductions) throws SQLException {

        AppSettings settings = settingsRepository.findSettings();

        BigDecimal taxRate = BigDecimal.valueOf(
                settings.getDefaultTaxRate()
        );

        BigDecimal ssnitRate = BigDecimal.valueOf(
                settings.getDefaultSsnitRate()
        );

        return calculatePayroll(
                payroll,
                taxRate,
                ssnitRate,
                otherDeductions
        );
    }

    // Calculate payroll using supplied percentage rates.
    public Payroll calculatePayroll(
            Payroll payroll,
            BigDecimal taxRate,
            BigDecimal ssnitRate,
            BigDecimal otherDeductions) {

        BigDecimal grossSalary = payroll.getBasicSalary()
                .add(payroll.getAllowances())
                .add(payroll.getOvertime());

        if (taxRate == null || ssnitRate == null
                || otherDeductions == null
                || taxRate.compareTo(BigDecimal.ZERO) < 0
                || ssnitRate.compareTo(BigDecimal.ZERO) < 0
                || taxRate.compareTo(new BigDecimal("100")) > 0
                || ssnitRate.compareTo(new BigDecimal("100")) > 0
                || otherDeductions.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Invalid tax, SSNIT or other deduction values."
            );
        }

        BigDecimal taxDeduction = grossSalary
                .multiply(taxRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal ssnitDeduction = grossSalary
                .multiply(ssnitRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal totalDeductions = taxDeduction
                .add(ssnitDeduction)
                .add(otherDeductions);

        BigDecimal netSalary = grossSalary.subtract(totalDeductions);

        if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Total deductions cannot exceed gross salary."
            );
        }

        payroll.setGrossSalary(
                grossSalary.setScale(2, RoundingMode.HALF_UP)
        );

        payroll.setTaxDeduction(taxDeduction);
        payroll.setSsnitDeduction(ssnitDeduction);
        payroll.setOtherDeductions(
                otherDeductions.setScale(2, RoundingMode.HALF_UP)
        );

        payroll.setTotalDeductions(
                totalDeductions.setScale(2, RoundingMode.HALF_UP)
        );

        payroll.setNetSalary(
                netSalary.setScale(2, RoundingMode.HALF_UP)
        );

        return payroll;
    }
}
