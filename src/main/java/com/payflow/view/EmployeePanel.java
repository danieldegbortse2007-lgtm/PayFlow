
package com.payflow.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;

import com.payflow.model.Employee;
import com.payflow.repository.EmployeeRepository;

public class EmployeePanel extends JPanel {

    private final EmployeeRepository repository =
            new EmployeeRepository();

    private final Runnable onEmployeeDeleted;

    private final Color BACKGROUND = new Color(245, 247, 251);
    private final Color BLUE = new Color(48, 105, 220);
    private final Color TEXT = new Color(35, 45, 65);

    private final DefaultTableModel tableModel;
    private final JTable employeeTable;
    private final JTextField searchField;

    private List<Employee> displayedEmployees = Collections.emptyList();

    public EmployeePanel() {
        this(null);
    }

    public EmployeePanel(Runnable onEmployeeDeleted) {

        this.onEmployeeDeleted = onEmployeeDeleted != null
                ? onEmployeeDeleted
                : () -> {};

        setLayout(new BorderLayout(0, 20));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // PAGE HEADING
        JLabel heading = new JLabel("Employee Management");
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Register, view, update and manage your employees."
        );
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BACKGROUND);
        titlePanel.add(heading);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(subtitle);

        // SEARCH FIELD AND BUTTONS
        searchField = new JTextField(18);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton searchButton = createButton("Search", BLUE);
        JButton refreshButton = createButton(
                "Refresh", new Color(90, 105, 125));
        JButton addButton = createButton(
                "Add Employee", new Color(35, 155, 100));
        JButton editButton = createButton(
                "Edit Employee", BLUE);
        JButton deactivateButton = createButton(
                "Deactivate", new Color(190, 65, 65));
        JButton activateButton = createButton(
                "Activate", new Color(35, 155, 100));
        JButton deleteButton = createButton(
                "Delete Employee", new Color(160, 45, 45));

        // FIRST TOOLBAR ROW
        JPanel firstRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 5));
        firstRow.setBackground(BACKGROUND);

        firstRow.add(searchField);
        firstRow.add(searchButton);
        firstRow.add(refreshButton);
        firstRow.add(addButton);
        firstRow.add(editButton);

        // SECOND TOOLBAR ROW
        JPanel secondRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 5));
        secondRow.setBackground(BACKGROUND);

        secondRow.add(deactivateButton);
        secondRow.add(activateButton);
        secondRow.add(deleteButton);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(
                new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBackground(BACKGROUND);

        searchPanel.add(firstRow);
        searchPanel.add(secondRow);

        JPanel topPanel = new JPanel(
                new BorderLayout(0, 18));
        topPanel.setBackground(BACKGROUND);
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        // EMPLOYEE TABLE
        String[] columns = {
                "Employee No.",
                "First Name",
                "Last Name",
                "Department",
                "Job Title",
                "Basic Salary (GH₵)",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        employeeTable = new JTable(tableModel);
        employeeTable.setFont(new Font("Arial", Font.PLAIN, 13));
        employeeTable.setRowHeight(35);
        employeeTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        employeeTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13));
        employeeTable.setFillsViewportHeight(true);

        JScrollPane tableScroll = new JScrollPane(employeeTable);
        tableScroll.setBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 230, 238)));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        // BUTTON ACTIONS
        addButton.addActionListener(
                event -> showEmployeeForm(null));

        editButton.addActionListener(event -> {
            Employee selected = getSelectedEmployee();
            if (selected != null) {
                showEmployeeForm(selected);
            }
        });

        deactivateButton.addActionListener(
                event -> deactivateSelected());

        activateButton.addActionListener(
                event -> activateSelected());

        deleteButton.addActionListener(
                event -> deleteSelected());

        refreshButton.addActionListener(event -> {
            searchField.setText("");
            loadEmployees();
        });

        searchButton.addActionListener(
                event -> searchEmployees());

        searchField.addActionListener(
                event -> searchEmployees());

        loadEmployees();
    }

    // --------------------------------------------------
    // CREATE CLEARLY VISIBLE BUTTONS
    // --------------------------------------------------

    private JButton createButton(String text, Color background) {

        JButton button = new JButton(text);

        button.setUI(new BasicButtonUI());

        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(background);

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(false);

        button.setHorizontalAlignment(SwingConstants.CENTER);

        button.setBorder(
                new EmptyBorder(10, 13, 10, 13));

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        button.setPreferredSize(null);

        return button;
    }

    // --------------------------------------------------
    // SAFE TEXT HELPER
    // --------------------------------------------------

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    // --------------------------------------------------
    // LOAD EMPLOYEES
    // --------------------------------------------------

    private void loadEmployees() {

        try {

            List<Employee> employees = repository.findAll();

            updateTable(employees);

        } catch (SQLException e) {

            showError("Could not load employees.", e);
        }
    }

    public void refreshRecords() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadEmployees();
        } else {
            searchEmployees();
        }
    }

    // --------------------------------------------------
    // SEARCH EMPLOYEES
    // --------------------------------------------------

    private void searchEmployees() {

        String keyword = searchField.getText().trim();

        try {

            if (keyword.isEmpty()) {
                loadEmployees();
                return;
            }

            List<Employee> employees = repository.search(keyword);

            updateTable(employees);

        } catch (SQLException e) {

            showError("Employee search failed.", e);
        }
    }

    // --------------------------------------------------
    // UPDATE EMPLOYEE TABLE
    // --------------------------------------------------

    private void updateTable(List<Employee> employees) {

        tableModel.setRowCount(0);

        if (employees == null) {
            displayedEmployees = Collections.emptyList();
            return;
        }

        displayedEmployees = employees;

        for (Employee employee : employees) {

            if (employee == null) {
                continue;
            }

            String status = employee.isActive()
                    ? "Active"
                    : "Inactive";

            BigDecimal salary = employee.getBasicSalary();

            Object[] row = {
                    safeText(employee.getEmployeeNumber()),
                    safeText(employee.getFirstName()),
                    safeText(employee.getLastName()),
                    safeText(employee.getDepartment()),
                    safeText(employee.getJobTitle()),
                    salary == null ? "0.00" : salary.toPlainString(),
                    status
            };

            tableModel.addRow(row);
        }
    }

    // --------------------------------------------------
    // GET SELECTED EMPLOYEE
    // --------------------------------------------------

    private Employee getSelectedEmployee() {

        int selectedRow = employeeTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an employee first.",
                    "No Employee Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        int modelRow =
                employeeTable.convertRowIndexToModel(selectedRow);

        if (modelRow < 0 || modelRow >= displayedEmployees.size()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not identify the selected employee. "
                            + "Please refresh the table.",
                    "Selection Error",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        return displayedEmployees.get(modelRow);
    }

    // --------------------------------------------------
    // REGISTER OR EDIT EMPLOYEE
    // --------------------------------------------------

    private void showEmployeeForm(Employee employee) {

        boolean editing = employee != null;

        JTextField numberField = new JTextField(
                editing ? safeText(employee.getEmployeeNumber()) : "");

        JTextField firstNameField = new JTextField(
                editing ? safeText(employee.getFirstName()) : "");

        JTextField lastNameField = new JTextField(
                editing ? safeText(employee.getLastName()) : "");

        JTextField emailField = new JTextField(
                editing ? safeText(employee.getEmail()) : "");

        JTextField phoneField = new JTextField(
                editing ? safeText(employee.getPhone()) : "");

        JTextField departmentField = new JTextField(
                editing ? safeText(employee.getDepartment()) : "");

        JTextField jobTitleField = new JTextField(
                editing ? safeText(employee.getJobTitle()) : "");

        BigDecimal existingSalary = editing
                ? employee.getBasicSalary()
                : null;

        JTextField salaryField = new JTextField(
                existingSalary == null
                        ? ""
                        : existingSalary.toPlainString());

        JPanel form = new JPanel(
                new GridLayout(0, 2, 10, 12));

        form.add(new JLabel("Employee Number:"));
        form.add(numberField);
        form.add(new JLabel("First Name:"));
        form.add(firstNameField);
        form.add(new JLabel("Last Name:"));
        form.add(lastNameField);
        form.add(new JLabel("Email Address:"));
        form.add(emailField);
        form.add(new JLabel("Phone Number:"));
        form.add(phoneField);
        form.add(new JLabel("Department:"));
        form.add(departmentField);
        form.add(new JLabel("Job Title:"));
        form.add(jobTitleField);
        form.add(new JLabel("Basic Salary (GH₵):"));
        form.add(salaryField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                editing ? "Edit Employee" : "Register New Employee",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            String employeeNumber = numberField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String department = departmentField.getText().trim();
            String jobTitle = jobTitleField.getText().trim();
            String salaryText = salaryField.getText().trim();

            if (employeeNumber.isEmpty()
                    || firstName.isEmpty()
                    || lastName.isEmpty()
                    || email.isEmpty()
                    || phone.isEmpty()
                    || department.isEmpty()
                    || jobTitle.isEmpty()
                    || salaryText.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please complete all employee fields.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if (!email.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid email address.",
                        "Invalid Email",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            BigDecimal salary = new BigDecimal(salaryText);

            if (salary.compareTo(BigDecimal.ZERO) < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Salary cannot be negative.",
                        "Invalid Salary",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            Employee newEmployee = new Employee(
                    employeeNumber,
                    firstName,
                    lastName,
                    email,
                    phone,
                    department,
                    jobTitle,
                    salary
            );

            if (editing) {

                newEmployee.setId(employee.getId());
                newEmployee.setActive(employee.isActive());

                boolean updated = repository.update(newEmployee);

                if (!updated) {
                    throw new SQLException(
                            "Employee record was not found.");
                }

                JOptionPane.showMessageDialog(
                        this,
                        "Employee details updated successfully.");

            } else {

                if (repository.exists(employeeNumber, email)) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Employee number or email already exists.",
                            "Duplicate Employee",
                            JOptionPane.WARNING_MESSAGE
                    );

                    return;
                }

                repository.save(newEmployee);

                JOptionPane.showMessageDialog(
                        this,
                        "Employee registered successfully.");
            }

            searchField.setText("");
            loadEmployees();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid salary, for example 2500.00.",
                    "Invalid Salary",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException e) {

            showError("Could not save employee information.", e);
        }
    }

    // --------------------------------------------------
    // DEACTIVATE EMPLOYEE
    // --------------------------------------------------

    private void deactivateSelected() {

        Employee employee = getSelectedEmployee();

        if (employee == null) {
            return;
        }

        if (!employee.isActive()) {

            JOptionPane.showMessageDialog(
                    this,
                    "This employee is already inactive.");

            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to deactivate "
                        + safeText(employee.getFullName())
                        + "?\nThey will no longer be available "
                        + "for new payroll processing.",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            repository.deactivate(employee.getId());

            JOptionPane.showMessageDialog(
                    this,
                    "Employee deactivated successfully.");

            loadEmployees();

        } catch (SQLException e) {

            showError("Could not deactivate employee.", e);
        }
    }

    // --------------------------------------------------
    // ACTIVATE EMPLOYEE
    // --------------------------------------------------

    private void activateSelected() {

        Employee employee = getSelectedEmployee();

        if (employee == null) {
            return;
        }

        if (employee.isActive()) {

            JOptionPane.showMessageDialog(
                    this,
                    "This employee is already active.");

            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Do you want to activate "
                        + safeText(employee.getFullName())
                        + " again?",
                "Confirm Activation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            repository.activate(employee.getId());

            JOptionPane.showMessageDialog(
                    this,
                    "Employee activated successfully.");

            loadEmployees();

        } catch (SQLException e) {

            showError("Could not activate employee.", e);
        }
    }

    // --------------------------------------------------
    // PERMANENTLY DELETE EMPLOYEE AND PAYROLL HISTORY
    // --------------------------------------------------

    private void deleteSelected() {

        Employee employee = getSelectedEmployee();

        if (employee == null) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "WARNING: PERMANENT DELETION\n\n"
                        + "You are about to permanently delete "
                        + safeText(employee.getFullName()) + ".\n\n"
                        + "This will remove:\n"
                        + "- Their employee details\n"
                        + "- All their payroll records\n"
                        + "- Their payslip history\n\n"
                        + "This action cannot be undone.\n"
                        + "Are you sure you want to continue?",
                "Confirm Permanent Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean deleted =
                    repository.deletePermanently(employee.getId());

            if (!deleted) {

                JOptionPane.showMessageDialog(
                        this,
                        "The employee could not be found. "
                                + "Please refresh the employee list.",
                        "Employee Not Found",
                        JOptionPane.WARNING_MESSAGE
                );

                loadEmployees();
                return;
            }

            searchField.setText("");
            loadEmployees();

            JOptionPane.showMessageDialog(
                    this,
                    safeText(employee.getFullName())
                            + " and their payroll history "
                            + "have been permanently deleted.",
                    "Deletion Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            onEmployeeDeleted.run();

        } catch (SQLException e) {

            showError(
                    "Could not permanently delete employee. "
                            + "No deletion was completed if the "
                            + "database transaction was rolled back.",
                    e
            );
        }
    }

    // --------------------------------------------------
    // DISPLAY ERRORS
    // --------------------------------------------------

    private void showError(String message, Exception e) {

        e.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                message + "\n" + e.getMessage(),
                "PayFlow Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
