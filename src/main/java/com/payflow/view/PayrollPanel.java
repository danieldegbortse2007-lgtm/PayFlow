
package com.payflow.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;

import com.payflow.model.Employee;
import com.payflow.model.Payroll;
import com.payflow.repository.EmployeeRepository;
import com.payflow.repository.PayrollRepository;
import com.payflow.service.PayrollService;

public class PayrollPanel extends JPanel {

    private final Color BACKGROUND = new Color(245, 247, 251);
    private final Color BLUE = new Color(48, 105, 220);
    private final Color TEXT = new Color(35, 45, 65);
    private final Color MUTED = new Color(110, 120, 140);

    private final EmployeeRepository employeeRepository =
            new EmployeeRepository();

    private final PayrollRepository payrollRepository =
            new PayrollRepository();

    private final PayrollService payrollService =
            new PayrollService();

    private JComboBox<Employee> employeeCombo;
    private JComboBox<String> monthCombo;
    private JSpinner yearSpinner;

    private JTextField allowancesField;
    private JTextField overtimeField;
    private JTextField taxField;
    private JTextField ssnitField;
    private JTextField otherDeductionsField;

    private JLabel basicSalaryLabel;
    private JLabel grossSalaryLabel;
    private JLabel taxSummaryLabel;
    private JLabel ssnitSummaryLabel;
    private JLabel totalDeductionsLabel;
    private JLabel netSalaryLabel;
    private JLabel employeeStatusLabel;

    private JButton calculateButton;
    private JButton saveButton;

    private DefaultTableModel tableModel;
    private JTable payrollTable;

    private List<Payroll> loadedPayrollRecords;
    private Payroll calculatedPayroll;

    public PayrollPanel() {

        setLayout(new BorderLayout(0, 10));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(10, 5, 10, 5));

        add(createHeading(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadActiveEmployees();
        loadPayrollRecords();
    }

    // --------------------------------------------------
    // HEADING
    // --------------------------------------------------

    private JPanel createHeading() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND);

        JLabel heading = new JLabel("Payroll Management");
        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Calculate, process and manage employee salaries."
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(7));
        panel.add(subtitle);

        return panel;
    }

    // --------------------------------------------------
    // MAIN CONTENT
    // --------------------------------------------------

    private JPanel createMainContent() {

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND);

        panel.add(createPayrollForm(), BorderLayout.NORTH);
        panel.add(createPayrollTable(), BorderLayout.CENTER);

        return panel;
    }

    // --------------------------------------------------
    // PAYROLL FORM
    // --------------------------------------------------

    private JPanel createPayrollForm() {

        JPanel form = new JPanel(new BorderLayout(10, 10));
        form.setBackground(Color.WHITE);

        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 230, 238)
                ),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JPanel fields = new JPanel(new GridLayout(0, 2, 15, 6));
        fields.setBackground(Color.WHITE);

        employeeCombo = new JComboBox<>();

        employeeCombo.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        employeeCombo.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean selected,
                    boolean focused) {

                super.getListCellRendererComponent(
                        list, value, index, selected, focused
                );

                if (value instanceof Employee) {

                    Employee employee = (Employee) value;

                    setText(
                            employee.getEmployeeNumber()
                            + " - "
                            + employee.getFullName()
                    );
                }

                return this;
            }
        });

        monthCombo = new JComboBox<>();

        for (Month month : Month.values()) {
            monthCombo.addItem(
                    month.getDisplayName(
                            TextStyle.FULL,
                            Locale.ENGLISH
                    )
            );
        }

        monthCombo.setSelectedIndex(
                LocalDate.now().getMonthValue() - 1
        );

        yearSpinner = new JSpinner(
                new SpinnerNumberModel(
                        LocalDate.now().getYear(),
                        2000,
                        9999,
                        1
                )
        );

        JSpinner.NumberEditor yearEditor =
                new JSpinner.NumberEditor(yearSpinner, "0");

        yearSpinner.setEditor(yearEditor);

        allowancesField = new JTextField("0.00");
        overtimeField = new JTextField("0.00");

        taxField = new JTextField("0.00");
        ssnitField = new JTextField("0.00");

        taxField.setEditable(false);
        ssnitField.setEditable(false);

        taxField.setBackground(new Color(240, 243, 248));
        ssnitField.setBackground(new Color(240, 243, 248));

        otherDeductionsField = new JTextField("0.00");

        addField(fields, "Select Employee", employeeCombo);
        addField(fields, "Payroll Month", monthCombo);
        addField(fields, "Payroll Year", yearSpinner);
        addField(fields, "Allowances (GH₵)", allowancesField);
        addField(fields, "Overtime (GH₵)", overtimeField);
        addField(fields, "Tax Deduction (Automatic)", taxField);
        addField(fields, "SSNIT Deduction (Automatic)", ssnitField);
        addField(fields, "Other Deductions (GH₵)", otherDeductionsField);

        employeeStatusLabel = new JLabel(" ");

        employeeStatusLabel.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        employeeStatusLabel.setForeground(
                new Color(190, 90, 35)
        );

        JPanel fieldsContainer = new JPanel(new BorderLayout(0, 5));
        fieldsContainer.setBackground(Color.WHITE);
        fieldsContainer.add(fields, BorderLayout.CENTER);
        fieldsContainer.add(employeeStatusLabel, BorderLayout.SOUTH);

        JPanel summary = createSalarySummary();

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );

        buttons.setBackground(Color.WHITE);

        calculateButton = createButton(
                "Calculate Salary", BLUE
        );

        saveButton = createButton(
                "Save Payroll", new Color(35, 150, 95)
        );

        JButton refreshButton = createButton(
                "Refresh", new Color(100, 110, 125)
        );

        calculateButton.addActionListener(
                event -> calculatePayroll(true)
        );

        saveButton.addActionListener(
                event -> savePayroll()
        );

        refreshButton.addActionListener(event -> {
            loadActiveEmployees();
            loadPayrollRecords();
            clearCalculation();
        });

        allowancesField.addActionListener(
                event -> calculatePayroll(false)
        );

        overtimeField.addActionListener(
                event -> calculatePayroll(false)
        );

        otherDeductionsField.addActionListener(
                event -> calculatePayroll(false)
        );

        employeeCombo.addActionListener(
                event -> calculatePayroll(false)
        );

        buttons.add(refreshButton);
        buttons.add(calculateButton);
        buttons.add(saveButton);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setBackground(Color.WHITE);

        bottom.add(summary, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.SOUTH);

        form.add(fieldsContainer, BorderLayout.CENTER);
        form.add(bottom, BorderLayout.SOUTH);

        return form;
    }

    private void addField(
            JPanel panel,
            String label,
            JComponent component) {

        JPanel fieldPanel = new JPanel(
                new BorderLayout(0, 4)
        );

        fieldPanel.setBackground(Color.WHITE);

        JLabel fieldLabel = new JLabel(label);

        fieldLabel.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        fieldLabel.setForeground(TEXT);

        component.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        fieldPanel.add(fieldLabel, BorderLayout.NORTH);
        fieldPanel.add(component, BorderLayout.CENTER);

        panel.add(fieldPanel);
    }

    // --------------------------------------------------
    // SALARY SUMMARY
    // --------------------------------------------------

    private JPanel createSalarySummary() {

        JPanel summary = new JPanel(
                new GridLayout(3, 2, 15, 8)
        );

        summary.setBackground(
                new Color(247, 249, 253)
        );

        summary.setBorder(
                new EmptyBorder(8, 12, 8, 12)
        );

        basicSalaryLabel = createSummaryLabel(
                "Basic Salary: GH₵ 0.00"
        );

        grossSalaryLabel = createSummaryLabel(
                "Gross Salary: GH₵ 0.00"
        );

        taxSummaryLabel = createSummaryLabel(
                "Tax: GH₵ 0.00"
        );

        ssnitSummaryLabel = createSummaryLabel(
                "SSNIT: GH₵ 0.00"
        );

        totalDeductionsLabel = createSummaryLabel(
                "Total Deductions: GH₵ 0.00"
        );

        netSalaryLabel = createSummaryLabel(
                "Net Salary: GH₵ 0.00"
        );

        summary.add(basicSalaryLabel);
        summary.add(grossSalaryLabel);
        summary.add(taxSummaryLabel);
        summary.add(ssnitSummaryLabel);
        summary.add(totalDeductionsLabel);
        summary.add(netSalaryLabel);

        return summary;
    }

    private JLabel createSummaryLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        label.setForeground(TEXT);

        return label;
    }

    // --------------------------------------------------
    // CALCULATE PAYROLL
    // --------------------------------------------------

    private void calculatePayroll(boolean showMessage) {

        calculatedPayroll = null;

        try {

            Employee employee =
                    (Employee) employeeCombo.getSelectedItem();

            if (employee == null) {
                throw new IllegalArgumentException(
                        "Please register and select an active employee."
                );
            }

            int month = monthCombo.getSelectedIndex() + 1;
            int year = (Integer) yearSpinner.getValue();

            BigDecimal allowances = readAmount(allowancesField);
            BigDecimal overtime = readAmount(overtimeField);
            BigDecimal other = readAmount(otherDeductionsField);

            Payroll payroll = new Payroll();

            payroll.setEmployeeId(employee.getId());
            payroll.setEmployeeNumber(employee.getEmployeeNumber());
            payroll.setEmployeeName(employee.getFullName());

            payroll.setPayMonth(month);
            payroll.setPayYear(year);

            payroll.setBasicSalary(employee.getBasicSalary());
            payroll.setAllowances(allowances);
            payroll.setOvertime(overtime);

            payroll.setPaymentStatus("PENDING");
            payroll.setPaymentDate(null);

            calculatedPayroll = payrollService.calculatePayroll(
                    payroll,
                    other
            );

            taxField.setText(
                    calculatedPayroll.getTaxDeduction().toPlainString()
            );

            ssnitField.setText(
                    calculatedPayroll.getSsnitDeduction().toPlainString()
            );

            updateSalarySummary(calculatedPayroll);

            if (showMessage) {
                JOptionPane.showMessageDialog(
                        this,
                        "Payroll calculated successfully!",
                        "Calculation Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (NumberFormatException e) {

            calculatedPayroll = null;
            showError("Enter valid numbers for allowances, overtime "
                    + "and other deductions.");

        } catch (IllegalArgumentException e) {

            calculatedPayroll = null;
            showError(e.getMessage());

        } catch (SQLException e) {

            calculatedPayroll = null;
            showError("Could not retrieve payroll settings: "
                    + e.getMessage());
        }
    }

    private BigDecimal readAmount(JTextField field) {

        String value = field.getText().trim();

        if (value.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal amount = new BigDecimal(value);

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Salary amounts and deductions cannot be negative."
            );
        }

        return amount;
    }

    private void updateSalarySummary(Payroll payroll) {

        basicSalaryLabel.setText(
                "Basic Salary: " + money(payroll.getBasicSalary())
        );

        grossSalaryLabel.setText(
                "Gross Salary: " + money(payroll.getGrossSalary())
        );

        taxSummaryLabel.setText(
                "Tax: " + money(payroll.getTaxDeduction())
        );

        ssnitSummaryLabel.setText(
                "SSNIT: " + money(payroll.getSsnitDeduction())
        );

        totalDeductionsLabel.setText(
                "Total Deductions: "
                + money(payroll.getTotalDeductions())
        );

        netSalaryLabel.setText(
                "Net Salary: " + money(payroll.getNetSalary())
        );
    }

    private void clearCalculation() {

        calculatedPayroll = null;

        taxField.setText("0.00");
        ssnitField.setText("0.00");

        basicSalaryLabel.setText("Basic Salary: GH₵ 0.00");
        grossSalaryLabel.setText("Gross Salary: GH₵ 0.00");
        taxSummaryLabel.setText("Tax: GH₵ 0.00");
        ssnitSummaryLabel.setText("SSNIT: GH₵ 0.00");
        totalDeductionsLabel.setText("Total Deductions: GH₵ 0.00");
        netSalaryLabel.setText("Net Salary: GH₵ 0.00");
    }

    // --------------------------------------------------
    // SAVE PAYROLL
    // --------------------------------------------------

    private void savePayroll() {

        try {

            calculatePayroll(false);

            if (calculatedPayroll == null) {
                return;
            }

            int employeeId = calculatedPayroll.getEmployeeId();
            int month = calculatedPayroll.getPayMonth();
            int year = calculatedPayroll.getPayYear();

            if (payrollRepository.existsForPeriod(
                    employeeId, month, year)) {

                showError(
                        "Payroll already exists for this employee "
                        + "and period."
                );

                return;
            }

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Save payroll for "
                    + calculatedPayroll.getEmployeeName()
                    + "?\n\nGross Salary: "
                    + money(calculatedPayroll.getGrossSalary())
                    + "\nTax: "
                    + money(calculatedPayroll.getTaxDeduction())
                    + "\nSSNIT: "
                    + money(calculatedPayroll.getSsnitDeduction())
                    + "\nOther Deductions: "
                    + money(calculatedPayroll.getOtherDeductions())
                    + "\nNet Salary: "
                    + money(calculatedPayroll.getNetSalary())
                    + "\n\nPayment status: PENDING",
                    "Confirm Payroll",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            payrollRepository.save(calculatedPayroll);

            JOptionPane.showMessageDialog(
                    this,
                    "Payroll saved successfully!\n"
                    + "Payment status: PENDING",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadPayrollRecords();

        } catch (SQLException e) {

            showError(
                    "Could not save payroll. "
                    + "Check that the employee exists and "
                    + "that payroll has not already been saved.\n"
                    + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            showError(e.getMessage());
        }
    }

    // --------------------------------------------------
    // PAYROLL TABLE
    // --------------------------------------------------

    private JPanel createPayrollTable() {

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 230, 238)
                ),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel heading = new JLabel("Payroll Records");

        heading.setFont(
                new Font("Arial", Font.BOLD, 17)
        );

        heading.setForeground(TEXT);

        String[] columns = {
                "Employee",
                "Period",
                "Basic Salary",
                "Gross Salary",
                "Deductions",
                "Net Salary",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        payrollTable = new JTable(tableModel);

        payrollTable.setRowHeight(32);
        payrollTable.setFont(new Font("Arial", Font.PLAIN, 12));

        payrollTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        payrollTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        payrollTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(payrollTable);
        scrollPane.setPreferredSize(new Dimension(700, 180));

        JButton markPaidButton = createButton(
                "Mark as Paid",
                new Color(35, 150, 95)
        );

        markPaidButton.addActionListener(
                event -> markSelectedPayrollAsPaid()
        );

        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(markPaidButton);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --------------------------------------------------
    // MARK SELECTED PAYROLL AS PAID
    // --------------------------------------------------

    private void markSelectedPayrollAsPaid() {

        int selectedRow = payrollTable.getSelectedRow();

        if (selectedRow == -1) {
            showError("Please select a payroll record first.");
            return;
        }

        int modelRow =
                payrollTable.convertRowIndexToModel(selectedRow);

        if (loadedPayrollRecords == null
                || modelRow >= loadedPayrollRecords.size()) {

            showError(
                    "Could not identify the selected payroll record."
            );

            return;
        }

        Payroll payroll = loadedPayrollRecords.get(modelRow);

        if ("PAID".equalsIgnoreCase(payroll.getPaymentStatus())) {

            JOptionPane.showMessageDialog(
                    this,
                    "This payroll has already been marked as paid.",
                    "Payment Status",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Confirm that payment has actually been made to "
                + payroll.getEmployeeName()
                + "?\n\nNet Salary: "
                + money(payroll.getNetSalary()),
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            payrollRepository.updatePaymentStatus(
                    payroll.getId(),
                    "PAID",
                    LocalDate.now()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Payroll successfully marked as PAID.",
                    "Payment Confirmed",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadPayrollRecords();

        } catch (SQLException e) {

            showError(
                    "Could not update payment status: "
                    + e.getMessage()
            );
        }
    }

    // --------------------------------------------------
    // LOAD ACTIVE EMPLOYEES
    // --------------------------------------------------

    private void loadActiveEmployees() {

        employeeCombo.removeAllItems();

        try {

            List<Employee> employees =
                    employeeRepository.findAllActive();

            for (Employee employee : employees) {
                employeeCombo.addItem(employee);
            }

            boolean hasEmployees = !employees.isEmpty();

            employeeCombo.setEnabled(hasEmployees);
            calculateButton.setEnabled(hasEmployees);
            saveButton.setEnabled(hasEmployees);

            if (!hasEmployees) {

                employeeStatusLabel.setText(
                        "No active employees. Activate or register an "
                        + "employee before processing payroll."
                );

            } else {

                employeeStatusLabel.setText(" ");
            }

        } catch (SQLException e) {

            employeeCombo.setEnabled(false);
            calculateButton.setEnabled(false);
            saveButton.setEnabled(false);

            employeeStatusLabel.setText(
                    "Could not load employees. Please refresh."
            );

            showError(
                    "Could not load employees: " + e.getMessage()
            );
        }
    }

    // --------------------------------------------------
    // LOAD PAYROLL RECORDS
    // --------------------------------------------------

    private void loadPayrollRecords() {

        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);

        try {

            List<Payroll> records =
                    payrollRepository.findAll();

            loadedPayrollRecords = records;

            for (Payroll payroll : records) {

                String period =
                        Month.of(payroll.getPayMonth())
                                .getDisplayName(
                                        TextStyle.SHORT,
                                        Locale.ENGLISH
                                )
                        + " " + payroll.getPayYear();

                tableModel.addRow(new Object[]{

                        payroll.getEmployeeName(),
                        period,
                        money(payroll.getBasicSalary()),
                        money(payroll.getGrossSalary()),
                        money(payroll.getTotalDeductions()),
                        money(payroll.getNetSalary()),
                        payroll.getPaymentStatus()
                });
            }

        } catch (SQLException e) {

            loadedPayrollRecords = null;

            showError(
                    "Could not load payroll records: "
                    + e.getMessage()
            );
        }
    }

    // --------------------------------------------------
    // CONSISTENT BUTTON DESIGN
    // --------------------------------------------------

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setUI(new BasicButtonUI());

        button.setBackground(color);
        button.setForeground(Color.WHITE);

        button.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(false);

        button.setHorizontalAlignment(SwingConstants.CENTER);

        button.setBorder(
                new EmptyBorder(10, 15, 10, 15)
        );

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    private String money(BigDecimal amount) {

        return String.format(
                Locale.US,
                "GH₵ %,.2f",
                amount
        );
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "PayFlow",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
