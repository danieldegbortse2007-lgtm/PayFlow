package com.payflow.service;

import com.payflow.model.Payroll;
import com.payflow.repository.PayrollRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PayrollInsightsService {

    private final PayrollRepository payrollRepository;

    public PayrollInsightsService() {
        this.payrollRepository = new PayrollRepository();
    }

    public String generateInsights() throws SQLException {

        List<Payroll> records = payrollRepository.findAll();

        if (records == null || records.isEmpty()) {
            return "PAYFLOW PAYROLL INSIGHTS\n\n"
                    + "There are no payroll records available yet.\n"
                    + "Process payroll to begin receiving insights.";
        }

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal highestSalary = BigDecimal.ZERO;
        BigDecimal lowestSalary = null;

        int paidRecords = 0;
        int pendingRecords = 0;

        List<String> alerts = new ArrayList<>();

        for (Payroll payroll : records) {

            BigDecimal gross = safeAmount(payroll.getGrossSalary());
            BigDecimal net = safeAmount(payroll.getNetSalary());
            BigDecimal deductions = safeAmount(
                    payroll.getTotalDeductions()
            );

            totalGross = totalGross.add(gross);
            totalNet = totalNet.add(net);
            totalDeductions = totalDeductions.add(deductions);

            if (gross.compareTo(highestSalary) > 0) {
                highestSalary = gross;
            }

            if (lowestSalary == null
                    || gross.compareTo(lowestSalary) < 0) {
                lowestSalary = gross;
            }

            if ("PAID".equalsIgnoreCase(payroll.getPaymentStatus())) {
                paidRecords++;
            } else {
                pendingRecords++;
            }

            if (gross.compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal deductionRate = deductions
                        .multiply(new BigDecimal("100"))
                        .divide(gross, 2, RoundingMode.HALF_UP);

                if (deductionRate.compareTo(new BigDecimal("50")) > 0) {
                    alerts.add(
                            payroll.getEmployeeName()
                            + " has deductions above 50% of gross salary."
                    );
                }
            }

            if (net.compareTo(BigDecimal.ZERO) < 0) {
                alerts.add(
                        payroll.getEmployeeName()
                        + " has a negative net salary."
                );
            }

            if (deductions.compareTo(gross) > 0) {
                alerts.add(
                        payroll.getEmployeeName()
                        + " has deductions greater than gross salary."
                );
            }
        }

        BigDecimal averageGross = totalGross.divide(
                BigDecimal.valueOf(records.size()),
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal averageNet = totalNet.divide(
                BigDecimal.valueOf(records.size()),
                2,
                RoundingMode.HALF_UP
        );

        StringBuilder report = new StringBuilder();

        report.append("PAYFLOW AI - PAYROLL INSIGHTS\n");
        report.append("====================================\n\n");

        report.append("PAYROLL OVERVIEW\n");
        report.append("------------------------------------\n");

        report.append("Total payroll records: ")
                .append(records.size()).append("\n");

        report.append("Total gross payroll: GH₵")
                .append(format(totalGross)).append("\n");

        report.append("Total net payroll: GH₵")
                .append(format(totalNet)).append("\n");

        report.append("Total deductions: GH₵")
                .append(format(totalDeductions)).append("\n\n");

        report.append("SALARY ANALYSIS\n");
        report.append("------------------------------------\n");

        report.append("Highest gross salary: GH₵")
                .append(format(highestSalary)).append("\n");

        report.append("Lowest gross salary: GH₵")
                .append(format(lowestSalary)).append("\n");

        report.append("Average gross salary: GH₵")
                .append(format(averageGross)).append("\n");

        report.append("Average net salary: GH₵")
                .append(format(averageNet)).append("\n\n");

        report.append("PAYMENT STATUS\n");
        report.append("------------------------------------\n");

        report.append("Paid records: ")
                .append(paidRecords).append("\n");

        report.append("Pending records: ")
                .append(pendingRecords).append("\n\n");

        report.append("PAYROLL ALERTS\n");
        report.append("------------------------------------\n");

        if (alerts.isEmpty()) {
            report.append("No unusual payroll figures detected.\n");
        } else {
            for (String alert : alerts) {
                report.append("WARNING: ")
                        .append(alert)
                        .append("\n");
            }
        }

        report.append("\n");
        report.append("Note: These are automated checks, not proof ");
        report.append("of payroll errors. Review flagged records ");
        report.append("before taking action.");

        return report.toString();
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String format(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
