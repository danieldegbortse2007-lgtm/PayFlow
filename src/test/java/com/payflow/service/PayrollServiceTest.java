
package com.payflow.service;

import com.payflow.model.Payroll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PayrollServiceTest {

    private final PayrollService payrollService = new PayrollService();

    // TEST 1: Check whether gross salary and net salary are calculated correctly.
    @Test
    void shouldCalculateNetSalaryCorrectly() {

        Payroll payroll = new Payroll();

        payroll.setBasicSalary(new BigDecimal("5000.00"));
        payroll.setAllowances(new BigDecimal("500.00"));
        payroll.setOvertime(new BigDecimal("200.00"));

        Payroll result = payrollService.calculatePayroll(
                payroll,
                new BigDecimal("300.00"),
                new BigDecimal("200.00"),
                new BigDecimal("200.00")
        );

        assertEquals(
                new BigDecimal("5700.00"),
                result.getGrossSalary()
        );

        assertEquals(
                new BigDecimal("700.00"),
                result.getTotalDeductions()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                result.getNetSalary()
        );
    }

    // TEST 2: Prevent deductions from exceeding gross salary.
    @Test
    void shouldRejectDeductionsGreaterThanGrossSalary() {

        Payroll payroll = new Payroll();

        payroll.setBasicSalary(new BigDecimal("1000.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> payrollService.calculatePayroll(
                        payroll,
                        new BigDecimal("500.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("300.00")
                )
        );
    }

    // TEST 3: Check payroll when there are no deductions.
    @Test
    void shouldCalculateFullSalaryWhenThereAreNoDeductions() {

        Payroll payroll = new Payroll();

        payroll.setBasicSalary(new BigDecimal("2500.00"));

        Payroll result = payrollService.calculatePayroll(
                payroll,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        assertEquals(
                new BigDecimal("2500.00"),
                result.getNetSalary()
        );
    }
}

