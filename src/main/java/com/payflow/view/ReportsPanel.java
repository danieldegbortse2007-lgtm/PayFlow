
package com.payflow.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import com.payflow.model.Employee;
import com.payflow.model.Payroll;
import com.payflow.repository.EmployeeRepository;
import com.payflow.repository.PayrollRepository;

public class ReportsPanel extends JPanel {

    private final Color BACKGROUND = new Color(245, 247, 251);
    private final Color TEXT = new Color(35, 45, 65);
    private final Color MUTED = new Color(110, 120, 140);
    private final Color BLUE = new Color(48, 105, 220);

    private final PayrollRepository payrollRepository =
            new PayrollRepository();

    private final EmployeeRepository employeeRepository =
            new EmployeeRepository();

    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;

    private JLabel recordCountLabel;
    private JLabel totalPayrollLabel;

    private JTable reportTable;
    private DefaultTableModel tableModel;

    private List<Payroll> displayedPayrollRecords =
            new ArrayList<>();

    private List<Employee> displayedEmployees =
            new ArrayList<>();

    public ReportsPanel() {

        setLayout(new BorderLayout(0, 20));
        setBackground(BACKGROUND);

        setBorder(
                BorderFactory.createEmptyBorder(10, 5, 10, 5)
        );

        add(createFilterSection(), BorderLayout.NORTH);
        add(createReportContent(), BorderLayout.CENTER);

        loadReport();
    }

    // --------------------------------------------------
    // FILTER SECTION
    // --------------------------------------------------

    private JPanel createFilterSection() {

        JPanel panel = new JPanel(new BorderLayout(10, 15));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        BorderFactory.createEmptyBorder(
                                18, 18, 18, 18
                        )
                )
        );

        JLabel heading = new JLabel("Generate Reports");

        heading.setFont(new Font("Arial", Font.BOLD, 19));
        heading.setForeground(TEXT);

        JPanel filters = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 5)
        );

        filters.setBackground(Color.WHITE);

        reportTypeCombo = new JComboBox<>(new String[]{
                "Monthly Payroll",
                "Annual Payroll",
                "Employee Salary",
                "Payment Status"
        });

        monthCombo = new JComboBox<>();

        monthCombo.addItem("All Months");

        for (Month month : Month.values()) {
            monthCombo.addItem(
                    month.getDisplayName(
                            java.time.format.TextStyle.FULL,
                            Locale.ENGLISH
                    )
            );
        }

        yearCombo = new JComboBox<>();

        int currentYear = LocalDate.now().getYear();

        for (int year = currentYear + 2; year >= 2000; year--) {
            yearCombo.addItem(year);
        }

        yearCombo.setSelectedItem(currentYear);

        JButton generateButton = createButton(
                "Generate Report",
                BLUE
        );

        JButton exportButton = createButton(
                "Export PDF",
                new Color(35, 145, 95)
        );

        generateButton.addActionListener(event -> loadReport());

        exportButton.addActionListener(event -> exportPDF());

        filters.add(new JLabel("Report Type:"));
        filters.add(reportTypeCombo);

        filters.add(new JLabel("Month:"));
        filters.add(monthCombo);

        filters.add(new JLabel("Year:"));
        filters.add(yearCombo);

        filters.add(generateButton);
        filters.add(exportButton);

        reportTypeCombo.addActionListener(
                event -> updateFilterAvailability()
        );

        panel.add(heading, BorderLayout.NORTH);
        panel.add(filters, BorderLayout.CENTER);

        return panel;
    }

    // --------------------------------------------------
    // BUTTON STYLING
    // --------------------------------------------------

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setUI(new BasicButtonUI());

        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(false);

        button.setHorizontalAlignment(SwingConstants.CENTER);

        button.setBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );

        button.setCursor(
                new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
        );

        return button;
    }

    private void updateFilterAvailability() {

        String type = (String) reportTypeCombo.getSelectedItem();

        boolean monthly = "Monthly Payroll".equals(type);

        monthCombo.setEnabled(monthly);

        if (!monthly) {
            monthCombo.setSelectedItem("All Months");
        }
    }

    // --------------------------------------------------
    // REPORT CONTENT
    // --------------------------------------------------

    private JPanel createReportContent() {

        JPanel content = new JPanel(
                new BorderLayout(0, 15)
        );

        content.setBackground(BACKGROUND);

        JPanel summary = new JPanel(
                new GridLayout(1, 2, 15, 0)
        );

        summary.setBackground(BACKGROUND);
        summary.setPreferredSize(new java.awt.Dimension(0, 95));

        recordCountLabel = new JLabel("0 records");
        totalPayrollLabel = new JLabel("GH₵ 0.00");

        summary.add(
                createSummaryCard("Total Records", recordCountLabel)
        );

        summary.add(
                createSummaryCard("Total Net Payroll", totalPayrollLabel)
        );

        String[] columns = {
                "Employee Number",
                "Employee Name",
                "Period",
                "Basic Salary",
                "Gross Salary",
                "Deductions",
                "Net Salary",
                "Payment Status",
                "Payment Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);

        reportTable.setRowHeight(32);
        reportTable.setFont(new Font("Arial", Font.PLAIN, 12));

        reportTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(reportTable);

        content.add(summary, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private JPanel createSummaryCard(
            String heading,
            JLabel valueLabel) {

        JPanel card = new JPanel();

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        BorderFactory.createEmptyBorder(
                                12, 18, 12, 18
                        )
                )
        );

        JLabel title = new JLabel(heading);

        title.setFont(new Font("Arial", Font.PLAIN, 13));
        title.setForeground(MUTED);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 21));
        valueLabel.setForeground(TEXT);

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    // --------------------------------------------------
    // GENERATE REPORT
    // --------------------------------------------------

    public void loadReport() {

        try {

            tableModel.setRowCount(0);

            displayedPayrollRecords.clear();
            displayedEmployees.clear();

            String type = (String) reportTypeCombo.getSelectedItem();

            if ("Employee Salary".equals(type)) {
                loadEmployeeSalaryReport();
            } else {
                loadPayrollReport(type);
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not generate report:\n" + e.getMessage(),
                    "Report Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    private void loadPayrollReport(String type) throws Exception {

        List<Payroll> records = payrollRepository.findAll();

        int selectedYear = (Integer) yearCombo.getSelectedItem();

        String selectedMonth =
                (String) monthCombo.getSelectedItem();

        int monthNumber = 0;

        if (!"All Months".equals(selectedMonth)) {
            monthNumber = Month.valueOf(
                    selectedMonth.toUpperCase(Locale.ENGLISH)
            ).getValue();
        }

        BigDecimal total = BigDecimal.ZERO;

        int count = 0;

        for (Payroll payroll : records) {

            if (payroll.getPayYear() != selectedYear) {
                continue;
            }

            if ("Monthly Payroll".equals(type)
                    && monthNumber != 0
                    && payroll.getPayMonth() != monthNumber) {
                continue;
            }

            if ("Payment Status".equals(type)) {
                // Both paid and pending records are included.
            }

            displayedPayrollRecords.add(payroll);

            tableModel.addRow(new Object[]{
                    payroll.getEmployeeNumber(),
                    payroll.getEmployeeName(),
                    String.format(
                            Locale.ROOT,
                            "%02d/%04d",
                            payroll.getPayMonth(),
                            payroll.getPayYear()
                    ),
                    formatCurrency(payroll.getBasicSalary()),
                    formatCurrency(payroll.getGrossSalary()),
                    formatCurrency(payroll.getTotalDeductions()),
                    formatCurrency(payroll.getNetSalary()),
                    payroll.getPaymentStatus(),
                    payroll.getPaymentDate() == null
                            ? "Not paid"
                            : payroll.getPaymentDate().toString()
            });

            total = total.add(payroll.getNetSalary());
            count++;
        }

        recordCountLabel.setText(String.valueOf(count));

        totalPayrollLabel.setText(formatCurrency(total));
    }

    // --------------------------------------------------
    // EMPLOYEE SALARY REPORT
    // --------------------------------------------------

    private void loadEmployeeSalaryReport() throws Exception {

        displayedEmployees = employeeRepository.findAll();

        BigDecimal totalSalary = BigDecimal.ZERO;

        int count = 0;

        for (Employee employee : displayedEmployees) {

            BigDecimal salary = employee.getBasicSalary();

            if (salary == null) {
                salary = BigDecimal.ZERO;
            }

            tableModel.addRow(new Object[]{
                    employee.getEmployeeNumber(),
                    employee.getFullName(),
                    "Current Salary",
                    formatCurrency(salary),
                    "-",
                    "-",
                    formatCurrency(salary),
                    "N/A",
                    "N/A"
            });

            totalSalary = totalSalary.add(salary);
            count++;
        }

        recordCountLabel.setText(String.valueOf(count));

        totalPayrollLabel.setText(
                formatCurrency(totalSalary)
        );
    }

    // --------------------------------------------------
    // PDF EXPORT
    // --------------------------------------------------

    private void exportPDF() {

        if (tableModel.getRowCount() == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "There are no records to export.",
                    "No Report Data",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        javax.swing.JFileChooser chooser =
                new javax.swing.JFileChooser();

        chooser.setDialogTitle("Save Payroll Report");

        chooser.setSelectedFile(
                new java.io.File("PayFlow_Report.pdf")
        );

        int result = chooser.showSaveDialog(this);

        if (result != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File file = chooser.getSelectedFile();

        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            file = new java.io.File(file.getAbsolutePath() + ".pdf");
        }

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);

            document.addPage(page);

            try (PDPageContentStream stream =
                    new PDPageContentStream(document, page)) {

                stream.beginText();

                stream.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),
                        18
                );

                stream.newLineAtOffset(40, 800);

                stream.showText("PayFlow Payroll Report");

                stream.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                        9
                );

                stream.newLineAtOffset(0, -20);

                stream.showText(
                        "Generated: " + LocalDate.now()
                );

                stream.newLineAtOffset(0, -18);

                stream.showText(
                        "Total Records: " + recordCountLabel.getText()
                );

                stream.newLineAtOffset(0, -18);

                stream.showText(
                        "Total Net Payroll: "
                        + totalPayrollLabel.getText()
                );

                stream.newLineAtOffset(0, -28);

                for (int row = 0; row < tableModel.getRowCount(); row++) {

                    String employeeNumber =
                            safeText(tableModel.getValueAt(row, 0));

                    String employeeName =
                            safeText(tableModel.getValueAt(row, 1));

                    String period =
                            safeText(tableModel.getValueAt(row, 2));

                    String netSalary =
                            safeText(tableModel.getValueAt(row, 6));

                    String status =
                            safeText(tableModel.getValueAt(row, 7));

                    String line = employeeNumber + " | "
                            + employeeName + " | "
                            + period + " | "
                            + netSalary + " | "
                            + status;

                    if (line.length() > 110) {
                        line = line.substring(0, 110);
                    }

                    stream.showText(line);
                    stream.newLineAtOffset(0, -14);
                }

                stream.endText();
            }

            document.save(file);

            JOptionPane.showMessageDialog(
                    this,
                    "Report exported successfully:\n"
                    + file.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not export PDF:\n" + e.getMessage(),
                    "PDF Export Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String safeText(Object value) {

        if (value == null) {
            return "-";
        }

        return value.toString()
                .replace("\n", " ")
                .replace("\r", " ");
    }

    // --------------------------------------------------
    // CURRENCY FORMATTING
    // --------------------------------------------------

    private String formatCurrency(BigDecimal amount) {

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        return String.format(
                Locale.US,
                "GH₵ %,.2f",
                amount
        );
    }
}
