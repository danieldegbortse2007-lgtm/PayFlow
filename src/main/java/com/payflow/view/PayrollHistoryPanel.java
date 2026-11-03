
package com.payflow.view;

import com.payflow.model.Payroll;
import com.payflow.repository.PayrollRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class PayrollHistoryPanel extends JPanel {

    private final Color BACKGROUND = new Color(245, 247, 251);
    private final Color BLUE = new Color(48, 105, 220);
    private final Color TEXT = new Color(35, 45, 65);
    private final Color MUTED = new Color(110, 120, 140);

    private final PayrollRepository payrollRepository =
            new PayrollRepository();

    private JTextField searchField;
    private JComboBox<String> monthFilter;
    private JComboBox<String> yearFilter;
    private JComboBox<String> statusFilter;

    private DefaultTableModel tableModel;
    private JTable historyTable;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel recordCountLabel;
    private JLabel totalNetLabel;

    private List<Payroll> payrollRecords;

    public PayrollHistoryPanel() {

        setLayout(new BorderLayout(0, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(10, 5, 10, 5));

        add(createHeading(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadPayrollHistory();
    }

    private JPanel createHeading() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND);

        JLabel heading = new JLabel("Payroll History");
        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "View, search and track all previously processed payroll records."
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(7));
        panel.add(subtitle);

        return panel;
    }

    private JPanel createMainContent() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BACKGROUND);

        panel.add(createFilterPanel(), BorderLayout.NORTH);
        panel.add(createHistoryTable(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFilterPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 230, 238)
                ),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        searchField = new JTextField();
        searchField.setToolTipText(
                "Search employee name or employee number"
        );

        monthFilter = new JComboBox<>();
        monthFilter.addItem("All Months");

        for (Month month : Month.values()) {
            monthFilter.addItem(
                    month.getDisplayName(
                            TextStyle.FULL,
                            Locale.ENGLISH
                    )
            );
        }

        yearFilter = new JComboBox<>();
        yearFilter.addItem("All Years");

        int currentYear = java.time.LocalDate.now().getYear();

        for (int year = currentYear + 2; year >= 2000; year--) {
            yearFilter.addItem(String.valueOf(year));
        }

        statusFilter = new JComboBox<>(
                new String[]{"All Status", "PAID", "PENDING"}
        );

        JButton searchButton = createButton("Search", BLUE);

        JButton refreshButton = createButton(
                "Refresh", new Color(100, 110, 125)
        );

        addFilterField(panel, gbc, 0, "Search Employee", searchField);
        addFilterField(panel, gbc, 1, "Month", monthFilter);
        addFilterField(panel, gbc, 2, "Year", yearFilter);
        addFilterField(panel, gbc, 3, "Payment Status", statusFilter);

        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(searchButton, gbc);

        gbc.gridx = 5;
        panel.add(refreshButton, gbc);

        searchButton.addActionListener(event -> applyFilters());

        searchField.addActionListener(event -> applyFilters());

        monthFilter.addActionListener(event -> applyFilters());
        yearFilter.addActionListener(event -> applyFilters());
        statusFilter.addActionListener(event -> applyFilters());

        refreshButton.addActionListener(event -> {
            searchField.setText("");
            monthFilter.setSelectedIndex(0);
            yearFilter.setSelectedIndex(0);
            statusFilter.setSelectedIndex(0);
            loadPayrollHistory();
        });

        return panel;
    }

    private void addFilterField(
            JPanel panel,
            GridBagConstraints gbc,
            int column,
            String label,
            JComponent component) {

        JPanel field = new JPanel(new BorderLayout(0, 5));
        field.setBackground(Color.WHITE);

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Arial", Font.BOLD, 11));
        fieldLabel.setForeground(TEXT);

        component.setFont(new Font("Arial", Font.PLAIN, 12));

        field.add(fieldLabel, BorderLayout.NORTH);
        field.add(component, BorderLayout.CENTER);

        gbc.gridx = column;
        gbc.gridy = 0;
        gbc.weightx = 1;

        panel.add(field, gbc);
    }

    private JPanel createHistoryTable() {

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 230, 238)
                ),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel headingPanel = new JPanel(new BorderLayout());
        headingPanel.setBackground(Color.WHITE);

        JLabel heading = new JLabel("All Payroll Records");
        heading.setFont(new Font("Arial", Font.BOLD, 17));
        heading.setForeground(TEXT);

        recordCountLabel = new JLabel("0 records");
        recordCountLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        recordCountLabel.setForeground(MUTED);

        headingPanel.add(heading, BorderLayout.WEST);
        headingPanel.add(recordCountLabel, BorderLayout.EAST);

        String[] columns = {
                "Employee Number",
                "Employee Name",
                "Period",
                "Basic Salary",
                "Allowances",
                "Overtime",
                "Gross Salary",
                "Deductions",
                "Net Salary",
                "Status",
                "Payment Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);

        historyTable.setRowHeight(32);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 11));

        historyTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 11)
        );

        historyTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        historyTable.setFillsViewportHeight(true);

        sorter = new TableRowSorter<>(tableModel);
        historyTable.setRowSorter(sorter);

        int[] widths = {
                120, 150, 90, 115, 110, 100,
                115, 115, 115, 90, 110
        };

        for (int i = 0; i < widths.length; i++) {
            historyTable.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);

        totalNetLabel = new JLabel("Total Net Payroll: GH₵ 0.00");
        totalNetLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalNetLabel.setForeground(TEXT);

        footer.add(totalNetLabel, BorderLayout.EAST);

        panel.add(headingPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    public void loadPayrollHistory() {

        tableModel.setRowCount(0);

        try {

            payrollRecords = payrollRepository.findAll();

            BigDecimal totalNet = BigDecimal.ZERO;

            for (Payroll payroll : payrollRecords) {

                String period = String.format(
                        Locale.ENGLISH,
                        "%s %d",
                        Month.of(payroll.getPayMonth())
                                .getDisplayName(
                                        TextStyle.SHORT,
                                        Locale.ENGLISH
                                ),
                        payroll.getPayYear()
                );

                String paymentDate =
                        payroll.getPaymentDate() == null
                                ? "Not paid"
                                : payroll.getPaymentDate().toString();

                tableModel.addRow(new Object[]{
                        payroll.getEmployeeNumber(),
                        payroll.getEmployeeName(),
                        period,
                        money(payroll.getBasicSalary()),
                        money(payroll.getAllowances()),
                        money(payroll.getOvertime()),
                        money(payroll.getGrossSalary()),
                        money(payroll.getTotalDeductions()),
                        money(payroll.getNetSalary()),
                        payroll.getPaymentStatus(),
                        paymentDate
                });

                totalNet = totalNet.add(payroll.getNetSalary());
            }

            recordCountLabel.setText(
                    payrollRecords.size()
                    + (payrollRecords.size() == 1
                    ? " record"
                    : " records")
            );

            totalNetLabel.setText(
                    "Total Net Payroll: " + money(totalNet)
            );

            applyFilters();

        } catch (SQLException e) {

            payrollRecords = List.of();

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load payroll history:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void applyFilters() {

        if (sorter == null) {
            return;
        }

        String search = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        String selectedMonth =
                (String) monthFilter.getSelectedItem();

        String selectedYear =
                (String) yearFilter.getSelectedItem();

        String selectedStatus =
                (String) statusFilter.getSelectedItem();

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {

            @Override
            public boolean include(
                    Entry<? extends DefaultTableModel, ? extends Integer> entry) {

                String employeeNumber =
                        entry.getStringValue(0).toLowerCase(Locale.ROOT);

                String employeeName =
                        entry.getStringValue(1).toLowerCase(Locale.ROOT);

                String period = entry.getStringValue(2);

                String status = entry.getStringValue(9);

                if (!search.isEmpty()
                        && !employeeNumber.contains(search)
                        && !employeeName.contains(search)) {
                    return false;
                }

                if (selectedMonth != null
                        && !"All Months".equals(selectedMonth)) {

                    String monthAbbreviation =
                            Month.valueOf(
                                    selectedMonth.toUpperCase(Locale.ENGLISH)
                            ).getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.ENGLISH
                            );

                    if (!period.startsWith(monthAbbreviation)) {
                        return false;
                    }
                }

                if (selectedYear != null
                        && !"All Years".equals(selectedYear)
                        && !period.endsWith(selectedYear)) {
                    return false;
                }

                return selectedStatus == null
                        || "All Status".equals(selectedStatus)
                        || selectedStatus.equals(status);
            }
        });

        recordCountLabel.setText(
                historyTable.getRowCount() + " matching records"
        );
    }

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setUI(new BasicButtonUI());
        button.setBackground(color);
        button.setForeground(Color.WHITE);

        button.setFont(new Font("Arial", Font.BOLD, 11));

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(false);

        button.setHorizontalAlignment(SwingConstants.CENTER);

        button.setBorder(
                new EmptyBorder(9, 12, 9, 12)
        );

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private String money(BigDecimal amount) {

        return String.format(
                Locale.US,
                "GH₵ %,.2f",
                amount
        );
    }
}
